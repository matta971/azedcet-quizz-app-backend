package com.mindsoccer.shared.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

/**
 * Exception levée pour les erreurs de validation.
 */
@Getter
public class ValidationException extends GameException {

    private final Map<String, String> fieldErrors;

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(ErrorCode errorCode, Map<String, String> fieldErrors) {
        super(errorCode);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyMap();
    }

    public ValidationException(ErrorCode errorCode, String field, String message) {
        super(errorCode);
        this.fieldErrors = Map.of(field, message);
    }

    public static ValidationException badRequest() {
        return new ValidationException(ErrorCode.BAD_REQUEST);
    }

    public static ValidationException invalidAnswer() {
        return new ValidationException(ErrorCode.ROUND_INVALID_ANSWER);
    }

    public static ValidationException invalidFormat() {
        return new ValidationException(ErrorCode.QUESTION_INVALID_FORMAT);
    }

    public static ValidationException themeCodeExists() {
        return new ValidationException(ErrorCode.THEME_CODE_EXISTS);
    }

    public static ValidationException themeHasQuestions() {
        return new ValidationException(ErrorCode.THEME_HAS_QUESTIONS);
    }

    public static ValidationException importFailed(String message) {
        return new ValidationException(ErrorCode.IMPORT_FAILED, "file", message);
    }
}
