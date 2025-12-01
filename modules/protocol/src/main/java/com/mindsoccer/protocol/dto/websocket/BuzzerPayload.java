package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

public record BuzzerPayload(
        UUID playerId,
        TeamSide team,
        long timestamp
) {}
