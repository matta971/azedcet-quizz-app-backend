package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

public record PlayerJoinedPayload(
        UUID playerId,
        String handle,
        TeamSide team
) {}
