package com.mindsoccer.shared.exception;

/**
 * Exception levée pour les erreurs d'authentification.
 */
public class AuthenticationException extends GameException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }

    public static AuthenticationException tokenExpired() {
        return new AuthenticationException(ErrorCode.AUTH_TOKEN_EXPIRED);
    }

    public static AuthenticationException tokenInvalid() {
        return new AuthenticationException(ErrorCode.AUTH_TOKEN_INVALID);
    }

    public static AuthenticationException accountLocked() {
        return new AuthenticationException(ErrorCode.AUTH_ACCOUNT_LOCKED);
    }

    public static AuthenticationException emailExists() {
        return new AuthenticationException(ErrorCode.AUTH_EMAIL_EXISTS);
    }

    public static AuthenticationException handleExists() {
        return new AuthenticationException(ErrorCode.AUTH_HANDLE_EXISTS);
    }

    public static AuthenticationException unauthorized() {
        return new AuthenticationException(ErrorCode.UNAUTHORIZED);
    }

    public static AuthenticationException forbidden() {
        return new AuthenticationException(ErrorCode.FORBIDDEN);
    }
}
