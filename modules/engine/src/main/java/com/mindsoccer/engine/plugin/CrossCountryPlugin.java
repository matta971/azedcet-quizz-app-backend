package com.mindsoccer.engine.plugin;

import com.mindsoccer.engine.AnswerPayload;
import com.mindsoccer.engine.MatchContext;
import com.mindsoccer.engine.RoundState;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.service.AnswerValidationService;
import com.mindsoccer.scoring.service.ScoringService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * Plugin CROSS-COUNTRY - Parcours géographique avec questions.
 *
 * Règles:
 * - 6 questions sur différents pays/régions
 * - Chaque question correspond à une "étape" du parcours
 * - +10 points par bonne réponse
 * - Bonus de parcours: +15 points si toutes les étapes sont réussies
 * - Mode buzzer pour départager
 */
@Component

public class CrossCountryPlugin extends AbstractRulePlugin {

    private static final int TOTAL_STAGES = 6;
    private static final int BASE_POINTS = 10;
    private static final int PERFECT_JOURNEY_BONUS = 15;
    private static final long ANSWER_TIME_MS = 12_000L;

    private static final String PHASE_KEY = "phase";
    private static final String BUZZER_WINNER_KEY = "buzzerWinner";
    private static final String STAGES_A_KEY = "stagesCompletedA";
    private static final String STAGES_B_KEY = "stagesCompletedB";
    private static final String CONSECUTIVE_A_KEY = "consecutiveA";
    private static final String CONSECUTIVE_B_KEY = "consecutiveB";

    public CrossCountryPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.CROSS_COUNTRY;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.CROSS_COUNTRY);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANSWER_WINDOW), ANSWER_TIME_MS), Map.of(
                PHASE_KEY, "BUZZER",
                BUZZER_WINNER_KEY, "",
                STAGES_A_KEY, 0,
                STAGES_B_KEY, 0,
                CONSECUTIVE_A_KEY, 0,
                CONSECUTIVE_B_KEY, 0
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

        String phase = getExtra(state, PHASE_KEY, "BUZZER");
        String buzzerWinner = getExtra(state, BUZZER_WINNER_KEY, "");
        String playerIdStr = payload.playerId().toString();

        if ("BUZZER".equals(phase)) {
            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "ANSWER",
                    BUZZER_WINNER_KEY, playerIdStr
            ));
        }

        if ("ANSWER".equals(phase)) {
            if (!playerIdStr.equals(buzzerWinner)) {
                return state;
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            boolean isTeamA = payload.team() == TeamSide.A;
            String stagesKey = isTeamA ? STAGES_A_KEY : STAGES_B_KEY;
            String consecutiveKey = isTeamA ? CONSECUTIVE_A_KEY : CONSECUTIVE_B_KEY;

            if (correct) {
                int stages = getExtra(state, stagesKey, 0) + 1;
                int consecutive = getExtra(state, consecutiveKey, 0) + 1;

                RoundState newState = withExtras(state, Map.of(
                        stagesKey, stages,
                        consecutiveKey, consecutive
                ));

                return advanceToNextStage(newState);
            } else {
                RoundState newState = withExtra(state, consecutiveKey, 0);
                return advanceToNextStage(newState);
            }
        }

        return state;
    }

    private RoundState advanceToNextStage(RoundState state) {
        int nextIndex = state.questionIndex() + 1;

        if (nextIndex >= TOTAL_STAGES) {
            return finishRound(state);
        }

        RoundState newState = new RoundState(
                state.type(),
                RoundState.Phase.ANSWER_WINDOW,
                nextIndex,
                null,
                null,
                ANSWER_TIME_MS,
                false,
                state.extra()
        );

        return withExtras(newState, Map.of(
                PHASE_KEY, "BUZZER",
                BUZZER_WINNER_KEY, ""
        ));
    }

    private RoundState finishRound(RoundState state) {
        int stagesA = getExtra(state, STAGES_A_KEY, 0);
        int stagesB = getExtra(state, STAGES_B_KEY, 0);
        int consecutiveA = getExtra(state, CONSECUTIVE_A_KEY, 0);
        int consecutiveB = getExtra(state, CONSECUTIVE_B_KEY, 0);

        String perfectTeam = "";
        int bonus = 0;

        if (consecutiveA == TOTAL_STAGES) {
            perfectTeam = TeamSide.A.name();
            bonus = PERFECT_JOURNEY_BONUS;
        } else if (consecutiveB == TOTAL_STAGES) {
            perfectTeam = TeamSide.B.name();
            bonus = PERFECT_JOURNEY_BONUS;
        }

        return withExtras(state.completed(), Map.of(
                "finalStagesA", stagesA,
                "finalStagesB", stagesB,
                "perfectTeam", perfectTeam,
                "perfectBonus", bonus
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "BUZZER");

        if ("BUZZER".equals(phase) || "ANSWER".equals(phase)) {
            return advanceToNextStage(state);
        }

        return state;
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

    public int getPerfectJourneyBonus() {
        return PERFECT_JOURNEY_BONUS;
    }
}
