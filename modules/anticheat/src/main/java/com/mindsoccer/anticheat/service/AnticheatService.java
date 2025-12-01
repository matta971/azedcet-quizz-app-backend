package com.mindsoccer.anticheat.service;

import com.mindsoccer.shared.exception.RoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service principal anti-triche.
 * Coordonne les différentes vérifications.
 */
@Service
public class AnticheatService {

    private static final Logger log = LoggerFactory.getLogger(AnticheatService.class);

    private final RateLimitService rateLimitService;
    private final TimingValidationService timingValidationService;
    private final AnswerIntegrityService answerIntegrityService;

    public AnticheatService(RateLimitService rateLimitService,
                            TimingValidationService timingValidationService,
                            AnswerIntegrityService answerIntegrityService) {
        this.rateLimitService = rateLimitService;
        this.timingValidationService = timingValidationService;
        this.answerIntegrityService = answerIntegrityService;
    }

    /**
     * Valide une soumission de réponse.
     * Lance une exception si la validation échoue.
     */
    public ValidationResult validateAnswerSubmission(
            UUID matchId,
            UUID questionId,
            UUID playerId,
            String answer,
            long questionShownTimestamp,
            long answerTimestamp,
            long clientTimestamp
    ) {
        // 1. Vérifier le rate limiting
        rateLimitService.checkAnswer(playerId, matchId);

        // 2. Vérifier si déjà répondu
        if (answerIntegrityService.hasAlreadyAnswered(matchId, questionId, playerId)) {
            log.warn("Player {} attempted to answer question {} twice", playerId, questionId);
            throw RoundException.alreadyAnswered();
        }

        // 3. Valider le timing
        long latency = timingValidationService.estimateLatency(clientTimestamp, answerTimestamp);
        var timingResult = timingValidationService.validate(questionShownTimestamp, answerTimestamp, latency);

        if (!timingResult.valid()) {
            log.warn("Invalid timing for player {}: {}", playerId, timingResult.reason());
            throw RoundException.invalidTiming();
        }

        // 4. Enregistrer la réponse pour l'intégrité
        String hash = answerIntegrityService.recordAnswer(matchId, questionId, playerId, answer, answerTimestamp);

        return new ValidationResult(
                true,
                timingResult.confidence(),
                timingResult.adjustedResponseTimeMs(),
                hash
        );
    }

    /**
     * Valide un buzzer.
     */
    public void validateBuzzer(UUID matchId, UUID playerId) {
        rateLimitService.checkBuzzer(playerId, matchId);
    }

    /**
     * Nettoie les données pour un match terminé.
     */
    public void cleanupMatch(UUID matchId) {
        answerIntegrityService.cleanupMatch(matchId);
        log.debug("Cleaned up anticheat data for match {}", matchId);
    }

    /**
     * Réinitialise les limites pour un nouveau round.
     */
    public void resetForNewRound(UUID matchId, UUID playerId) {
        rateLimitService.resetForPlayer(playerId, matchId);
    }

    /**
     * Résultat de validation.
     */
    public record ValidationResult(
            boolean valid,
            double confidence,
            long adjustedResponseTimeMs,
            String integrityHash
    ) {}
}
