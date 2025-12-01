package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;

import java.util.Map;
import java.util.UUID;

/**
 * Payload de fin de rubrique.
 */
public record RoundFinishedPayload(
        UUID roundId,
        RoundType type,
        Map<TeamSide, Integer> roundScores,
        Map<TeamSide, Integer> totalScores,
        TeamSide roundWinner,
        int bonusAwarded,
        String bonusReason
) {}
