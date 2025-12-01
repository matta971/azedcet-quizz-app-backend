package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.List;
import java.util.UUID;

public record QuestionPayload(
        UUID questionId,
        String text,
        List<String> choices,
        long timeLimitMs,
        TeamSide targetTeam,
        int questionIndex
) {}
