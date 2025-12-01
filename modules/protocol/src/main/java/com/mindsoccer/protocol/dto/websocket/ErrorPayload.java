package com.mindsoccer.protocol.dto.websocket;

public record ErrorPayload(
        String code,
        String message
) {}
