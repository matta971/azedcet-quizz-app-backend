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
 * Plugin SAUT PATRIOTIQUE - Questions sur le patrimoine national.
 *
 * Règles:
 * - 5 questions sur la culture et l'histoire nationale
 * - Questions alternées entre les équipes
 * - Bonne réponse: +15 points
 * - Mauvaise réponse: l'autre équipe peut voler (+10 points)
 * - Bonus "Patriote" pour 5/5: +20 points
 */
@Component
public class SautPatriotiquePlugin extends AbstractRulePlugin {

    private static final int TOTAL_QUESTIONS = 5;
    private static final int CORRECT_POINTS = 15;
    private static final int STEAL_POINTS = 10;
    private static final int PERFECT_BONUS = 20;
    private static final long ANSWER_TIME_MS = 15_000L;

    private static final String PHASE_KEY = "phase";
    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String CORRECT_COUNT_A_KEY = "correctCountA";
    private static final String CORRECT_COUNT_B_KEY = "correctCountB";

    public SautPatriotiquePlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.SAUT_PATRIOTIQUE;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.SAUT_PATRIOTIQUE);

        TeamSide startingTeam = ctx.leadingTeam() == null ? TeamSide.A :
                (ctx.leadingTeam() == TeamSide.A ? TeamSide.B : TeamSide.A);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                PHASE_KEY, "ANSWER",
                CURRENT_TEAM_KEY, startingTeam.name(),
                CORRECT_COUNT_A_KEY, 0,
                CORRECT_COUNT_B_KEY, 0
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

        // En phase vol, seule l'équipe adverse peut répondre
        if ("STEAL".equals(phase)) {
            TeamSide expectedTeam = getOppositeTeam(currentTeam);
            if (payload.team() != expectedTeam) {
                return state;
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            if (correct) {
                return advanceToNextQuestion(state);
            } else {
                return advanceToNextQuestion(state);
            }
        }

        // En phase normale, seule l'équipe désignée peut répondre
        if (payload.team() != currentTeam) {
            return state;
        }

        boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

        if (correct) {
            String countKey = currentTeam == TeamSide.A ? CORRECT_COUNT_A_KEY : CORRECT_COUNT_B_KEY;
            int count = getExtra(state, countKey, 0) + 1;
            RoundState newState = withExtra(state, countKey, count);
            return advanceToNextQuestion(newState);
        } else {
            // L'autre équipe peut voler
            TeamSide oppositeTeam = getOppositeTeam(currentTeam);
            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "STEAL",
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
        int correctA = getExtra(state, CORRECT_COUNT_A_KEY, 0);
        int correctB = getExtra(state, CORRECT_COUNT_B_KEY, 0);

        String perfectTeam = "";
        int bonus = 0;

        if (correctA == TOTAL_QUESTIONS) {
            perfectTeam = TeamSide.A.name();
            bonus = PERFECT_BONUS;
        } else if (correctB == TOTAL_QUESTIONS) {
            perfectTeam = TeamSide.B.name();
            bonus = PERFECT_BONUS;
        }

        return withExtras(state.completed(), Map.of(
                "perfectTeam", perfectTeam,
                "perfectBonus", bonus
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "ANSWER");

        if ("STEAL".equals(phase)) {
            return advanceToNextQuestion(state);
        } else {
            TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
            TeamSide oppositeTeam = getOppositeTeam(currentTeam);
            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "STEAL",
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

    public int getCorrectPoints() {
        return CORRECT_POINTS;
    }

    public int getStealPoints() {
        return STEAL_POINTS;
    }

    public int getPerfectBonus() {
        return PERFECT_BONUS;
    }
}
