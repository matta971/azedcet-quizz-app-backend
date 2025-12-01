package com.mindsoccer.protocol.dto.websocket;

import java.util.UUID;

/**
 * Payload d'utilisation de joker CIME.
 */
public record JokerPayload(
        UUID helperId,
        String helperHandle,
        UUID targetId,
        String targetHandle,
        String jokerAnswer,
        int remainingJokers
) {}
