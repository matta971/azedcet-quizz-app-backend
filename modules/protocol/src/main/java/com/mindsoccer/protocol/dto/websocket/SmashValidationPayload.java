package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

/**
 * Payload pour la validation/invalidation d'une question SMASH.
 */
public record SmashValidationPayload(
        UUID matchId,
        boolean valid,
        String reason,
        TeamSide validatorTeam,
        int pointsAwarded,
        long timestamp
) {
    public static SmashValidationPayload valid(UUID matchId, TeamSide validator) {
        return new SmashValidationPayload(matchId, true, null, validator, 0, System.currentTimeMillis());
    }

    public static SmashValidationPayload invalid(UUID matchId, TeamSide validator, String reason, int points) {
        return new SmashValidationPayload(matchId, false, reason, validator, points, System.currentTimeMillis());
    }
}
