package com.mindsoccer.shared.exception;

/**
 * Exception levée pour les erreurs d'idempotence.
 */
public class IdempotencyException extends GameException {

    public IdempotencyException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static IdempotencyException duplicate() {
        return new IdempotencyException(ErrorCode.IDEMPOTENCY_DUPLICATE);
    }

    public static IdempotencyException keyRequired() {
        return new IdempotencyException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
    }
}
