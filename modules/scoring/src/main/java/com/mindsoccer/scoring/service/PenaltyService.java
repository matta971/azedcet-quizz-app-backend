package com.mindsoccer.scoring.service;

import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.model.PenaltyInfo;
import com.mindsoccer.scoring.model.SuspensionOption;
import com.mindsoccer.shared.util.GameConstants;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service de gestion des pénalités.
 * Gère les cartons jaunes et les suspensions.
 */
@Service
public class PenaltyService {

    /**
     * Enregistre une pénalité pour un joueur.
     * Retourne les informations sur la pénalité, y compris si elle déclenche une suspension.
     */
    public PenaltyInfo recordPenalty(UUID playerId, TeamSide side, int currentPenalties, String reason) {
        int newPenaltyCount = currentPenalties + 1;
        return PenaltyInfo.create(playerId, side, reason, newPenaltyCount);
    }

    /**
     * Vérifie si le nombre de pénalités déclenche une suspension.
     */
    public boolean triggersSuspension(int penaltyCount) {
        return penaltyCount >= GameConstants.PENALTY_SUSPENSION_THRESHOLD;
    }

    /**
     * Calcule les points de suspension à récupérer selon l'option choisie.
     */
    public int getSuspensionPoints(SuspensionOption option) {
        return switch (option) {
            case IMMEDIATE_40 -> GameConstants.SUSPENSION_OPTION_1X40;
            case FOUR_QUESTIONS -> GameConstants.SUSPENSION_OPTION_4X10;
        };
    }

    /**
     * Calcule les points perdus par question de suspension manquée.
     */
    public int getSuspensionPointsPerQuestion() {
        return GameConstants.SUSPENSION_OPTION_4X10 / 4; // 10 points par question
    }

    /**
     * Vérifie si un joueur peut être libéré de suspension.
     * Le joueur est libéré quand l'équipe a récupéré les points de suspension.
     */
    public boolean canReleaseSuspension(int pointsRemaining) {
        return pointsRemaining <= 0;
    }

    /**
     * Retourne le nombre de pénalités avant suspension.
     */
    public int getPenaltiesBeforeSuspension() {
        return GameConstants.PENALTY_SUSPENSION_THRESHOLD;
    }
}
