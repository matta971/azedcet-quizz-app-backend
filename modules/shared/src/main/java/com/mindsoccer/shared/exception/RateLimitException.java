package com.mindsoccer.shared.exception;

/**
 * Exception levée quand une limite de taux est dépassée.
 */
public class RateLimitException extends GameException {

    public RateLimitException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static RateLimitException exceeded() {
        return new RateLimitException(ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    public static RateLimitException answerLimit() {
        return new RateLimitException(ErrorCode.RATE_LIMIT_ANSWER);
    }

    public static RateLimitException rateLimitExceeded() {
        return new RateLimitException(ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    public static RateLimitException answerRateExceeded() {
        return new RateLimitException(ErrorCode.RATE_LIMIT_ANSWER);
    }
}
