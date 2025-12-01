package com.mindsoccer.protocol.dto.websocket;

import java.util.UUID;

public record MatchStartedPayload(
        UUID matchId,
        long timestamp
) {}
