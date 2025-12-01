package com.mindsoccer.anticheat.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de vérification d'intégrité des réponses.
 * Empêche la modification des réponses après soumission.
 */
@Service
public class AnswerIntegrityService {

    // Cache des réponses pour détecter les modifications
    private final Map<String, AnswerRecord> answerCache = new ConcurrentHashMap<>();

    /**
     * Génère un hash d'intégrité pour une réponse.
     */
    public String generateIntegrityHash(UUID matchId, UUID questionId, UUID playerId, String answer, long timestamp) {
        String data = String.format("%s:%s:%s:%s:%d",
                matchId, questionId, playerId, answer.toLowerCase().trim(), timestamp);
        return DigestUtils.sha256Hex(data);
    }

    /**
     * Enregistre une réponse et retourne son hash d'intégrité.
     */
    public String recordAnswer(UUID matchId, UUID questionId, UUID playerId, String answer, long timestamp) {
        String hash = generateIntegrityHash(matchId, questionId, playerId, answer, timestamp);
        String key = buildKey(matchId, questionId, playerId);

        AnswerRecord record = new AnswerRecord(answer, timestamp, hash);
        AnswerRecord existing = answerCache.putIfAbsent(key, record);

        if (existing != null) {
            // Réponse déjà enregistrée - retourner le hash existant
            return existing.hash();
        }

        return hash;
    }

    /**
     * Vérifie si une réponse a déjà été soumise.
     */
    public boolean hasAlreadyAnswered(UUID matchId, UUID questionId, UUID playerId) {
        String key = buildKey(matchId, questionId, playerId);
        return answerCache.containsKey(key);
    }

    /**
     * Vérifie l'intégrité d'une réponse.
     */
    public boolean verifyIntegrity(UUID matchId, UUID questionId, UUID playerId, String answer, long timestamp, String providedHash) {
        String expectedHash = generateIntegrityHash(matchId, questionId, playerId, answer, timestamp);
        return expectedHash.equals(providedHash);
    }

    /**
     * Récupère l'enregistrement d'une réponse.
     */
    public AnswerRecord getAnswer(UUID matchId, UUID questionId, UUID playerId) {
        String key = buildKey(matchId, questionId, playerId);
        return answerCache.get(key);
    }

    /**
     * Nettoie les réponses pour un match terminé.
     */
    public void cleanupMatch(UUID matchId) {
        String matchPrefix = matchId.toString();
        answerCache.keySet().removeIf(k -> k.startsWith(matchPrefix));
    }

    private String buildKey(UUID matchId, UUID questionId, UUID playerId) {
        return matchId + ":" + questionId + ":" + playerId;
    }

    /**
     * Enregistrement d'une réponse.
     */
    public record AnswerRecord(
            String answer,
            long timestamp,
            String hash
    ) {}
}
