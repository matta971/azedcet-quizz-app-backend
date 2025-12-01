package com.mindsoccer.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IdempotencyException Tests")
class IdempotencyExceptionTest {

    @Test
    @DisplayName("Should create exception with error code")
    void shouldCreateExceptionWithErrorCode() {
        IdempotencyException ex = new IdempotencyException(ErrorCode.IDEMPOTENCY_DUPLICATE);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.IDEMPOTENCY_DUPLICATE);
    }

    @Test
    @DisplayName("Should create duplicate exception")
    void shouldCreateDuplicateException() {
        IdempotencyException ex = IdempotencyException.duplicate();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.IDEMPOTENCY_DUPLICATE);
    }

    @Test
    @DisplayName("Should create key required exception")
    void shouldCreateKeyRequiredException() {
        IdempotencyException ex = IdempotencyException.keyRequired();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
    }
}
