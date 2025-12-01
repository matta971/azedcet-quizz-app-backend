package com.mindsoccer.scoring.service;

import org.springframework.stereotype.Service;

/**
 * Service de calcul du classement ELO.
 * Gère les gains et pertes de rating après un match.
 */
@Service
public class RatingService {

    private static final int K_FACTOR = 32;
    private static final int BASE_RATING = 1500;
    private static final int MIN_RATING = 100;
    private static final int MAX_RATING = 3000;

    /**
     * Calcule le nouveau rating après un match.
     *
     * @param playerRating Rating actuel du joueur
     * @param opponentRating Rating moyen de l'équipe adverse
     * @param result 1.0 pour victoire, 0.5 pour nul, 0.0 pour défaite
     * @return Nouveau rating du joueur
     */
    public int calculateNewRating(int playerRating, int opponentRating, double result) {
        double expectedScore = calculateExpectedScore(playerRating, opponentRating);
        int change = (int) Math.round(K_FACTOR * (result - expectedScore));
        int newRating = playerRating + change;

        return clamp(newRating, MIN_RATING, MAX_RATING);
    }

    /**
     * Calcule le score attendu selon la formule ELO.
     */
    public double calculateExpectedScore(int playerRating, int opponentRating) {
        return 1.0 / (1.0 + Math.pow(10, (opponentRating - playerRating) / 400.0));
    }

    /**
     * Calcule le changement de rating.
     */
    public int calculateRatingChange(int playerRating, int opponentRating, double result) {
        double expectedScore = calculateExpectedScore(playerRating, opponentRating);
        return (int) Math.round(K_FACTOR * (result - expectedScore));
    }

    /**
     * Calcule le rating moyen d'une équipe.
     */
    public int calculateTeamAverageRating(int[] playerRatings) {
        if (playerRatings == null || playerRatings.length == 0) {
            return BASE_RATING;
        }
        int sum = 0;
        for (int rating : playerRatings) {
            sum += rating;
        }
        return sum / playerRatings.length;
    }

    /**
     * Retourne le rating de base pour un nouveau joueur.
     */
    public int getBaseRating() {
        return BASE_RATING;
    }

    /**
     * Retourne le K-factor utilisé pour les calculs.
     */
    public int getKFactor() {
        return K_FACTOR;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Résultat de calcul de rating pour une équipe.
     */
    public record TeamRatingResult(
            int[] oldRatings,
            int[] newRatings,
            int[] changes,
            int averageChange
    ) {
        public static TeamRatingResult calculate(int[] playerRatings, int opponentAvgRating, double result, RatingService service) {
            int[] newRatings = new int[playerRatings.length];
            int[] changes = new int[playerRatings.length];
            int totalChange = 0;

            for (int i = 0; i < playerRatings.length; i++) {
                newRatings[i] = service.calculateNewRating(playerRatings[i], opponentAvgRating, result);
                changes[i] = newRatings[i] - playerRatings[i];
                totalChange += changes[i];
            }

            int avgChange = playerRatings.length > 0 ? totalChange / playerRatings.length : 0;
            return new TeamRatingResult(playerRatings.clone(), newRatings, changes, avgChange);
        }
    }
}
