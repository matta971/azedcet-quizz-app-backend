package com.mindsoccer.shared.exception;

/**
 * Codes d'erreur pour MINDSOCCER.
 * Chaque code correspond à une clé i18n dans errors_*.properties
 */
public enum ErrorCode {

    // Erreurs générales (1xxx)
    INTERNAL_ERROR("error.internal", 1000),
    NOT_FOUND("error.not_found", 1001),
    FORBIDDEN("error.forbidden", 1002),
    UNAUTHORIZED("error.unauthorized", 1003),
    BAD_REQUEST("error.bad_request", 1004),
    CONFLICT("error.conflict", 1005),

    // Erreurs d'authentification (2xxx)
    AUTH_INVALID_CREDENTIALS("error.auth.invalid_credentials", 2000),
    AUTH_TOKEN_EXPIRED("error.auth.token_expired", 2001),
    AUTH_TOKEN_INVALID("error.auth.token_invalid", 2002),
    AUTH_ACCOUNT_LOCKED("error.auth.account_locked", 2003),
    AUTH_EMAIL_EXISTS("error.auth.email_exists", 2004),
    AUTH_HANDLE_EXISTS("error.auth.handle_exists", 2005),

    // Erreurs de match (3xxx)
    MATCH_NOT_FOUND("error.match.not_found", 3000),
    MATCH_ALREADY_STARTED("error.match.already_started", 3001),
    MATCH_NOT_STARTED("error.match.not_started", 3002),
    MATCH_ALREADY_FINISHED("error.match.already_finished", 3003),
    MATCH_FULL("error.match.full", 3004),
    MATCH_NOT_PARTICIPANT("error.match.not_participant", 3005),
    MATCH_REFEREE_REQUIRED("error.match.referee_required", 3006),
    MATCH_ALREADY_PARTICIPANT("error.match.already_participant", 3007),
    MATCH_TEAMS_INCOMPLETE("error.match.teams_incomplete", 3008),

    // Erreurs de round (4xxx)
    ROUND_NOT_FOUND("error.round.not_found", 4000),
    ROUND_NOT_ACTIVE("error.round.not_active", 4001),
    ROUND_ALREADY_ANSWERED("error.round.already_answered", 4002),
    ROUND_TIMEOUT("error.round.timeout", 4003),
    ROUND_NOT_YOUR_TURN("error.round.not_your_turn", 4004),
    ROUND_INVALID_ANSWER("error.round.invalid_answer", 4005),
    ROUND_INVALID_TIMING("error.round.invalid_timing", 4006),

    // Erreurs de joueur (5xxx)
    PLAYER_NOT_FOUND("error.player.not_found", 5000),
    PLAYER_SUSPENDED("error.player.suspended", 5001),
    PLAYER_NOT_IN_TEAM("error.player.not_in_team", 5002),

    // Erreurs d'équipe (6xxx)
    TEAM_NOT_FOUND("error.team.not_found", 6000),
    TEAM_INCOMPLETE("error.team.incomplete", 6001),

    // Erreurs de question (7xxx)
    QUESTION_NOT_FOUND("error.question.not_found", 7000),
    QUESTION_INVALID_FORMAT("error.question.invalid_format", 7001),
    THEME_NOT_FOUND("error.theme.not_found", 7100),
    THEME_CODE_EXISTS("error.theme.code_exists", 7101),
    THEME_HAS_QUESTIONS("error.theme.has_questions", 7102),
    MEDIA_NOT_FOUND("error.media.not_found", 7200),
    IMPORT_FAILED("error.import.failed", 7300),

    // Erreurs d'idempotence (8xxx)
    IDEMPOTENCY_DUPLICATE("error.idempotency.duplicate", 8000),
    IDEMPOTENCY_KEY_REQUIRED("error.idempotency.key_required", 8001),

    // Erreurs de limite (9xxx)
    RATE_LIMIT_EXCEEDED("error.rate_limit.exceeded", 9000),
    RATE_LIMIT_ANSWER("error.rate_limit.answer", 9001);

    private final String messageKey;
    private final int code;

    ErrorCode(String messageKey, int code) {
        this.messageKey = messageKey;
        this.code = code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public int getCode() {
        return code;
    }
}
