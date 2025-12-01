package com.mindsoccer.protocol.dto.websocket;

/**
 * Payload de timer.
 */
public record TimerPayload(
        long remainingMs,
        long totalMs,
        String context,
        boolean warning
) {
    public static TimerPayload of(long remainingMs, long totalMs, String context) {
        return new TimerPayload(remainingMs, totalMs, context, remainingMs < 5000);
    }
}
