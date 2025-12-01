package com.mindsoccer.scoring.service;

import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.model.ScoreResult;
import com.mindsoccer.shared.util.GameConstants;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service de calcul des scores selon les règles MINDSOCCER.
 */
@Service
public class ScoringService {

    /**
     * Calcule les points pour une bonne réponse standard.
     */
    public ScoreResult calculateCorrectAnswer(RoundType roundType, TeamSide side, UUID playerId) {
        int points = getPointsForRoundType(roundType);
        return ScoreResult.forPlayer(points, side, playerId, "Bonne réponse");
    }

    /**
     * Calcule les points pour une bonne réponse CASCADE.
     * Points progressifs: 1ère=10pts, puis 5pts de plus à chaque bonne réponse consécutive.
     */
    public ScoreResult calculateCascadeScore(int consecutiveCorrect, TeamSide side, UUID playerId) {
        int points = 10 + (consecutiveCorrect - 1) * 5;
        return ScoreResult.forPlayer(points, side, playerId, "CASCADE - réponse " + consecutiveCorrect);
    }

    /**
     * Calcule les points pour ESTOCADE.
     * 40 points par question.
     */
    public ScoreResult calculateEstocadeScore(TeamSide side, UUID playerId) {
        return ScoreResult.forPlayer(GameConstants.ESTOCADE_POINTS_PER_QUESTION, side, playerId, "ESTOCADE");
    }

    /**
     * Calcule le bonus RELAIS sans faute.
     */
    public ScoreResult calculateRelaisBonus(TeamSide side, long timeMs) {
        if (timeMs <= GameConstants.RELAIS_BONUS_TIMEOUT_MS) {
            return ScoreResult.bonus(GameConstants.RELAIS_BONUS_POINTS, side, "Bonus RELAIS - sans faute en temps");
        }
        return ScoreResult.zero();
    }

    /**
     * Calcule les points IDENTIFICATION selon l'indice.
     */
    public ScoreResult calculateIdentificationScore(int hintIndex, TeamSide side, UUID playerId) {
        int points = GameConstants.IDENTIFICATION_POINTS[Math.min(hintIndex, 3)];
        return ScoreResult.forPlayer(points, side, playerId, "IDENTIFICATION - indice " + (hintIndex + 1));
    }

    /**
     * Calcule le bonus CIME selon le nombre de bonnes réponses.
     */
    public ScoreResult calculateCimeBonus(int correctAnswers, TeamSide side) {
        if (correctAnswers > GameConstants.CIME_BONUS_THRESHOLD_HIGH) {
            return ScoreResult.bonus(GameConstants.CIME_THRESHOLD_7_BONUS, side, "Bonus CIME - plus de 7 bonnes réponses");
        } else if (correctAnswers > GameConstants.CIME_BONUS_THRESHOLD_LOW) {
            return ScoreResult.bonus(GameConstants.CIME_THRESHOLD_4_BONUS, side, "Bonus CIME - plus de 4 bonnes réponses");
        }
        return ScoreResult.zero();
    }

    /**
     * Calcule les points TIRS AU BUT pour une victoire de séance.
     */
    public ScoreResult calculateTirsAuButVictory(TeamSide side) {
        return ScoreResult.forTeam(GameConstants.TIRS_AU_BUT_VICTORY_POINTS, side, "Victoire TIRS AU BUT");
    }

    /**
     * Calcule les points JACKPOT.
     * Les points sont prélevés de la cagnotte adverse.
     */
    public ScoreResult calculateJackpotScore(int pointsFromPool, TeamSide side, UUID playerId) {
        return ScoreResult.forPlayer(pointsFromPool, side, playerId, "JACKPOT - prélèvement cagnotte");
    }

    /**
     * Calcule la pénalité SMASH pour timeout d'annonce.
     */
    public ScoreResult calculateSmashTimeoutPenalty(TeamSide side, UUID playerId) {
        return ScoreResult.penalty(GameConstants.SMASH_TIMEOUT_PENALTY_POINTS, side, playerId, "Pénalité SMASH - timeout annonce");
    }

    /**
     * Retourne les points par défaut pour un type de round.
     */
    public int getPointsForRoundType(RoundType roundType) {
        return switch (roundType) {
            case ESTOCADE -> GameConstants.ESTOCADE_POINTS_PER_QUESTION;
            case IDENTIFICATION -> GameConstants.IDENTIFICATION_POINTS[3]; // Par défaut dernier indice
            case TIRS_AU_BUT -> GameConstants.TIRS_AU_BUT_VICTORY_POINTS;
            default -> GameConstants.DEFAULT_POINTS;
        };
    }

    /**
     * Calcule le nombre de questions pour un type de round.
     */
    public int getQuestionCountForRoundType(RoundType roundType) {
        return switch (roundType) {
            case CASCADE -> GameConstants.CASCADE_QUESTION_COUNT;
            case PANIER -> GameConstants.PANIER_QUESTION_COUNT;
            case RELAIS -> GameConstants.RELAIS_QUESTION_COUNT;
            case DUEL -> GameConstants.DUEL_QUESTION_COUNT;
            case SAUT_PATRIOTIQUE -> GameConstants.SAUT_PATRIOTIQUE_QUESTION_COUNT;
            case ECHAPPEE -> GameConstants.ECHAPPEE_QUESTION_COUNT;
            case ESTOCADE -> GameConstants.ESTOCADE_QUESTION_COUNT;
            case MARATHON -> GameConstants.MARATHON_QUESTION_COUNT;
            case JACKPOT -> GameConstants.JACKPOT_QUESTION_COUNT;
            case TRANSALT -> GameConstants.TRANSALT_QUESTION_COUNT;
            case CROSS_COUNTRY -> GameConstants.CROSS_COUNTRY_QUESTION_COUNT;
            case CROSS_DICTIONARY -> GameConstants.CROSS_DICTIONARY_QUESTION_COUNT;
            case TIRS_AU_BUT -> GameConstants.TIRS_AU_BUT_QUESTION_COUNT;
            case CAPOEIRA -> GameConstants.CAPOEIRA_QUESTION_COUNT;
            case CIME -> GameConstants.CIME_QUESTION_COUNT;
            case RANDONNEE_LEXICALE -> GameConstants.RANDONNEE_LEXICALE_QUESTION_COUNT;
            case IDENTIFICATION -> GameConstants.IDENTIFICATION_HINT_COUNT;
            case SPRINT_FINAL -> GameConstants.SPRINT_FINAL_QUESTION_COUNT;
            default -> 4; // Par défaut
        };
    }
}
