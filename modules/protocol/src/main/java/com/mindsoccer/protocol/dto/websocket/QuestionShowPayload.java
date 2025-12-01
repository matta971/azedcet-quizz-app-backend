package com.mindsoccer.protocol.dto.websocket;

import java.util.List;
import java.util.UUID;

/**
 * Payload d'affichage d'une question.
 */
public record QuestionShowPayload(
        UUID questionId,
        int questionIndex,
        int totalQuestions,
        String statement,
        List<String> choices,
        String hint,
        int pointsValue,
        String mediaUrl,
        String mediaType,
        long timeoutMs,
        UUID targetPlayerId,
        String targetPlayerHandle
) {}
