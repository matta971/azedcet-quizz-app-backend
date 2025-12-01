package com.mindsoccer.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RoundException Tests")
class RoundExceptionTest {

    @Test
    @DisplayName("Should create exception with error code")
    void shouldCreateExceptionWithErrorCode() {
        RoundException ex = new RoundException(ErrorCode.ROUND_NOT_ACTIVE);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_NOT_ACTIVE);
    }

    @Test
    @DisplayName("Should create exception with error code and args")
    void shouldCreateExceptionWithErrorCodeAndArgs() {
        RoundException ex = new RoundException(ErrorCode.ROUND_NOT_ACTIVE, "Round1");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_NOT_ACTIVE);
        assertThat(ex.getArgs()).containsExactly("Round1");
    }

    @Test
    @DisplayName("Should create not active exception")
    void shouldCreateNotActiveException() {
        RoundException ex = RoundException.notActive();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_NOT_ACTIVE);
    }

    @Test
    @DisplayName("Should create already answered exception")
    void shouldCreateAlreadyAnsweredException() {
        RoundException ex = RoundException.alreadyAnswered();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_ALREADY_ANSWERED);
    }

    @Test
    @DisplayName("Should create timeout exception")
    void shouldCreateTimeoutException() {
        RoundException ex = RoundException.timeout();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_TIMEOUT);
    }

    @Test
    @DisplayName("Should create not your turn exception")
    void shouldCreateNotYourTurnException() {
        RoundException ex = RoundException.notYourTurn();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_NOT_YOUR_TURN);
    }

    @Test
    @DisplayName("Should create invalid answer exception")
    void shouldCreateInvalidAnswerException() {
        RoundException ex = RoundException.invalidAnswer();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_INVALID_ANSWER);
    }

    @Test
    @DisplayName("Should create invalid timing exception")
    void shouldCreateInvalidTimingException() {
        RoundException ex = RoundException.invalidTiming();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_INVALID_TIMING);
    }
}
