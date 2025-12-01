package com.mindsoccer.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Codes d'erreur pour MINDSOCCER.
 * Chaque code correspond à une clé i18n dans errors_*.properties
 */
public enum ErrorCode {

    // Erreurs générales (1xxx)
    INTERNAL_ERROR("error.internal", 1000, HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("error.not_found", 1001, HttpStatus.NOT_FOUND),
    FORBIDDEN("error.forbidden", 1002, HttpStatus.FORBIDDEN),
    UNAUTHORIZED("error.unauthorized", 1003, HttpStatus.UNAUTHORIZED),
    BAD_REQUEST("error.bad_request", 1004, HttpStatus.BAD_REQUEST),
    CONFLICT("error.conflict", 1005, HttpStatus.CONFLICT),

    // Erreurs d'authentification (2xxx)
    AUTH_INVALID_CREDENTIALS("error.auth.invalid_credentials", 2000, HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_EXPIRED("error.auth.token_expired", 2001, HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_INVALID("error.auth.token_invalid", 2002, HttpStatus.UNAUTHORIZED),
    AUTH_ACCOUNT_LOCKED("error.auth.account_locked", 2003, HttpStatus.FORBIDDEN),
    AUTH_EMAIL_EXISTS("error.auth.email_exists", 2004, HttpStatus.CONFLICT),
    AUTH_HANDLE_EXISTS("error.auth.handle_exists", 2005, HttpStatus.CONFLICT),
    TOKEN_EXPIRED("error.token.expired", 2010, HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("error.token.invalid", 2011, HttpStatus.UNAUTHORIZED),

    // Erreurs de match (3xxx)
    MATCH_NOT_FOUND("error.match.not_found", 3000, HttpStatus.NOT_FOUND),
    MATCH_ALREADY_STARTED("error.match.already_started", 3001, HttpStatus.CONFLICT),
    MATCH_NOT_STARTED("error.match.not_started", 3002, HttpStatus.BAD_REQUEST),
    MATCH_ALREADY_FINISHED("error.match.already_finished", 3003, HttpStatus.CONFLICT),
    MATCH_FULL("error.match.full", 3004, HttpStatus.CONFLICT),
    MATCH_NOT_PARTICIPANT("error.match.not_participant", 3005, HttpStatus.FORBIDDEN),
    MATCH_REFEREE_REQUIRED("error.match.referee_required", 3006, HttpStatus.BAD_REQUEST),
    MATCH_ALREADY_PARTICIPANT("error.match.already_participant", 3007, HttpStatus.CONFLICT),
    MATCH_TEAMS_INCOMPLETE("error.match.teams_incomplete", 3008, HttpStatus.BAD_REQUEST),

    // Erreurs de round (4xxx)
    ROUND_NOT_FOUND("error.round.not_found", 4000, HttpStatus.NOT_FOUND),
    ROUND_NOT_ACTIVE("error.round.not_active", 4001, HttpStatus.BAD_REQUEST),
    ROUND_ALREADY_ANSWERED("error.round.already_answered", 4002, HttpStatus.CONFLICT),
    ROUND_TIMEOUT("error.round.timeout", 4003, HttpStatus.BAD_REQUEST),
    ROUND_NOT_YOUR_TURN("error.round.not_your_turn", 4004, HttpStatus.FORBIDDEN),
    ROUND_INVALID_ANSWER("error.round.invalid_answer", 4005, HttpStatus.BAD_REQUEST),
    ROUND_INVALID_TIMING("error.round.invalid_timing", 4006, HttpStatus.BAD_REQUEST),

    // Erreurs de joueur (5xxx)
    PLAYER_NOT_FOUND("error.player.not_found", 5000, HttpStatus.NOT_FOUND),
    PLAYER_SUSPENDED("error.player.suspended", 5001, HttpStatus.FORBIDDEN),
    PLAYER_NOT_IN_TEAM("error.player.not_in_team", 5002, HttpStatus.BAD_REQUEST),

    // Erreurs d'équipe (6xxx)
    TEAM_NOT_FOUND("error.team.not_found", 6000, HttpStatus.NOT_FOUND),
    TEAM_INCOMPLETE("error.team.incomplete", 6001, HttpStatus.BAD_REQUEST),

    // Erreurs de question (7xxx)
    QUESTION_NOT_FOUND("error.question.not_found", 7000, HttpStatus.NOT_FOUND),
    QUESTION_INVALID_FORMAT("error.question.invalid_format", 7001, HttpStatus.BAD_REQUEST),
    THEME_NOT_FOUND("error.theme.not_found", 7100, HttpStatus.NOT_FOUND),
    THEME_CODE_EXISTS("error.theme.code_exists", 7101, HttpStatus.CONFLICT),
    THEME_HAS_QUESTIONS("error.theme.has_questions", 7102, HttpStatus.CONFLICT),
    MEDIA_NOT_FOUND("error.media.not_found", 7200, HttpStatus.NOT_FOUND),
    IMPORT_FAILED("error.import.failed", 7300, HttpStatus.BAD_REQUEST),

    // Erreurs d'idempotence (8xxx)
    IDEMPOTENCY_DUPLICATE("error.idempotency.duplicate", 8000, HttpStatus.CONFLICT),
    IDEMPOTENCY_KEY_REQUIRED("error.idempotency.key_required", 8001, HttpStatus.BAD_REQUEST),

    // Erreurs de limite (9xxx)
    RATE_LIMIT_EXCEEDED("error.rate_limit.exceeded", 9000, HttpStatus.TOO_MANY_REQUESTS),
    RATE_LIMIT_ANSWER("error.rate_limit.answer", 9001, HttpStatus.TOO_MANY_REQUESTS);

    private final String messageKey;
    private final int code;
    private final HttpStatus httpStatus;

    ErrorCode(String messageKey, int code, HttpStatus httpStatus) {
        this.messageKey = messageKey;
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
