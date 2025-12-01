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
 * Plugin TIRS AU BUT - Épreuve de départage style penalties.
 *
 * Règles:
 * - 5 tirs par équipe (extensible en cas d'égalité)
 * - Chaque tir = une question
 * - Bonne réponse = BUT (+10 points)
 * - Mauvaise réponse = TIR ARRÊTÉ (0 points)
 * - Alternance stricte entre les équipes
 * - En cas d'égalité après 5 tirs: mort subite
 */
@Component
public class TirsAuButPlugin extends AbstractRulePlugin {

    private static final int INITIAL_SHOTS = 5;
    private static final int GOAL_POINTS = 10;
    private static final long ANSWER_TIME_MS = 10_000L;

    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String SHOTS_A_KEY = "shotsA";
    private static final String SHOTS_B_KEY = "shotsB";
    private static final String GOALS_A_KEY = "goalsA";
    private static final String GOALS_B_KEY = "goalsB";
    private static final String SUDDEN_DEATH_KEY = "suddenDeath";

    public TirsAuButPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.TIRS_AU_BUT;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.TIRS_AU_BUT);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                CURRENT_TEAM_KEY, TeamSide.A.name(),
                SHOTS_A_KEY, 0,
                SHOTS_B_KEY, 0,
                GOALS_A_KEY, 0,
                GOALS_B_KEY, 0,
                SUDDEN_DEATH_KEY, false
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

        boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

        boolean isTeamA = currentTeam == TeamSide.A;
        String shotsKey = isTeamA ? SHOTS_A_KEY : SHOTS_B_KEY;
        String goalsKey = isTeamA ? GOALS_A_KEY : GOALS_B_KEY;

        int shots = getExtra(state, shotsKey, 0) + 1;
        int goals = getExtra(state, goalsKey, 0);

        if (correct) {
            goals++;
        }

        RoundState newState = withExtras(state, Map.of(
                shotsKey, shots,
                goalsKey, goals
        ));

        return checkMatchState(newState);
    }

    private RoundState checkMatchState(RoundState state) {
        int shotsA = getExtra(state, SHOTS_A_KEY, 0);
        int shotsB = getExtra(state, SHOTS_B_KEY, 0);
        int goalsA = getExtra(state, GOALS_A_KEY, 0);
        int goalsB = getExtra(state, GOALS_B_KEY, 0);
        boolean suddenDeath = getExtra(state, SUDDEN_DEATH_KEY, false);

        if (suddenDeath) {
            if (shotsA == shotsB) {
                if (goalsA != goalsB) {
                    return finishRound(state, goalsA > goalsB ? TeamSide.A : TeamSide.B);
                }
                // Continuer la mort subite
                return nextShot(state, TeamSide.A);
            }
            return nextShot(state, TeamSide.B);
        }

        // Phase normale
        if (shotsA >= INITIAL_SHOTS && shotsB >= INITIAL_SHOTS) {
            if (goalsA != goalsB) {
                return finishRound(state, goalsA > goalsB ? TeamSide.A : TeamSide.B);
            }
            // Égalité - passer en mort subite
            return withExtras(nextShot(state, TeamSide.A), Map.of(
                    SUDDEN_DEATH_KEY, true
            ));
        }

        // Vérifier victoire anticipée
        int remainingA = INITIAL_SHOTS - shotsA;
        int remainingB = INITIAL_SHOTS - shotsB;

        if (goalsA > goalsB + remainingB && shotsA == shotsB) {
            return finishRound(state, TeamSide.A);
        }
        if (goalsB > goalsA + remainingA && shotsB == shotsA) {
            return finishRound(state, TeamSide.B);
        }

        // Alterner
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
        return nextShot(state, getOppositeTeam(currentTeam));
    }

    private RoundState nextShot(RoundState state, TeamSide team) {
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

        return withExtra(newState, CURRENT_TEAM_KEY, team.name());
    }

    private RoundState finishRound(RoundState state, TeamSide winner) {
        int goalsA = getExtra(state, GOALS_A_KEY, 0);
        int goalsB = getExtra(state, GOALS_B_KEY, 0);

        return withExtras(state.completed(), Map.of(
                "winner", winner.name(),
                "finalGoalsA", goalsA,
                "finalGoalsB", goalsB
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        // Timeout = tir manqué
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
        String shotsKey = currentTeam == TeamSide.A ? SHOTS_A_KEY : SHOTS_B_KEY;
        int shots = getExtra(state, shotsKey, 0) + 1;

        RoundState newState = withExtra(state, shotsKey, shots);
        return checkMatchState(newState);
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring appliqué par le GameEngine
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }

    public int getGoalPoints() {
        return GOAL_POINTS;
    }
}
