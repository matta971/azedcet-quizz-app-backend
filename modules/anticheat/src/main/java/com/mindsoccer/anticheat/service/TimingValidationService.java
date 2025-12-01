package com.mindsoccer.anticheat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service de validation des temps de réponse.
 * Détecte les réponses anormalement rapides qui pourraient indiquer une triche.
 */
@Service
public class TimingValidationService {

    private static final Logger log = LoggerFactory.getLogger(TimingValidationService.class);

    // Temps minimum humainement possible pour lire et répondre (en ms)
    private static final long MIN_RESPONSE_TIME_MS = 500;

    // Temps maximum de latence acceptable
    private static final long MAX_LATENCY_MS = 5000;

    // Seuil de suspicion pour réponses très rapides
    private static final long SUSPICIOUS_THRESHOLD_MS = 1000;

    /**
     * Valide le timing d'une réponse.
     * Retourne le niveau de confiance (0.0 = triche probable, 1.0 = légitime).
     */
    public TimingValidation validate(long questionShownTimestamp, long answerTimestamp, long clientLatency) {
        long responseTime = answerTimestamp - questionShownTimestamp;
        long adjustedResponseTime = responseTime - clientLatency;

        // Réponse impossible (temps négatif ou trop court)
        if (adjustedResponseTime < MIN_RESPONSE_TIME_MS) {
            log.warn("Suspicious response time: {}ms (adjusted: {}ms)", responseTime, adjustedResponseTime);
            return new TimingValidation(false, 0.0, "Response too fast", adjustedResponseTime);
        }

        // Latence trop élevée (connexion instable ou manipulation)
        if (clientLatency > MAX_LATENCY_MS) {
            log.warn("High latency detected: {}ms", clientLatency);
            return new TimingValidation(true, 0.7, "High latency", adjustedResponseTime);
        }

        // Réponse suspecte mais pas impossible
        if (adjustedResponseTime < SUSPICIOUS_THRESHOLD_MS) {
            log.debug("Fast response: {}ms", adjustedResponseTime);
            return new TimingValidation(true, 0.8, "Fast but plausible", adjustedResponseTime);
        }

        // Réponse normale
        return new TimingValidation(true, 1.0, "Normal", adjustedResponseTime);
    }

    /**
     * Calcule la latence estimée entre client et serveur.
     */
    public long estimateLatency(long clientTimestamp, long serverReceivedTimestamp) {
        return Math.abs(serverReceivedTimestamp - clientTimestamp) / 2;
    }

    /**
     * Vérifie si une série de réponses montre un pattern suspect.
     */
    public boolean detectBotPattern(long[] responseTimes) {
        if (responseTimes == null || responseTimes.length < 5) {
            return false;
        }

        // Les bots ont souvent des temps de réponse très réguliers
        double mean = 0;
        for (long time : responseTimes) {
            mean += time;
        }
        mean /= responseTimes.length;

        double variance = 0;
        for (long time : responseTimes) {
            variance += Math.pow(time - mean, 2);
        }
        variance /= responseTimes.length;

        double stdDev = Math.sqrt(variance);

        // Si l'écart-type est très faible (< 50ms), c'est suspect
        if (stdDev < 50 && mean < 2000) {
            log.warn("Bot pattern detected: mean={}ms, stdDev={}ms", mean, stdDev);
            return true;
        }

        return false;
    }

    /**
     * Résultat de validation de timing.
     */
    public record TimingValidation(
            boolean valid,
            double confidence,
            String reason,
            long adjustedResponseTimeMs
    ) {}
}
