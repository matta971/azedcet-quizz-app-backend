package com.mindsoccer.protocol.dto.websocket;

import java.util.UUID;

public record MatchEndedPayload(
        UUID matchId,
        UUID winnerId,
        int scoreTeamA,
        int scoreTeamB
) {}
