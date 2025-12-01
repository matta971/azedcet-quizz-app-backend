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

/**
 * Plugin CASCADE.
 *
 * Règles:
 * - 10 questions alternées entre les équipes
 * - Points progressifs: 10, 15, 20, 25... (+5 par bonne réponse consécutive)
 * - Mauvaise réponse = retour à 10 points
 * - Pas de limite de temps stricte par question
 */
@Component
public class CascadePlugin extends AbstractRulePlugin {

    private static final String CONSECUTIVE_KEY = "consecutive";
    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String LAST_CORRECT_KEY = "lastCorrect";

    public CascadePlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.CASCADE;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.CASCADE);
        return withExtras(state, Map.of(
                CONSECUTIVE_KEY, 0,
                CURRENT_TEAM_KEY, TeamSide.A,
                LAST_CORRECT_KEY, false
        ));
    }

    @Override
    public RoundState onTick(MatchContext ctx, Duration dt) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        // CASCADE n'a pas de timer strict par question
        // Le tick est utilisé pour gérer les transitions
        if (state.phase() == RoundState.Phase.TRANSITION) {
            return state.withPhase(RoundState.Phase.QUESTION_SHOWN);
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

        // Vérifier que c'est bien l'équipe qui doit répondre
        if (payload.team() != currentTeam) {
            return state; // Ignorer la réponse
        }

        int consecutive = getExtra(state, CONSECUTIVE_KEY, 0);
        boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

        int newConsecutive;
        if (correct) {
            newConsecutive = consecutive + 1;
        } else {
            newConsecutive = 0;
        }

        // Passer à la question suivante
        int nextIndex = state.questionIndex() + 1;
        TeamSide nextTeam = getOppositeTeam(currentTeam);

        if (nextIndex >= GameConstants.CASCADE_QUESTION_COUNT) {
            // Round terminé
            return withExtras(state.completed(), Map.of(
                    CONSECUTIVE_KEY, newConsecutive,
                    LAST_CORRECT_KEY, correct
            ));
        }

        RoundState newState = new RoundState(
                state.type(),
                RoundState.Phase.TRANSITION,
                nextIndex,
                null, // Question ID sera défini par le moteur
                null,
                0L,
                false,
                state.extra()
        );

        return withExtras(newState, Map.of(
                CONSECUTIVE_KEY, newConsecutive,
                CURRENT_TEAM_KEY, nextTeam,
                LAST_CORRECT_KEY, correct
        ));
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Le scoring est appliqué par le GameEngine après chaque réponse
    }

    /**
     * Calcule les points CASCADE pour le nombre de réponses consécutives.
     */
    public int calculatePoints(int consecutiveCorrect) {
        if (consecutiveCorrect <= 0) return 0;
        return 10 + (consecutiveCorrect - 1) * 5;
    }

    private String getExpectedAnswer(RoundState state) {
        // En production, la réponse attendue serait récupérée via le contexte
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }
}
