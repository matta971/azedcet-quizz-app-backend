package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

/**
 * Payload de résultat de réponse.
 */
public record AnswerResultPayload(
        UUID questionId,
        UUID playerId,
        String playerHandle,
        TeamSide team,
        boolean correct,
        String givenAnswer,
        String expectedAnswer,
        int pointsAwarded,
        long responseTimeMs,
        int newTeamScore,
        boolean hasReplyRight
) {}
