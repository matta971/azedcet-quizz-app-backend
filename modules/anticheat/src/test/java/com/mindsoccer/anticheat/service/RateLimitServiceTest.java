package com.mindsoccer.anticheat.service;

import com.mindsoccer.shared.exception.RateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RateLimitService Tests")
class RateLimitServiceTest {

    private RateLimitService rateLimitService;
    private final UUID playerId = UUID.randomUUID();
    private final UUID matchId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Nested
    @DisplayName("checkAnswer Tests")
    class CheckAnswerTests {

        @Test
        @DisplayName("Should allow first answer")
        void shouldAllowFirstAnswer() {
            assertThatNoException().isThrownBy(() ->
                    rateLimitService.checkAnswer(playerId, matchId)
            );
        }

        @Test
        @DisplayName("Should allow second answer within limit")
        void shouldAllowSecondAnswerWithinLimit() {
            rateLimitService.checkAnswer(playerId, matchId);

            assertThatNoException().isThrownBy(() ->
                    rateLimitService.checkAnswer(playerId, matchId)
            );
        }

        @Test
        @DisplayName("Should throw when exceeding rate limit")
        void shouldThrowWhenExceedingRateLimit() {
            // Use up all tokens
            rateLimitService.checkAnswer(playerId, matchId);
            rateLimitService.checkAnswer(playerId, matchId);

            // Third attempt should fail
            assertThatThrownBy(() -> rateLimitService.checkAnswer(playerId, matchId))
                    .isInstanceOf(RateLimitException.class);
        }

        @Test
        @DisplayName("Different players should have separate limits")
        void differentPlayersShouldHaveSeparateLimits() {
            UUID player2 = UUID.randomUUID();

            rateLimitService.checkAnswer(playerId, matchId);
            rateLimitService.checkAnswer(playerId, matchId);

            // Player 2 should still have tokens
            assertThatNoException().isThrownBy(() ->
                    rateLimitService.checkAnswer(player2, matchId)
            );
        }
    }

    @Nested
    @DisplayName("checkBuzzer Tests")
    class CheckBuzzerTests {

        @Test
        @DisplayName("Should allow buzzer presses within limit")
        void shouldAllowBuzzerWithinLimit() {
            assertThatNoException().isThrownBy(() -> {
                rateLimitService.checkBuzzer(playerId, matchId);
                rateLimitService.checkBuzzer(playerId, matchId);
                rateLimitService.checkBuzzer(playerId, matchId);
            });
        }

        @Test
        @DisplayName("Should throw when exceeding buzzer rate limit")
        void shouldThrowWhenExceedingBuzzerLimit() {
            rateLimitService.checkBuzzer(playerId, matchId);
            rateLimitService.checkBuzzer(playerId, matchId);
            rateLimitService.checkBuzzer(playerId, matchId);

            assertThatThrownBy(() -> rateLimitService.checkBuzzer(playerId, matchId))
                    .isInstanceOf(RateLimitException.class);
        }
    }

    @Nested
    @DisplayName("checkGeneralRequest Tests")
    class CheckGeneralRequestTests {

        @Test
        @DisplayName("Should allow requests within limit")
        void shouldAllowRequestsWithinLimit() {
            for (int i = 0; i < 50; i++) {
                assertThatNoException().isThrownBy(() ->
                        rateLimitService.checkGeneralRequest(playerId)
                );
            }
        }
    }

    @Nested
    @DisplayName("resetForPlayer Tests")
    class ResetForPlayerTests {

        @Test
        @DisplayName("Should reset limits for player")
        void shouldResetLimitsForPlayer() {
            // Exhaust tokens
            rateLimitService.checkAnswer(playerId, matchId);
            rateLimitService.checkAnswer(playerId, matchId);

            // Reset
            rateLimitService.resetForPlayer(playerId, matchId);

            // Should be able to answer again
            assertThatNoException().isThrownBy(() ->
                    rateLimitService.checkAnswer(playerId, matchId)
            );
        }
    }

    @Nested
    @DisplayName("cleanup Tests")
    class CleanupTests {

        @Test
        @DisplayName("Should not throw on cleanup")
        void shouldNotThrowOnCleanup() {
            rateLimitService.checkAnswer(playerId, matchId);

            assertThatNoException().isThrownBy(() ->
                    rateLimitService.cleanup()
            );
        }
    }
}
