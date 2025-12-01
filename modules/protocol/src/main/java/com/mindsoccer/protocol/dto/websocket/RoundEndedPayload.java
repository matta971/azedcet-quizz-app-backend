package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.RoundType;

public record RoundEndedPayload(
        RoundType roundType,
        int teamAPoints,
        int teamBPoints
) {}
