package com.mindsoccer.protocol.dto.websocket;

public record TimerTickPayload(
        long remainingMs,
        long serverTimestamp
) {}
