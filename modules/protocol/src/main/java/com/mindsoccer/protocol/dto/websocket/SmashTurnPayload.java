package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

/**
 * Payload pour le début d'un tour SMASH.
 */
public record SmashTurnPayload(
        UUID matchId,
        int turnNumber,
        TeamSide attackerTeam,
        TeamSide defenderTeam,
        RoundType roundType,
        boolean hasConcertation,
        long timestamp
) {
    public static SmashTurnPayload of(UUID matchId, int turnNumber, TeamSide attacker, RoundType roundType) {
        return new SmashTurnPayload(
                matchId,
                turnNumber,
                attacker,
                attacker == TeamSide.A ? TeamSide.B : TeamSide.A,
                roundType,
                roundType == RoundType.SMASH_A,
                System.currentTimeMillis()
        );
    }
}
