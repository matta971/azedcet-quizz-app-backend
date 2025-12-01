package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.TeamSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AnswerPayload Tests")
class AnswerPayloadTest {

    @Test
    @DisplayName("Should create payload with all fields")
    void shouldCreatePayloadWithAllFields() {
        UUID matchId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Instant now = Instant.now();
        long clientTimestamp = now.toEpochMilli() - 100;

        AnswerPayload payload = new AnswerPayload(
                matchId, roundId, questionId, playerId,
                TeamSide.A, "Paris", now, clientTimestamp, "key-123"
        );

        assertThat(payload.matchId()).isEqualTo(matchId);
        assertThat(payload.roundId()).isEqualTo(roundId);
        assertThat(payload.questionId()).isEqualTo(questionId);
        assertThat(payload.playerId()).isEqualTo(playerId);
        assertThat(payload.team()).isEqualTo(TeamSide.A);
        assertThat(payload.answer()).isEqualTo("Paris");
        assertThat(payload.submittedAt()).isEqualTo(now);
        assertThat(payload.clientTimestampMs()).isEqualTo(clientTimestamp);
        assertThat(payload.idempotencyKey()).isEqualTo("key-123");
    }

    @Test
    @DisplayName("Should calculate latency correctly")
    void shouldCalculateLatencyCorrectly() {
        Instant now = Instant.now();
        long clientTimestamp = now.toEpochMilli() - 150; // 150ms ago

        AnswerPayload payload = new AnswerPayload(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TeamSide.B, "London", now, clientTimestamp, "key"
        );

        assertThat(payload.latencyMs()).isEqualTo(150);
    }

    @Test
    @DisplayName("Should handle zero latency")
    void shouldHandleZeroLatency() {
        Instant now = Instant.now();
        long clientTimestamp = now.toEpochMilli();

        AnswerPayload payload = new AnswerPayload(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TeamSide.A, "Test", now, clientTimestamp, "key"
        );

        assertThat(payload.latencyMs()).isZero();
    }

    @Test
    @DisplayName("Should handle negative latency (clock skew)")
    void shouldHandleNegativeLatency() {
        Instant now = Instant.now();
        long clientTimestamp = now.toEpochMilli() + 50; // Future timestamp (clock skew)

        AnswerPayload payload = new AnswerPayload(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TeamSide.A, "Test", now, clientTimestamp, "key"
        );

        assertThat(payload.latencyMs()).isEqualTo(-50);
    }

    @Test
    @DisplayName("Should support both team sides")
    void shouldSupportBothTeamSides() {
        Instant now = Instant.now();

        AnswerPayload homePayload = new AnswerPayload(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TeamSide.A, "Test", now, now.toEpochMilli(), "key1"
        );

        AnswerPayload awayPayload = new AnswerPayload(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TeamSide.B, "Test", now, now.toEpochMilli(), "key2"
        );

        assertThat(homePayload.team()).isEqualTo(TeamSide.A);
        assertThat(awayPayload.team()).isEqualTo(TeamSide.B);
    }
}
