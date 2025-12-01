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
 * Plugin CIME - Escalade vers le sommet (questions de difficulté croissante).
 *
 * Règles:
 * - 5 niveaux de difficulté (paliers)
 * - Points croissants: 5, 10, 15, 20, 25 points
 * - Chaque équipe grimpe à tour de rôle
 * - Si on échoue, on reste au palier précédent
 * - On peut "consolider" son palier pour sécuriser les points
 * - Bonus sommet: +30 points pour atteindre le niveau 5
 */
@Component
public class CimePlugin extends AbstractRulePlugin {

    private static final int MAX_LEVEL = 5;
    private static final int[] LEVEL_POINTS = {5, 10, 15, 20, 25};
    private static final int SUMMIT_BONUS = 30;
    private static final long ANSWER_TIME_MS = 15_000L;
    private static final long DECISION_TIME_MS = 10_000L;

    private static final String PHASE_KEY = "phase"; // DECISION ou CLIMB
    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String LEVEL_A_KEY = "levelA";
    private static final String LEVEL_B_KEY = "levelB";
    private static final String SECURED_A_KEY = "securedA";
    private static final String SECURED_B_KEY = "securedB";
    private static final String FINISHED_A_KEY = "finishedA";
    private static final String FINISHED_B_KEY = "finishedB";

    public CimePlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.CIME;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.CIME);

        TeamSide startingTeam = ctx.leadingTeam() == null ? TeamSide.A :
                (ctx.leadingTeam() == TeamSide.A ? TeamSide.B : TeamSide.A);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANNOUNCE), DECISION_TIME_MS), Map.of(
                PHASE_KEY, "DECISION",
                CURRENT_TEAM_KEY, startingTeam.name(),
                LEVEL_A_KEY, 0,
                LEVEL_B_KEY, 0,
                SECURED_A_KEY, 0,
                SECURED_B_KEY, 0,
                FINISHED_A_KEY, false,
                FINISHED_B_KEY, false
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

        String phase = getExtra(state, PHASE_KEY, "DECISION");
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));

        if (payload.team() != currentTeam) {
            return state;
        }

        boolean isTeamA = currentTeam == TeamSide.A;
        String levelKey = isTeamA ? LEVEL_A_KEY : LEVEL_B_KEY;
        String securedKey = isTeamA ? SECURED_A_KEY : SECURED_B_KEY;
        String finishedKey = isTeamA ? FINISHED_A_KEY : FINISHED_B_KEY;

        if ("DECISION".equals(phase)) {
            // Décision: "CLIMB" pour grimper, autre chose pour consolider
            if ("CLIMB".equalsIgnoreCase(payload.answer())) {
                int currentLevel = getExtra(state, levelKey, 0);

                if (currentLevel >= MAX_LEVEL) {
                    // Déjà au sommet, consolider automatiquement
                    return consolidate(state, currentTeam);
                }

                return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                        PHASE_KEY, "CLIMB"
                ));
            } else {
                // Consolider
                return consolidate(state, currentTeam);
            }
        }

        if ("CLIMB".equals(phase)) {
            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            int currentLevel = getExtra(state, levelKey, 0);

            if (correct) {
                currentLevel++;
                RoundState newState = withExtra(state, levelKey, currentLevel);

                if (currentLevel >= MAX_LEVEL) {
                    // Atteint le sommet!
                    return withExtras(checkRoundEnd(withExtras(newState, Map.of(
                            securedKey, currentLevel,
                            finishedKey, true
                    ))), Map.of(
                            "summit", true,
                            "summitTeam", currentTeam.name()
                    ));
                }

                // Peut continuer - retour à la phase décision
                return withExtras(withRemainingTime(newState.withPhase(RoundState.Phase.ANNOUNCE), DECISION_TIME_MS), Map.of(
                        PHASE_KEY, "DECISION"
                ));
            } else {
                // Chute! Retour au niveau sécurisé
                int securedLevel = getExtra(state, securedKey, 0);

                return checkRoundEnd(withExtras(state, Map.of(
                        levelKey, securedLevel,
                        finishedKey, true
                )));
            }
        }

        return state;
    }

    private RoundState consolidate(RoundState state, TeamSide team) {
        boolean isTeamA = team == TeamSide.A;
        String levelKey = isTeamA ? LEVEL_A_KEY : LEVEL_B_KEY;
        String securedKey = isTeamA ? SECURED_A_KEY : SECURED_B_KEY;
        String finishedKey = isTeamA ? FINISHED_A_KEY : FINISHED_B_KEY;

        int currentLevel = getExtra(state, levelKey, 0);

        return checkRoundEnd(withExtras(state, Map.of(
                securedKey, currentLevel,
                finishedKey, true
        )));
    }

    private RoundState checkRoundEnd(RoundState state) {
        boolean finishedA = getExtra(state, FINISHED_A_KEY, false);
        boolean finishedB = getExtra(state, FINISHED_B_KEY, false);

        if (finishedA && finishedB) {
            return finishRound(state);
        }

        // Passer à l'autre équipe
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
        TeamSide nextTeam = getOppositeTeam(currentTeam);

        String nextFinishedKey = nextTeam == TeamSide.A ? FINISHED_A_KEY : FINISHED_B_KEY;
        boolean nextFinished = getExtra(state, nextFinishedKey, false);

        if (nextFinished) {
            return finishRound(state);
        }

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANNOUNCE), DECISION_TIME_MS), Map.of(
                PHASE_KEY, "DECISION",
                CURRENT_TEAM_KEY, nextTeam.name()
        ));
    }

    private RoundState finishRound(RoundState state) {
        int levelA = getExtra(state, LEVEL_A_KEY, 0);
        int levelB = getExtra(state, LEVEL_B_KEY, 0);

        int pointsA = calculatePoints(levelA);
        int pointsB = calculatePoints(levelB);

        String winner = "";
        if (pointsA > pointsB) {
            winner = TeamSide.A.name();
        } else if (pointsB > pointsA) {
            winner = TeamSide.B.name();
        }

        return withExtras(state.completed(), Map.of(
                "finalLevelA", levelA,
                "finalLevelB", levelB,
                "finalPointsA", pointsA,
                "finalPointsB", pointsB,
                "roundWinner", winner
        ));
    }

    private int calculatePoints(int level) {
        int total = 0;
        for (int i = 0; i < level && i < MAX_LEVEL; i++) {
            total += LEVEL_POINTS[i];
        }
        if (level >= MAX_LEVEL) {
            total += SUMMIT_BONUS;
        }
        return total;
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "DECISION");
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));

        if ("DECISION".equals(phase)) {
            // Timeout sur la décision = consolidation automatique
            return consolidate(state, currentTeam);
        } else {
            // Timeout sur l'escalade = chute
            boolean isTeamA = currentTeam == TeamSide.A;
            String levelKey = isTeamA ? LEVEL_A_KEY : LEVEL_B_KEY;
            String securedKey = isTeamA ? SECURED_A_KEY : SECURED_B_KEY;
            String finishedKey = isTeamA ? FINISHED_A_KEY : FINISHED_B_KEY;

            int securedLevel = getExtra(state, securedKey, 0);

            return checkRoundEnd(withExtras(state, Map.of(
                    levelKey, securedLevel,
                    finishedKey, true
            )));
        }
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring appliqué par le GameEngine
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }

    public int[] getLevelPoints() {
        return LEVEL_POINTS.clone();
    }

    public int getSummitBonus() {
        return SUMMIT_BONUS;
    }
}
