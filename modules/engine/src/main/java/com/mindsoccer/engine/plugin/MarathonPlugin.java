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
 * Plugin MARATHON - Endurance avec questions en série.
 *
 * Règles:
 * - 10 questions par équipe
 * - Les équipes répondent en parallèle (ou alternativement en mode classique)
 * - +8 points par bonne réponse
 * - Bonus de vitesse: +2 points si réponse en moins de 5 secondes
 * - Série bonus: +5 points supplémentaires pour 3 bonnes réponses consécutives
 */
@Component
public class MarathonPlugin extends AbstractRulePlugin {

    private static final int QUESTIONS_PER_TEAM = 10;
    private static final int BASE_POINTS = 8;
    private static final int SPEED_BONUS = 2;
    private static final long SPEED_THRESHOLD_MS = 5_000L;
    private static final int STREAK_BONUS = 5;
    private static final int STREAK_THRESHOLD = 3;
    private static final long ANSWER_TIME_MS = 15_000L;

    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String QUESTION_INDEX_A_KEY = "questionIndexA";
    private static final String QUESTION_INDEX_B_KEY = "questionIndexB";
    private static final String STREAK_A_KEY = "streakA";
    private static final String STREAK_B_KEY = "streakB";
    private static final String TOTAL_POINTS_A_KEY = "totalPointsA";
    private static final String TOTAL_POINTS_B_KEY = "totalPointsB";

    public MarathonPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.MARATHON;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.MARATHON);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                CURRENT_TEAM_KEY, TeamSide.A.name(),
                QUESTION_INDEX_A_KEY, 0,
                QUESTION_INDEX_B_KEY, 0,
                STREAK_A_KEY, 0,
                STREAK_B_KEY, 0,
                TOTAL_POINTS_A_KEY, 0,
                TOTAL_POINTS_B_KEY, 0
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

        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));

        if (payload.team() != currentTeam) {
            return state;
        }

        boolean isTeamA = currentTeam == TeamSide.A;
        String indexKey = isTeamA ? QUESTION_INDEX_A_KEY : QUESTION_INDEX_B_KEY;
        String streakKey = isTeamA ? STREAK_A_KEY : STREAK_B_KEY;
        String pointsKey = isTeamA ? TOTAL_POINTS_A_KEY : TOTAL_POINTS_B_KEY;

        boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

        int questionIndex = getExtra(state, indexKey, 0);
        int streak = getExtra(state, streakKey, 0);
        int totalPoints = getExtra(state, pointsKey, 0);

        if (correct) {
            streak++;
            int points = BASE_POINTS;

            // Bonus de vitesse
            long responseTime = state.remainingTimeMs() > 0 ? (ANSWER_TIME_MS - state.remainingTimeMs()) : ANSWER_TIME_MS;
            if (responseTime < SPEED_THRESHOLD_MS) {
                points += SPEED_BONUS;
            }

            // Bonus de série
            if (streak >= STREAK_THRESHOLD && streak % STREAK_THRESHOLD == 0) {
                points += STREAK_BONUS;
            }

            totalPoints += points;
        } else {
            streak = 0;
        }

        questionIndex++;

        // Vérifier si l'équipe a terminé
        if (questionIndex >= QUESTIONS_PER_TEAM) {
            // Passer à l'autre équipe ou terminer
            int otherIndex = isTeamA
                    ? getExtra(state, QUESTION_INDEX_B_KEY, 0)
                    : getExtra(state, QUESTION_INDEX_A_KEY, 0);

            RoundState newState = withExtras(state, Map.of(
                    indexKey, questionIndex,
                    streakKey, streak,
                    pointsKey, totalPoints
            ));

            if (otherIndex >= QUESTIONS_PER_TEAM) {
                return finishRound(newState);
            } else {
                TeamSide nextTeam = getOppositeTeam(currentTeam);
                return withExtras(withRemainingTime(newState.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                        CURRENT_TEAM_KEY, nextTeam.name()
                ));
            }
        }

        RoundState newState = new RoundState(
                state.type(),
                RoundState.Phase.QUESTION_SHOWN,
                state.questionIndex() + 1,
                null,
                null,
                ANSWER_TIME_MS,
                false,
                state.extra()
        );

        return withExtras(newState, Map.of(
                indexKey, questionIndex,
                streakKey, streak,
                pointsKey, totalPoints
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
        boolean isTeamA = currentTeam == TeamSide.A;

        String indexKey = isTeamA ? QUESTION_INDEX_A_KEY : QUESTION_INDEX_B_KEY;
        String streakKey = isTeamA ? STREAK_A_KEY : STREAK_B_KEY;

        int questionIndex = getExtra(state, indexKey, 0) + 1;

        RoundState newState = withExtras(state, Map.of(
                indexKey, questionIndex,
                streakKey, 0 // Réinitialiser la série
        ));

        if (questionIndex >= QUESTIONS_PER_TEAM) {
            int otherIndex = isTeamA
                    ? getExtra(state, QUESTION_INDEX_B_KEY, 0)
                    : getExtra(state, QUESTION_INDEX_A_KEY, 0);

            if (otherIndex >= QUESTIONS_PER_TEAM) {
                return finishRound(newState);
            } else {
                TeamSide nextTeam = getOppositeTeam(currentTeam);
                return withExtras(withRemainingTime(newState.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                        CURRENT_TEAM_KEY, nextTeam.name()
                ));
            }
        }

        return withRemainingTime(newState.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS);
    }

    private RoundState finishRound(RoundState state) {
        int pointsA = getExtra(state, TOTAL_POINTS_A_KEY, 0);
        int pointsB = getExtra(state, TOTAL_POINTS_B_KEY, 0);

        String winner = "";
        if (pointsA > pointsB) {
            winner = TeamSide.A.name();
        } else if (pointsB > pointsA) {
            winner = TeamSide.B.name();
        }

        return withExtras(state.completed(), Map.of(
                "roundWinner", winner,
                "finalPointsA", pointsA,
                "finalPointsB", pointsB
        ));
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

    public int getSpeedBonus() {
        return SPEED_BONUS;
    }

    public int getStreakBonus() {
        return STREAK_BONUS;
    }
}
