package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

/**
 * Payload pour une réponse SMASH soumise par le défenseur.
 */
public record SmashAnswerPayload(
        UUID matchId,
        String answerText,
        TeamSide defenderTeam,
        long timestamp
) {
    public static SmashAnswerPayload of(UUID matchId, String answer, TeamSide defender) {
        return new SmashAnswerPayload(matchId, answer, defender, System.currentTimeMillis());
    }
}
