package com.mindsoccer.engine.plugin;

import com.mindsoccer.engine.AnswerPayload;
import com.mindsoccer.engine.MatchContext;
import com.mindsoccer.engine.RoundState;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.service.AnswerValidationService;
import com.mindsoccer.scoring.service.ScoringService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * Plugin CAPOEIRA - Joute verbale rythmée (questions/réponses rapides).
 *
 * Règles:
 * - Échange rapide de questions entre les équipes
 * - 8 questions au total, alternance stricte
 * - Tempo accéléré: 8s par réponse
 * - +8 points par bonne réponse
 * - Combo bonus: +3 points pour chaque bonne réponse consécutive de l'équipe
 * - Si une équipe rate, l'autre peut "contre-attaquer" pour +5 points
 */
@Component
@Profile("!test")
public class CapoeiraPlugin extends AbstractRulePlugin {

    private static final int TOTAL_QUESTIONS = 8;
    private static final int BASE_POINTS = 8;
    private static final int COMBO_BONUS = 3;
    private static final int COUNTER_POINTS = 5;
    private static final long ANSWER_TIME_MS = 8_000L;

    private static final String PHASE_KEY = "phase";
    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String COMBO_A_KEY = "comboA";
    private static final String COMBO_B_KEY = "comboB";
    private static final String MAX_COMBO_A_KEY = "maxComboA";
    private static final String MAX_COMBO_B_KEY = "maxComboB";

    public CapoeiraPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.CAPOEIRA;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.CAPOEIRA);

        TeamSide startingTeam = ctx.leadingTeam() == null ? TeamSide.A :
                (ctx.leadingTeam() == TeamSide.A ? TeamSide.B : TeamSide.A);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                PHASE_KEY, "ANSWER",
                CURRENT_TEAM_KEY, startingTeam.name(),
                COMBO_A_KEY, 0,
                COMBO_B_KEY, 0,
                MAX_COMBO_A_KEY, 0,
                MAX_COMBO_B_KEY, 0
        ));
    }

    @Override
    public RoundState onTick(MatchContext ctx, Duration dt) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        RoundState newState = decrementTime(state, dt);

        if (isTimeUp(newState)) {
            return handleTimeout(newState);
        }

        return newState;
    }

    @Override
    public RoundState onAnswer(MatchContext ctx, AnswerPayload payload) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        String phase = getExtra(state, PHASE_KEY, "ANSWER");
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));

        if ("COUNTER".equals(phase)) {
            TeamSide expectedTeam = getOppositeTeam(currentTeam);
            if (payload.team() != expectedTeam) {
                return state;
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            if (correct) {
                boolean isTeamA = expectedTeam == TeamSide.A;
                String comboKey = isTeamA ? COMBO_A_KEY : COMBO_B_KEY;
                String maxComboKey = isTeamA ? MAX_COMBO_A_KEY : MAX_COMBO_B_KEY;

                int combo = getExtra(state, comboKey, 0) + 1;
                int maxCombo = Math.max(getExtra(state, maxComboKey, 0), combo);

                RoundState newState = withExtras(state, Map.of(
                        comboKey, combo,
                        maxComboKey, maxCombo
                ));

                return advanceToNextQuestion(newState);
            } else {
                return advanceToNextQuestion(state);
            }
        }

        if (payload.team() != currentTeam) {
            return state;
        }

        boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

        boolean isTeamA = currentTeam == TeamSide.A;
        String comboKey = isTeamA ? COMBO_A_KEY : COMBO_B_KEY;
        String maxComboKey = isTeamA ? MAX_COMBO_A_KEY : MAX_COMBO_B_KEY;
        String otherComboKey = isTeamA ? COMBO_B_KEY : COMBO_A_KEY;

        if (correct) {
            int combo = getExtra(state, comboKey, 0) + 1;
            int maxCombo = Math.max(getExtra(state, maxComboKey, 0), combo);

            RoundState newState = withExtras(state, Map.of(
                    comboKey, combo,
                    maxComboKey, maxCombo,
                    otherComboKey, 0
            ));

            return advanceToNextQuestion(newState);
        } else {
            // Réinitialiser son propre combo, l'autre équipe peut contre-attaquer
            TeamSide oppositeTeam = getOppositeTeam(currentTeam);

            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "COUNTER",
                    comboKey, 0,
                    CURRENT_TEAM_KEY, oppositeTeam.name()
            ));
        }
    }

    private RoundState advanceToNextQuestion(RoundState state) {
        int nextIndex = state.questionIndex() + 1;

        if (nextIndex >= TOTAL_QUESTIONS) {
            return finishRound(state);
        }

        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
        TeamSide nextTeam = getOppositeTeam(currentTeam);

        RoundState newState = new RoundState(
                state.type(),
                RoundState.Phase.QUESTION_SHOWN,
                nextIndex,
                null,
                null,
                ANSWER_TIME_MS,
                false,
                state.extra()
        );

        return withExtras(newState, Map.of(
                PHASE_KEY, "ANSWER",
                CURRENT_TEAM_KEY, nextTeam.name()
        ));
    }

    private RoundState finishRound(RoundState state) {
        int maxComboA = getExtra(state, MAX_COMBO_A_KEY, 0);
        int maxComboB = getExtra(state, MAX_COMBO_B_KEY, 0);

        String bestComboTeam = "";
        if (maxComboA >= 3 && maxComboA > maxComboB) {
            bestComboTeam = TeamSide.A.name();
        } else if (maxComboB >= 3 && maxComboB > maxComboA) {
            bestComboTeam = TeamSide.B.name();
        }

        return withExtras(state.completed(), Map.of(
                "finalMaxComboA", maxComboA,
                "finalMaxComboB", maxComboB,
                "bestComboTeam", bestComboTeam
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "ANSWER");

        if ("COUNTER".equals(phase)) {
            return advanceToNextQuestion(state);
        } else {
            TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
            String comboKey = currentTeam == TeamSide.A ? COMBO_A_KEY : COMBO_B_KEY;
            TeamSide oppositeTeam = getOppositeTeam(currentTeam);

            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "COUNTER",
                    comboKey, 0,
                    CURRENT_TEAM_KEY, oppositeTeam.name()
            ));
        }
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring appliqué par le GameEngine
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }

    public int getBasePoints() {
        return BASE_POINTS;
    }

    public int getComboBonus() {
        return COMBO_BONUS;
    }

    public int getCounterPoints() {
        return COUNTER_POINTS;
    }
}
