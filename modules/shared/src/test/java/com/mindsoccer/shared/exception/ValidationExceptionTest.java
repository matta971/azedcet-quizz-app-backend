package com.mindsoccer.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ValidationException Tests")
class ValidationExceptionTest {

    @Test
    @DisplayName("Should create exception with error code only")
    void shouldCreateExceptionWithErrorCodeOnly() {
        ValidationException ex = new ValidationException(ErrorCode.BAD_REQUEST);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(ex.getFieldErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should create exception with field errors map")
    void shouldCreateExceptionWithFieldErrorsMap() {
        Map<String, String> fieldErrors = Map.of(
                "username", "Username is required",
                "email", "Invalid email format"
        );
        ValidationException ex = new ValidationException(ErrorCode.BAD_REQUEST, fieldErrors);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(ex.getFieldErrors()).hasSize(2);
        assertThat(ex.getFieldErrors()).containsEntry("username", "Username is required");
        assertThat(ex.getFieldErrors()).containsEntry("email", "Invalid email format");
    }

    @Test
    @DisplayName("Should handle null field errors map")
    void shouldHandleNullFieldErrorsMap() {
        ValidationException ex = new ValidationException(ErrorCode.BAD_REQUEST, null);

        assertThat(ex.getFieldErrors()).isEmpty();
    }

    @Test
    @DisplayName("Should create exception with single field error")
    void shouldCreateExceptionWithSingleFieldError() {
        ValidationException ex = new ValidationException(ErrorCode.BAD_REQUEST, "password", "Password too short");

        assertThat(ex.getFieldErrors()).hasSize(1);
        assertThat(ex.getFieldErrors()).containsEntry("password", "Password too short");
    }

    @Test
    @DisplayName("Should create bad request exception")
    void shouldCreateBadRequestException() {
        ValidationException ex = ValidationException.badRequest();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should create invalid answer exception")
    void shouldCreateInvalidAnswerException() {
        ValidationException ex = ValidationException.invalidAnswer();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_INVALID_ANSWER);
    }

    @Test
    @DisplayName("Should create invalid format exception")
    void shouldCreateInvalidFormatException() {
        ValidationException ex = ValidationException.invalidFormat();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.QUESTION_INVALID_FORMAT);
    }

    @Test
    @DisplayName("Should create theme code exists exception")
    void shouldCreateThemeCodeExistsException() {
        ValidationException ex = ValidationException.themeCodeExists();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.THEME_CODE_EXISTS);
    }

    @Test
    @DisplayName("Should create theme has questions exception")
    void shouldCreateThemeHasQuestionsException() {
        ValidationException ex = ValidationException.themeHasQuestions();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.THEME_HAS_QUESTIONS);
    }

    @Test
    @DisplayName("Should create import failed exception")
    void shouldCreateImportFailedException() {
        ValidationException ex = ValidationException.importFailed("CSV parse error");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.IMPORT_FAILED);
        assertThat(ex.getFieldErrors()).containsEntry("file", "CSV parse error");
    }
}
