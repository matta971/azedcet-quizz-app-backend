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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Plugin IDENTIFICATION - Deviner une personnalité/lieu/chose avec indices progressifs.
 *
 * Règles:
 * - Une chose à identifier (personnalité, lieu, objet, etc.)
 * - 5 indices donnés progressivement
 * - Points dégressifs: 25, 20, 15, 10, 5 points selon l'indice
 * - Mode buzzer: le premier à buzzer peut tenter
 * - Si mauvaise réponse, le joueur est éliminé pour cette question
 * - L'équipe adverse peut tenter après un échec
 */
@Component
public class IdentificationPlugin extends AbstractRulePlugin {

    private static final int TOTAL_CLUES = 5;
    private static final int[] CLUE_POINTS = {25, 20, 15, 10, 5};
    private static final long ANSWER_TIME_MS = 10_000L;
    private static final long CLUE_DISPLAY_MS = 5_000L;

    private static final String PHASE_KEY = "phase"; // CLUE_DISPLAY, BUZZER, ANSWER
    private static final String CURRENT_CLUE_KEY = "currentClue";
    private static final String BUZZER_WINNER_KEY = "buzzerWinner";
    private static final String ELIMINATED_PLAYERS_KEY = "eliminatedPlayers";
    private static final String ANSWERED_KEY = "answered";

    public IdentificationPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.IDENTIFICATION;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.IDENTIFICATION);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANNOUNCE), CLUE_DISPLAY_MS), Map.of(
                PHASE_KEY, "CLUE_DISPLAY",
                CURRENT_CLUE_KEY, 1,
                BUZZER_WINNER_KEY, "",
                ELIMINATED_PLAYERS_KEY, new ArrayList<String>(),
                ANSWERED_KEY, false
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

        String phase = getExtra(state, PHASE_KEY, "CLUE_DISPLAY");
        String playerIdStr = payload.playerId().toString();

        // Vérifier si le joueur est éliminé
        @SuppressWarnings("unchecked")
        List<String> eliminated = new ArrayList<>((List<String>) state.extra().getOrDefault(ELIMINATED_PLAYERS_KEY, new ArrayList<>()));
        if (eliminated.contains(playerIdStr)) {
            return state;
        }

        if ("BUZZER".equals(phase)) {
            // Le joueur buzze
            return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANSWER_WINDOW), ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "ANSWER",
                    BUZZER_WINNER_KEY, playerIdStr
            ));
        }

        if ("ANSWER".equals(phase)) {
            String buzzerWinner = getExtra(state, BUZZER_WINNER_KEY, "");
            if (!playerIdStr.equals(buzzerWinner)) {
                return state;
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            if (correct) {
                int currentClue = getExtra(state, CURRENT_CLUE_KEY, 1);
                int points = CLUE_POINTS[currentClue - 1];

                return withExtras(state.completed(), Map.of(
                        ANSWERED_KEY, true,
                        "winnerPlayerId", playerIdStr,
                        "winnerTeam", payload.team().name(),
                        "pointsAwarded", points,
                        "clueUsed", currentClue
                ));
            } else {
                // Éliminer le joueur
                eliminated.add(playerIdStr);

                return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANSWER_WINDOW), ANSWER_TIME_MS), Map.of(
                        PHASE_KEY, "BUZZER",
                        BUZZER_WINNER_KEY, "",
                        ELIMINATED_PLAYERS_KEY, eliminated
                ));
            }
        }

        return state;
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "CLUE_DISPLAY");
        int currentClue = getExtra(state, CURRENT_CLUE_KEY, 1);

        if ("CLUE_DISPLAY".equals(phase)) {
            // Passer au mode buzzer après affichage de l'indice
            return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANSWER_WINDOW), ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "BUZZER"
            ));
        } else if ("BUZZER".equals(phase)) {
            // Personne n'a buzzé, passer à l'indice suivant
            if (currentClue < TOTAL_CLUES) {
                return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANNOUNCE), CLUE_DISPLAY_MS), Map.of(
                        PHASE_KEY, "CLUE_DISPLAY",
                        CURRENT_CLUE_KEY, currentClue + 1
                ));
            } else {
                // Plus d'indices, personne n'a trouvé
                return withExtras(state.completed(), Map.of(
                        ANSWERED_KEY, false,
                        "noOneFound", true
                ));
            }
        } else if ("ANSWER".equals(phase)) {
            // Timeout sur la réponse = mauvaise réponse
            String buzzerWinner = getExtra(state, BUZZER_WINNER_KEY, "");

            @SuppressWarnings("unchecked")
            List<String> eliminated = new ArrayList<>((List<String>) state.extra().getOrDefault(ELIMINATED_PLAYERS_KEY, new ArrayList<>()));
            if (!buzzerWinner.isEmpty()) {
                eliminated.add(buzzerWinner);
            }

            return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANSWER_WINDOW), ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "BUZZER",
                    BUZZER_WINNER_KEY, "",
                    ELIMINATED_PLAYERS_KEY, eliminated
            ));
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

    public int[] getCluePoints() {
        return CLUE_POINTS.clone();
    }

    public int getTotalClues() {
        return TOTAL_CLUES;
    }
}
