package com.mindsoccer.scoring.model;

import com.mindsoccer.protocol.enums.TeamSide;

import java.time.Instant;
import java.util.UUID;

/**
 * Information sur une pénalité attribuée à un joueur.
 */
public record PenaltyInfo(
        UUID playerId,
        TeamSide teamSide,
        String reason,
        int penaltyNumber,
        boolean triggersSuspension,
        Instant timestamp
) {
    public static PenaltyInfo create(UUID playerId, TeamSide side, String reason, int penaltyNumber) {
        boolean suspension = penaltyNumber >= 5;
        return new PenaltyInfo(playerId, side, reason, penaltyNumber, suspension, Instant.now());
    }
}
