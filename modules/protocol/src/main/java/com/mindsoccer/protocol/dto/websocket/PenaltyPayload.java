package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

/**
 * Payload de pénalité.
 */
public record PenaltyPayload(
        UUID playerId,
        String playerHandle,
        TeamSide team,
        String reason,
        int penaltyCount,
        boolean suspended,
        int suspensionPointsRemaining,
        boolean bonusQuestionForOpponent
) {}
