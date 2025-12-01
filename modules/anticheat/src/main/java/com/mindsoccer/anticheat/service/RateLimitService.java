package com.mindsoccer.anticheat.service;

import com.mindsoccer.shared.exception.RateLimitException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de rate limiting pour les actions de jeu.
 * Empêche le spam de réponses et les abus.
 */
@Service
public class RateLimitService {

    // Limites par type d'action
    private static final int MAX_ANSWERS_PER_SECOND = 2;
    private static final int MAX_BUZZERS_PER_SECOND = 3;
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    // Stockage en mémoire (Redis en production)
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    /**
     * Vérifie si une réponse peut être soumise.
     */
    public void checkAnswer(UUID playerId, UUID matchId) {
        String key = "answer:" + playerId + ":" + matchId;
        RateLimitBucket bucket = buckets.computeIfAbsent(key,
                k -> new RateLimitBucket(MAX_ANSWERS_PER_SECOND, Duration.ofSeconds(1)));

        if (!bucket.tryConsume()) {
            throw RateLimitException.answerRateExceeded();
        }
    }

    /**
     * Vérifie si un buzzer peut être envoyé.
     */
    public void checkBuzzer(UUID playerId, UUID matchId) {
        String key = "buzzer:" + playerId + ":" + matchId;
        RateLimitBucket bucket = buckets.computeIfAbsent(key,
                k -> new RateLimitBucket(MAX_BUZZERS_PER_SECOND, Duration.ofSeconds(1)));

        if (!bucket.tryConsume()) {
            throw RateLimitException.rateLimitExceeded();
        }
    }

    /**
     * Vérifie les requêtes générales d'un utilisateur.
     */
    public void checkGeneralRequest(UUID userId) {
        String key = "general:" + userId;
        RateLimitBucket bucket = buckets.computeIfAbsent(key,
                k -> new RateLimitBucket(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));

        if (!bucket.tryConsume()) {
            throw RateLimitException.rateLimitExceeded();
        }
    }

    /**
     * Réinitialise les limites pour un joueur (nouveau round par exemple).
     */
    public void resetForPlayer(UUID playerId, UUID matchId) {
        buckets.keySet().removeIf(k -> k.contains(playerId.toString()) && k.contains(matchId.toString()));
    }

    /**
     * Nettoie les buckets expirés.
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(e -> e.getValue().isExpired(now));
    }

    /**
     * Bucket simple de rate limiting.
     */
    private static class RateLimitBucket {
        private final int maxTokens;
        private final long refillIntervalMs;
        private int tokens;
        private long lastRefillTime;

        RateLimitBucket(int maxTokens, Duration refillInterval) {
            this.maxTokens = maxTokens;
            this.refillIntervalMs = refillInterval.toMillis();
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            if (elapsed >= refillIntervalMs) {
                tokens = maxTokens;
                lastRefillTime = now;
            }
        }

        boolean isExpired(long now) {
            return now - lastRefillTime > refillIntervalMs * 10;
        }
    }
}
