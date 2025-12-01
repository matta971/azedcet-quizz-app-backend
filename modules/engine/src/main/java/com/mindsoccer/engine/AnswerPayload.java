package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.TeamSide;
import java.time.Instant;
import java.util.UUID;

public record AnswerPayload(
        UUID matchId,
        UUID roundId,
        UUID questionId,
        UUID playerId,
        TeamSide team,
        String answer,
        Instant submittedAt,
        long clientTimestampMs,
        String idempotencyKey
) {

    public long latencyMs() {
        return submittedAt.toEpochMilli() - clientTimestampMs;
    }
}
