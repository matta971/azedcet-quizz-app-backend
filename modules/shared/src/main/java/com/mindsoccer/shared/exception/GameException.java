package com.mindsoccer.shared.exception;

import lombok.Getter;

/**
 * Exception de base pour toutes les erreurs métier MINDSOCCER.
 */
@Getter
public class GameException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public GameException(ErrorCode errorCode) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
        this.args = new Object[]{};
    }

    public GameException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
        this.args = args;
    }

    public GameException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessageKey(), cause);
        this.errorCode = errorCode;
        this.args = new Object[]{};
    }

    public GameException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.getMessageKey(), cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    public int getCode() {
        return errorCode.getCode();
    }

    public String getMessageKey() {
        return errorCode.getMessageKey();
    }
}
