package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.RoundType;

import java.util.UUID;

/**
 * Payload de début de rubrique.
 */
public record RoundStartedPayload(
        UUID roundId,
        RoundType type,
        int roundIndex,
        String instruction,
        int totalQuestions,
        long durationMs
) {}
