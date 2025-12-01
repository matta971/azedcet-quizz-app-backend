package com.mindsoccer.scoring.model;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

/**
 * Résultat de calcul de score pour une action de jeu.
 */
public record ScoreResult(
        int points,
        TeamSide teamSide,
        UUID playerId,
        String reason,
        boolean isBonus,
        boolean isPenalty
) {
    public static ScoreResult forTeam(int points, TeamSide side, String reason) {
        return new ScoreResult(points, side, null, reason, false, false);
    }

    public static ScoreResult forPlayer(int points, TeamSide side, UUID playerId, String reason) {
        return new ScoreResult(points, side, playerId, reason, false, false);
    }

    public static ScoreResult bonus(int points, TeamSide side, String reason) {
        return new ScoreResult(points, side, null, reason, true, false);
    }

    public static ScoreResult bonus(int points, TeamSide side, UUID playerId, String reason) {
        return new ScoreResult(points, side, playerId, reason, true, false);
    }

    public static ScoreResult penalty(int points, TeamSide side, UUID playerId, String reason) {
        return new ScoreResult(-points, side, playerId, reason, false, true);
    }

    public static ScoreResult zero() {
        return new ScoreResult(0, null, null, null, false, false);
    }
}
