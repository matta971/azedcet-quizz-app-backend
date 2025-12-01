package com.mindsoccer.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GameException Tests")
class GameExceptionTest {

    @Test
    @DisplayName("Should create exception with error code")
    void shouldCreateExceptionWithErrorCode() {
        GameException ex = new GameException(ErrorCode.MATCH_NOT_FOUND);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_NOT_FOUND);
        assertThat(ex.getArgs()).isEmpty();
        assertThat(ex.getMessage()).isEqualTo(ErrorCode.MATCH_NOT_FOUND.getMessageKey());
    }

    @Test
    @DisplayName("Should create exception with error code and args")
    void shouldCreateExceptionWithErrorCodeAndArgs() {
        Object[] args = {"arg1", 42};
        GameException ex = new GameException(ErrorCode.PLAYER_SUSPENDED, args);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PLAYER_SUSPENDED);
        assertThat(ex.getArgs()).containsExactly("arg1", 42);
    }

    @Test
    @DisplayName("Should create exception with error code and cause")
    void shouldCreateExceptionWithErrorCodeAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        GameException ex = new GameException(ErrorCode.INTERNAL_ERROR, cause);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR);
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
