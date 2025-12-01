package com.mindsoccer.engine.plugin;

import com.mindsoccer.engine.AnswerPayload;
import com.mindsoccer.engine.MatchContext;
import com.mindsoccer.engine.RoundState;
import com.mindsoccer.engine.RulePlugin;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.service.AnswerValidationService;
import com.mindsoccer.scoring.service.ScoringService;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Classe abstraite de base pour les plugins de règles.
 * Fournit des méthodes utilitaires communes.
 */
public abstract class AbstractRulePlugin implements RulePlugin {

    protected final ScoringService scoringService;
    protected final AnswerValidationService validationService;

    protected AbstractRulePlugin(ScoringService scoringService, AnswerValidationService validationService) {
        this.scoringService = scoringService;
        this.validationService = validationService;
    }

    /**
     * Crée un nouvel état avec les extras mis à jour.
     */
    protected RoundState withExtra(RoundState state, String key, Object value) {
        Map<String, Object> newExtra = new HashMap<>(state.extra());
        newExtra.put(key, value);
        return new RoundState(
                state.type(),
                state.phase(),
                state.questionIndex(),
                state.currentQuestionId(),
                state.activePlayerId(),
                state.remainingTimeMs(),
                state.finished(),
                newExtra
        );
    }

    /**
     * Crée un nouvel état avec plusieurs extras mis à jour.
     */
    protected RoundState withExtras(RoundState state, Map<String, Object> extras) {
        Map<String, Object> newExtra = new HashMap<>(state.extra());
        newExtra.putAll(extras);
        return new RoundState(
                state.type(),
                state.phase(),
                state.questionIndex(),
                state.currentQuestionId(),
                state.activePlayerId(),
                state.remainingTimeMs(),
                state.finished(),
                newExtra
        );
    }

    /**
     * Crée un nouvel état avec une nouvelle question.
     */
    protected RoundState withQuestion(RoundState state, int index, UUID questionId, long timeMs) {
        return new RoundState(
                state.type(),
                RoundState.Phase.QUESTION_SHOWN,
                index,
                questionId,
                state.activePlayerId(),
                timeMs,
                false,
                state.extra()
        );
    }

    /**
     * Crée un nouvel état avec un joueur actif.
     */
    protected RoundState withActivePlayer(RoundState state, UUID playerId) {
        return new RoundState(
                state.type(),
                state.phase(),
                state.questionIndex(),
                state.currentQuestionId(),
                playerId,
                state.remainingTimeMs(),
                state.finished(),
                state.extra()
        );
    }

    /**
     * Crée un nouvel état avec le temps restant mis à jour.
     */
    protected RoundState withRemainingTime(RoundState state, long timeMs) {
        return new RoundState(
                state.type(),
                state.phase(),
                state.questionIndex(),
                state.currentQuestionId(),
                state.activePlayerId(),
                timeMs,
                state.finished(),
                state.extra()
        );
    }

    /**
     * Récupère une valeur extra avec valeur par défaut.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getExtra(RoundState state, String key, T defaultValue) {
        Object value = state.extra().get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Valide une réponse.
     */
    protected boolean validateAnswer(String givenAnswer, String expectedAnswer, Set<String> alternatives) {
        return validationService.isCorrect(givenAnswer, expectedAnswer, alternatives);
    }

    /**
     * Obtient l'équipe adverse.
     */
    protected TeamSide getOppositeTeam(TeamSide side) {
        return side == TeamSide.A ? TeamSide.B : TeamSide.A;
    }

    /**
     * Vérifie si le round est terminé.
     */
    protected boolean isRoundFinished(RoundState state, int maxQuestions) {
        return state.questionIndex() >= maxQuestions;
    }

    /**
     * Décrémente le temps restant.
     */
    protected RoundState decrementTime(RoundState state, Duration dt) {
        long newTime = Math.max(0, state.remainingTimeMs() - dt.toMillis());
        return withRemainingTime(state, newTime);
    }

    /**
     * Vérifie si le temps est écoulé.
     */
    protected boolean isTimeUp(RoundState state) {
        return state.remainingTimeMs() <= 0;
    }
}
