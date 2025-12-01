package com.mindsoccer.anticheat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TimingValidationService Tests")
class TimingValidationServiceTest {

    private TimingValidationService timingService;

    @BeforeEach
    void setUp() {
        timingService = new TimingValidationService();
    }

    @Nested
    @DisplayName("validate Tests")
    class ValidateTests {

        @Test
        @DisplayName("Should mark response as invalid when too fast")
        void shouldMarkAsInvalidWhenTooFast() {
            long questionShown = System.currentTimeMillis();
            long answered = questionShown + 200; // 200ms response - too fast
            long latency = 100;

            TimingValidationService.TimingValidation result =
                    timingService.validate(questionShown, answered, latency);

            assertThat(result.valid()).isFalse();
            assertThat(result.confidence()).isZero();
            assertThat(result.reason()).contains("too fast");
        }

        @Test
        @DisplayName("Should mark response as valid with normal timing")
        void shouldMarkAsValidWithNormalTiming() {
            long questionShown = System.currentTimeMillis();
            long answered = questionShown + 3000; // 3 seconds
            long latency = 50;

            TimingValidationService.TimingValidation result =
                    timingService.validate(questionShown, answered, latency);

            assertThat(result.valid()).isTrue();
            assertThat(result.confidence()).isEqualTo(1.0);
            assertThat(result.reason()).isEqualTo("Normal");
        }

        @Test
        @DisplayName("Should flag high latency")
        void shouldFlagHighLatency() {
            long questionShown = System.currentTimeMillis();
            long answered = questionShown + 10000; // 10 seconds
            long latency = 6000; // 6 seconds latency

            TimingValidationService.TimingValidation result =
                    timingService.validate(questionShown, answered, latency);

            assertThat(result.valid()).isTrue();
            assertThat(result.confidence()).isLessThan(1.0);
            assertThat(result.reason()).contains("latency");
        }

        @Test
        @DisplayName("Should flag fast but plausible responses")
        void shouldFlagFastButPlausible() {
            long questionShown = System.currentTimeMillis();
            long answered = questionShown + 800; // 800ms
            long latency = 50;

            TimingValidationService.TimingValidation result =
                    timingService.validate(questionShown, answered, latency);

            assertThat(result.valid()).isTrue();
            assertThat(result.confidence()).isLessThan(1.0);
            assertThat(result.reason()).contains("Fast");
        }
    }

    @Nested
    @DisplayName("estimateLatency Tests")
    class EstimateLatencyTests {

        @Test
        @DisplayName("Should calculate latency correctly")
        void shouldCalculateLatencyCorrectly() {
            long clientTimestamp = 1000000L;
            long serverReceived = 1000200L; // 200ms later

            long latency = timingService.estimateLatency(clientTimestamp, serverReceived);

            assertThat(latency).isEqualTo(100); // RTT / 2
        }

        @Test
        @DisplayName("Should handle clock skew")
        void shouldHandleClockSkew() {
            long clientTimestamp = 1000200L; // Client ahead
            long serverReceived = 1000000L;

            long latency = timingService.estimateLatency(clientTimestamp, serverReceived);

            assertThat(latency).isGreaterThanOrEqualTo(0); // Should be positive
        }
    }

    @Nested
    @DisplayName("detectBotPattern Tests")
    class DetectBotPatternTests {

        @Test
        @DisplayName("Should return false for null input")
        void shouldReturnFalseForNull() {
            assertThat(timingService.detectBotPattern(null)).isFalse();
        }

        @Test
        @DisplayName("Should return false for insufficient data")
        void shouldReturnFalseForInsufficientData() {
            long[] responseTimes = {1000, 1100, 1050};

            assertThat(timingService.detectBotPattern(responseTimes)).isFalse();
        }

        @Test
        @DisplayName("Should detect bot pattern with very consistent times")
        void shouldDetectBotPatternWithConsistentTimes() {
            // Very consistent timing (< 50ms variance) and fast (< 2000ms mean)
            long[] responseTimes = {1000, 1010, 1005, 1008, 1003};

            assertThat(timingService.detectBotPattern(responseTimes)).isTrue();
        }

        @Test
        @DisplayName("Should not flag normal human variance")
        void shouldNotFlagNormalHumanVariance() {
            // Normal human variance
            long[] responseTimes = {2500, 3200, 1800, 4100, 2900};

            assertThat(timingService.detectBotPattern(responseTimes)).isFalse();
        }

        @Test
        @DisplayName("Should not flag slow consistent responses")
        void shouldNotFlagSlowConsistentResponses() {
            // Consistent but slow (> 2000ms) - could be slow reader
            long[] responseTimes = {5000, 5010, 5005, 5008, 5003};

            assertThat(timingService.detectBotPattern(responseTimes)).isFalse();
        }
    }

    @Nested
    @DisplayName("TimingValidation Record Tests")
    class TimingValidationRecordTests {

        @Test
        @DisplayName("Should create validation with all fields")
        void shouldCreateValidationWithAllFields() {
            TimingValidationService.TimingValidation validation =
                    new TimingValidationService.TimingValidation(true, 0.95, "Test reason", 1500);

            assertThat(validation.valid()).isTrue();
            assertThat(validation.confidence()).isEqualTo(0.95);
            assertThat(validation.reason()).isEqualTo("Test reason");
            assertThat(validation.adjustedResponseTimeMs()).isEqualTo(1500);
        }
    }
}
