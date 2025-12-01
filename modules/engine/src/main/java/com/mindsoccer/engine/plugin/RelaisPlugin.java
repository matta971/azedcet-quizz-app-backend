package com.mindsoccer.engine.plugin;

import com.mindsoccer.engine.AnswerPayload;
import com.mindsoccer.engine.MatchContext;
import com.mindsoccer.engine.RoundState;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.service.AnswerValidationService;
import com.mindsoccer.scoring.service.ScoringService;
import com.mindsoccer.shared.util.GameConstants;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Plugin RELAIS.
 *
 * Règles:
 * - 4 questions par équipe en relais (joueurs tournent)
 * - Chaque joueur doit répondre
 * - Bonus +20 points si l'équipe répond sans faute en moins de 40 secondes
 * - 10 points par bonne réponse standard
 */
@Component
public class RelaisPlugin extends AbstractRulePlugin {

    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String CURRENT_PLAYER_INDEX_KEY = "currentPlayerIndex";
    private static final String TEAM_A_CORRECT_KEY = "teamACorrect";
    private static final String TEAM_B_CORRECT_KEY = "teamBCorrect";
    private static final String TEAM_A_START_TIME_KEY = "teamAStartTime";
    private static final String TEAM_B_START_TIME_KEY = "teamBStartTime";
    private static final String TEAM_A_TOTAL_TIME_KEY = "teamATotalTime";
    private static final String TEAM_B_TOTAL_TIME_KEY = "teamBTotalTime";
    private static final String QUESTIONS_ANSWERED_KEY = "questionsAnswered";

    public RelaisPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.RELAIS;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.RELAIS);
        long now = System.currentTimeMillis();

        return withExtras(state, Map.of(
                CURRENT_TEAM_KEY, TeamSide.A,
                CURRENT_PLAYER_INDEX_KEY, 0,
                TEAM_A_CORRECT_KEY, 0,
                TEAM_B_CORRECT_KEY, 0,
                TEAM_A_START_TIME_KEY, now,
                TEAM_B_START_TIME_KEY, 0L,
                TEAM_A_TOTAL_TIME_KEY, 0L,
                TEAM_B_TOTAL_TIME_KEY, 0L,
                QUESTIONS_ANSWERED_KEY, 0
        )).withPhase(RoundState.Phase.QUESTION_SHOWN);
    }

    @Override
    public RoundState onTick(MatchContext ctx, Duration dt) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        return state;
    }

    @Override
    public RoundState onAnswer(MatchContext ctx, AnswerPayload payload) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        TeamSide currentTeam = getExtra(state, CURRENT_TEAM_KEY, TeamSide.A);
        int playerIndex = getExtra(state, CURRENT_PLAYER_INDEX_KEY, 0);

        // Vérifier que c'est le bon joueur de la bonne équipe
        if (payload.team() != currentTeam) {
            return state;
        }

        UUID expectedPlayerId = getExpectedPlayer(ctx, currentTeam, playerIndex);
        if (expectedPlayerId != null && !expectedPlayerId.equals(payload.playerId())) {
            return state; // Pas le bon joueur
        }

        boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

        // Mettre à jour les compteurs
        String correctKey = currentTeam == TeamSide.A ? TEAM_A_CORRECT_KEY : TEAM_B_CORRECT_KEY;
        int currentCorrect = getExtra(state, correctKey, 0);
        if (correct) {
            currentCorrect++;
        }

        int totalAnswered = getExtra(state, QUESTIONS_ANSWERED_KEY, 0) + 1;
        int nextPlayerIndex = playerIndex + 1;

        // Vérifier si l'équipe a fini ses questions
        boolean teamFinished = nextPlayerIndex >= GameConstants.RELAIS_QUESTION_COUNT;

        RoundState newState;
        if (teamFinished) {
            // Enregistrer le temps total pour cette équipe
            long startTime = getExtra(state, currentTeam == TeamSide.A ? TEAM_A_START_TIME_KEY : TEAM_B_START_TIME_KEY, 0L);
            long totalTime = System.currentTimeMillis() - startTime;
            String totalTimeKey = currentTeam == TeamSide.A ? TEAM_A_TOTAL_TIME_KEY : TEAM_B_TOTAL_TIME_KEY;

            if (currentTeam == TeamSide.A) {
                // Passer à l'équipe B
                newState = withExtras(state, Map.of(
                        CURRENT_TEAM_KEY, TeamSide.B,
                        CURRENT_PLAYER_INDEX_KEY, 0,
                        correctKey, currentCorrect,
                        totalTimeKey, totalTime,
                        TEAM_B_START_TIME_KEY, System.currentTimeMillis(),
                        QUESTIONS_ANSWERED_KEY, totalAnswered
                )).withPhase(RoundState.Phase.TRANSITION);
            } else {
                // Round terminé
                newState = withExtras(state.completed(), Map.of(
                        correctKey, currentCorrect,
                        totalTimeKey, totalTime,
                        QUESTIONS_ANSWERED_KEY, totalAnswered
                ));
            }
        } else {
            // Prochain joueur de la même équipe
            newState = withExtras(state, Map.of(
                    CURRENT_PLAYER_INDEX_KEY, nextPlayerIndex,
                    correctKey, currentCorrect,
                    QUESTIONS_ANSWERED_KEY, totalAnswered
            )).withPhase(RoundState.Phase.TRANSITION);
        }

        return newState;
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Le scoring est calculé à la fin du round
        // Bonus de 20 points si sans faute en moins de 40 secondes
    }

    /**
     * Vérifie si une équipe a droit au bonus RELAIS.
     */
    public boolean hasBonus(RoundState state, TeamSide team) {
        String correctKey = team == TeamSide.A ? TEAM_A_CORRECT_KEY : TEAM_B_CORRECT_KEY;
        String timeKey = team == TeamSide.A ? TEAM_A_TOTAL_TIME_KEY : TEAM_B_TOTAL_TIME_KEY;

        int correct = getExtra(state, correctKey, 0);
        long totalTime = getExtra(state, timeKey, 0L);

        return correct == GameConstants.RELAIS_QUESTION_COUNT &&
                totalTime <= GameConstants.RELAIS_BONUS_TIMEOUT_MS;
    }

    private UUID getExpectedPlayer(MatchContext ctx, TeamSide team, int index) {
        var players = ctx.players().get(team);
        if (players != null && index < players.size()) {
            return players.get(index);
        }
        return null;
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }
}
