package com.mindsoccer.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ErrorCode Tests")
class ErrorCodeTest {

    @Test
    @DisplayName("Should have unique codes for all error types")
    void shouldHaveUniqueCodes() {
        ErrorCode[] codes = ErrorCode.values();

        long uniqueCount = java.util.Arrays.stream(codes)
                .mapToInt(ErrorCode::getCode)
                .distinct()
                .count();

        assertThat(uniqueCount).isEqualTo(codes.length);
    }

    @Test
    @DisplayName("Should have message key for all error codes")
    void shouldHaveMessageKeyForAllCodes() {
        for (ErrorCode code : ErrorCode.values()) {
            assertThat(code.getMessageKey())
                    .isNotNull()
                    .isNotEmpty()
                    .startsWith("error.");
        }
    }

    @Test
    @DisplayName("Should have HTTP status for all error codes")
    void shouldHaveHttpStatusForAllCodes() {
        for (ErrorCode code : ErrorCode.values()) {
            assertThat(code.getHttpStatus())
                    .isNotNull()
                    .satisfies(status ->
                        assertThat(status.value()).isBetween(400, 599)
                    );
        }
    }

    @Test
    @DisplayName("Internal error should be 500")
    void internalErrorShouldBe500() {
        assertThat(ErrorCode.INTERNAL_ERROR.getHttpStatus().value()).isEqualTo(500);
        assertThat(ErrorCode.INTERNAL_ERROR.getCode()).isEqualTo(1000);
    }

    @Test
    @DisplayName("Not found errors should be 404")
    void notFoundErrorsShouldBe404() {
        assertThat(ErrorCode.NOT_FOUND.getHttpStatus().value()).isEqualTo(404);
        assertThat(ErrorCode.MATCH_NOT_FOUND.getHttpStatus().value()).isEqualTo(404);
        assertThat(ErrorCode.PLAYER_NOT_FOUND.getHttpStatus().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("Auth errors should be 401 or 403")
    void authErrorsShouldBeUnauthorizedOrForbidden() {
        assertThat(ErrorCode.UNAUTHORIZED.getHttpStatus().value()).isEqualTo(401);
        assertThat(ErrorCode.FORBIDDEN.getHttpStatus().value()).isEqualTo(403);
        assertThat(ErrorCode.TOKEN_INVALID.getHttpStatus().value()).isEqualTo(401);
        assertThat(ErrorCode.TOKEN_EXPIRED.getHttpStatus().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("Rate limit errors should be 429")
    void rateLimitErrorsShouldBe429() {
        assertThat(ErrorCode.RATE_LIMIT_EXCEEDED.getHttpStatus().value()).isEqualTo(429);
        assertThat(ErrorCode.RATE_LIMIT_ANSWER.getHttpStatus().value()).isEqualTo(429);
    }
}
