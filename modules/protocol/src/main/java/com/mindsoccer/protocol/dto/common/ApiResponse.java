package com.mindsoccer.protocol.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Enveloppe de réponse API standard.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorInfo error,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message, null), Instant.now());
    }

    public static <T> ApiResponse<T> error(int code, String message, Object details) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message, details), Instant.now());
    }

    public record ErrorInfo(
            int code,
            String message,
            Object details
    ) {}
}
