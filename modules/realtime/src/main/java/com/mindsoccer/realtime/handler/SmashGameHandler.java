package com.mindsoccer.realtime.handler;

import com.mindsoccer.protocol.enums.RoundType;

import java.util.UUID;

/**
 * Interface pour le gestionnaire de jeu SMASH.
 * Implémentée par SmashOrchestratorService dans le module API.
 */
public interface SmashGameHandler {

    /**
     * Démarre un round SMASH.
     */
    void startSmashRound(UUID matchId, RoundType roundType);

    /**
     * Gère l'action TOP (début de question).
     */
    void handleTop(UUID matchId, UUID playerId);

    /**
     * Gère la soumission d'une question par l'attaquant.
     */
    void handleQuestionSubmit(UUID matchId, UUID playerId, String questionText);

    /**
     * Gère la validation/invalidation de la question par le défenseur.
     */
    void handleValidation(UUID matchId, UUID playerId, boolean valid, String reason);

    /**
     * Gère la soumission de la réponse par le défenseur.
     */
    void handleAnswerSubmit(UUID matchId, UUID playerId, String answer);

    /**
     * Gère la validation du résultat (correct/incorrect) par l'attaquant.
     */
    void handleResultValidation(UUID matchId, UUID playerId, boolean correct);

    /**
     * Vérifie si un match a un round SMASH actif.
     */
    boolean hasActiveSmashRound(UUID matchId);
}
