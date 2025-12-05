package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

/**
 * Payload pour une question SMASH soumise par l'attaquant.
 */
public record SmashQuestionPayload(
        UUID matchId,
        String questionText,
        TeamSide attackerTeam,
        TeamSide defenderTeam,
        long timeoutMs,
        long timestamp
) {
    public static SmashQuestionPayload of(UUID matchId, String questionText, TeamSide attacker, long timeoutMs) {
        return new SmashQuestionPayload(
                matchId,
                questionText,
                attacker,
                attacker == TeamSide.A ? TeamSide.B : TeamSide.A,
                timeoutMs,
                System.currentTimeMillis()
        );
    }
}
