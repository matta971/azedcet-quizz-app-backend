package com.mindsoccer.scoring.service;

import com.mindsoccer.shared.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service de validation des réponses.
 * Utilise la normalisation et la distance de Levenshtein pour une validation souple.
 */
@Service
public class AnswerValidationService {

    private static final int DEFAULT_MAX_DISTANCE = 2;
    private static final double SIMILARITY_THRESHOLD = 0.8;

    /**
     * Vérifie si une réponse est correcte.
     * Compare la réponse donnée avec la réponse attendue et les alternatives.
     */
    public ValidationResult validate(String givenAnswer, String expectedAnswer, Set<String> alternatives) {
        if (givenAnswer == null || givenAnswer.isBlank()) {
            return ValidationResult.incorrect("Réponse vide");
        }

        String normalizedGiven = StringUtils.normalizeAnswer(givenAnswer);
        String normalizedExpected = StringUtils.normalizeAnswer(expectedAnswer);

        // Vérification exacte après normalisation
        if (normalizedGiven.equals(normalizedExpected)) {
            return ValidationResult.correct();
        }

        // Vérification avec tolérance (distance de Levenshtein)
        if (isCloseEnough(normalizedGiven, normalizedExpected)) {
            return ValidationResult.correct();
        }

        // Vérification des réponses alternatives
        if (alternatives != null && !alternatives.isEmpty()) {
            for (String alt : alternatives) {
                String normalizedAlt = StringUtils.normalizeAnswer(alt);
                if (normalizedGiven.equals(normalizedAlt) || isCloseEnough(normalizedGiven, normalizedAlt)) {
                    return ValidationResult.correct();
                }
            }
        }

        return ValidationResult.incorrect("Réponse incorrecte");
    }

    /**
     * Vérifie si une réponse est correcte (version simple).
     */
    public boolean isCorrect(String givenAnswer, String expectedAnswer) {
        return validate(givenAnswer, expectedAnswer, null).isCorrect();
    }

    /**
     * Vérifie si une réponse est correcte avec alternatives.
     */
    public boolean isCorrect(String givenAnswer, String expectedAnswer, Set<String> alternatives) {
        return validate(givenAnswer, expectedAnswer, alternatives).isCorrect();
    }

    /**
     * Vérifie si deux chaînes sont suffisamment proches.
     * Utilise la distance de Levenshtein avec un seuil adaptatif.
     */
    private boolean isCloseEnough(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return true;

        // Pour les réponses courtes (< 5 caractères), on est strict
        if (maxLength < 5) {
            return s1.equals(s2);
        }

        int distance = StringUtils.levenshteinDistance(s1, s2);
        int maxAllowedDistance = calculateMaxDistance(maxLength);

        return distance <= maxAllowedDistance;
    }

    /**
     * Calcule la distance maximale autorisée en fonction de la longueur.
     */
    private int calculateMaxDistance(int length) {
        if (length <= 5) return 0;
        if (length <= 10) return 1;
        if (length <= 20) return 2;
        return Math.min(3, length / 10);
    }

    /**
     * Calcule la similarité entre deux chaînes (0.0 à 1.0).
     */
    public double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;

        String n1 = StringUtils.normalizeAnswer(s1);
        String n2 = StringUtils.normalizeAnswer(s2);

        if (n1.equals(n2)) return 1.0;
        if (n1.isEmpty() || n2.isEmpty()) return 0.0;

        int maxLength = Math.max(n1.length(), n2.length());
        int distance = StringUtils.levenshteinDistance(n1, n2);

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Résultat de validation d'une réponse.
     */
    public record ValidationResult(
            boolean isCorrect,
            String message,
            double similarity
    ) {
        public static ValidationResult correct() {
            return new ValidationResult(true, "Correct", 1.0);
        }

        public static ValidationResult incorrect(String message) {
            return new ValidationResult(false, message, 0.0);
        }
    }
}
