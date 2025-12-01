package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;

import java.util.Map;
import java.util.UUID;

/**
 * Payload de mise à jour d'état (tick régulier).
 */
public record StateUpdatePayload(
        MatchStatus status,
        Map<TeamSide, Integer> scores,
        TeamSide leadingTeam,
        RoundType currentRoundType,
        String phase,
        int questionIndex,
        UUID activePlayerId,
        long remainingTimeMs
) {}
