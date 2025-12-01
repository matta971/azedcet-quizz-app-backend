package com.mindsoccer.shared.idempotency;

import java.util.UUID;

/**
 * Utilitaire pour générer des clés d'idempotence.
 */
public final class IdempotencyKeyGenerator {

    private IdempotencyKeyGenerator() {
        // Utility class
    }

    /**
     * Génère une clé d'idempotence pour une réponse.
     */
    public static String forAnswer(UUID matchId, UUID roundId, UUID questionId, UUID playerId) {
        return String.format("answer:%s:%s:%s:%s", matchId, roundId, questionId, playerId);
    }

    /**
     * Génère une clé d'idempotence pour une pénalité.
     */
    public static String forPenalty(UUID matchId, UUID playerId, String reason, long timestamp) {
        return String.format("penalty:%s:%s:%s:%d", matchId, playerId, reason, timestamp);
    }

    /**
     * Génère une clé d'idempotence pour une enchère (Jackpot).
     */
    public static String forBid(UUID matchId, UUID roundId, UUID teamId, int bidAmount) {
        return String.format("bid:%s:%s:%s:%d", matchId, roundId, teamId, bidAmount);
    }

    /**
     * Génère une clé d'idempotence pour l'utilisation d'un joker (Cime).
     */
    public static String forJoker(UUID matchId, UUID roundId, UUID helperId, UUID targetId) {
        return String.format("joker:%s:%s:%s:%s", matchId, roundId, helperId, targetId);
    }

    /**
     * Génère une clé d'idempotence pour le choix d'un thème.
     */
    public static String forThemeChoice(UUID matchId, UUID roundId, UUID teamId) {
        return String.format("theme:%s:%s:%s", matchId, roundId, teamId);
    }

    /**
     * Génère une clé d'idempotence pour une action générique.
     */
    public static String forAction(String action, UUID matchId, UUID playerId) {
        return String.format("%s:%s:%s:%d", action, matchId, playerId, System.currentTimeMillis());
    }

    /**
     * Génère une clé d'idempotence unique.
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
