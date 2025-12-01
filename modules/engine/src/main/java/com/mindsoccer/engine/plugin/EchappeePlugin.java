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
 * Plugin ECHAPPÉE - Course contre le temps.
 *
 * Règles:
 * - Chaque équipe a 60 secondes pour répondre à un maximum de questions
 * - +10 points par bonne réponse
 * - Les mauvaises réponses ne rapportent rien mais font perdre du temps
 * - Bonus de +5 points par question au-delà de 5 bonnes réponses
 */
@Component
public class EchappeePlugin extends AbstractRulePlugin {

    private static final int BASE_POINTS = 10;
    private static final int BONUS_POINTS = 5;
    private static final int BONUS_THRESHOLD = 5;
    private static final long ROUND_TIME_MS = 60_000L;
    private static final long TIME_PENALTY_MS = 3_000L;

    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String CORRECT_COUNT_A_KEY = "correctCountA";
    private static final String CORRECT_COUNT_B_KEY = "correctCountB";
    private static final String TOTAL_POINTS_A_KEY = "totalPointsA";
    private static final String TOTAL_POINTS_B_KEY = "totalPointsB";
    private static final String TEAM_B_STARTED_KEY = "teamBStarted";

    public EchappeePlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.ECHAPPEE;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.ECHAPPEE);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ROUND_TIME_MS), Map.of(
                CURRENT_TEAM_KEY, TeamSide.A.name(),
                CORRECT_COUNT_A_KEY, 0,
                CORRECT_COUNT_B_KEY, 0,
                TOTAL_POINTS_A_KEY, 0,
                TOTAL_POINTS_B_KEY, 0,
                TEAM_B_STARTED_KEY, false
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
            return handleTeamFinished(newState);
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

        // Seule l'équipe en cours peut répondre
        if (payload.team() != currentTeam) {
            return state;
        }

        boolean isTeamA = currentTeam == TeamSide.A;
        String countKey = isTeamA ? CORRECT_COUNT_A_KEY : CORRECT_COUNT_B_KEY;
        String pointsKey = isTeamA ? TOTAL_POINTS_A_KEY : TOTAL_POINTS_B_KEY;

        boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

        if (correct) {
            int correctCount = getExtra(state, countKey, 0) + 1;
            int totalPoints = getExtra(state, pointsKey, 0);

            int points = BASE_POINTS;
            if (correctCount > BONUS_THRESHOLD) {
                points += BONUS_POINTS;
            }
            totalPoints += points;

            RoundState newState = new RoundState(
                    state.type(),
                    RoundState.Phase.QUESTION_SHOWN,
                    state.questionIndex() + 1,
                    null,
                    null,
                    state.remainingTimeMs(),
                    false,
                    state.extra()
            );

            return withExtras(newState, Map.of(
                    countKey, correctCount,
                    pointsKey, totalPoints
            ));
        } else {
            // Mauvaise réponse: pénalité de temps
            long newTime = Math.max(0, state.remainingTimeMs() - TIME_PENALTY_MS);

            RoundState newState = new RoundState(
                    state.type(),
                    RoundState.Phase.QUESTION_SHOWN,
                    state.questionIndex() + 1,
                    null,
                    null,
                    newTime,
                    false,
                    state.extra()
            );

            if (newTime <= 0) {
                return handleTeamFinished(newState);
            }

            return newState;
        }
    }

    private RoundState handleTeamFinished(RoundState state) {
        boolean teamBStarted = getExtra(state, TEAM_B_STARTED_KEY, false);
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));

        if (currentTeam == TeamSide.A && !teamBStarted) {
            // Équipe A a terminé, passer à l'équipe B
            RoundState newState = new RoundState(
                    state.type(),
                    RoundState.Phase.QUESTION_SHOWN,
                    0,
                    null,
                    null,
                    ROUND_TIME_MS,
                    false,
                    state.extra()
            );

            return withExtras(newState, Map.of(
                    CURRENT_TEAM_KEY, TeamSide.B.name(),
                    TEAM_B_STARTED_KEY, true
            ));
        } else {
            // Les deux équipes ont terminé
            return finishRound(state);
        }
    }

    private RoundState finishRound(RoundState state) {
        int pointsA = getExtra(state, TOTAL_POINTS_A_KEY, 0);
        int pointsB = getExtra(state, TOTAL_POINTS_B_KEY, 0);
        int correctA = getExtra(state, CORRECT_COUNT_A_KEY, 0);
        int correctB = getExtra(state, CORRECT_COUNT_B_KEY, 0);

        String winner = "";
        if (pointsA > pointsB) {
            winner = TeamSide.A.name();
        } else if (pointsB > pointsA) {
            winner = TeamSide.B.name();
        }

        return withExtras(state.completed(), Map.of(
                "roundWinner", winner,
                "finalPointsA", pointsA,
                "finalPointsB", pointsB,
                "finalCorrectA", correctA,
                "finalCorrectB", correctB
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

    public int getBonusPoints() {
        return BONUS_POINTS;
    }

    public long getTimePenaltyMs() {
        return TIME_PENALTY_MS;
    }
}
