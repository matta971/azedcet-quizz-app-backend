package com.mindsoccer.protocol.dto.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * Message WebSocket générique.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WsMessage<T>(
        WsEventType type,
        UUID matchId,
        T payload,
        long serverTimestamp,
        String correlationId
) {
    public static <T> WsMessage<T> of(WsEventType type, T payload) {
        return new WsMessage<>(type, null, payload, Instant.now().toEpochMilli(), null);
    }

    public static <T> WsMessage<T> of(WsEventType type, UUID matchId, T payload) {
        return new WsMessage<>(type, matchId, payload, Instant.now().toEpochMilli(), null);
    }

    public static <T> WsMessage<T> of(WsEventType type, UUID matchId, T payload, String correlationId) {
        return new WsMessage<>(type, matchId, payload, Instant.now().toEpochMilli(), correlationId);
    }

    public WsEventType type() {
        return type;
    }
}
