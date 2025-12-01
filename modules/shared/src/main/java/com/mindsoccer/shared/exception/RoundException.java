package com.mindsoccer.shared.exception;

/**
 * Exception levée pour les erreurs liées aux rubriques (rounds).
 */
public class RoundException extends GameException {

    public RoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RoundException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static RoundException notActive() {
        return new RoundException(ErrorCode.ROUND_NOT_ACTIVE);
    }

    public static RoundException alreadyAnswered() {
        return new RoundException(ErrorCode.ROUND_ALREADY_ANSWERED);
    }

    public static RoundException timeout() {
        return new RoundException(ErrorCode.ROUND_TIMEOUT);
    }

    public static RoundException notYourTurn() {
        return new RoundException(ErrorCode.ROUND_NOT_YOUR_TURN);
    }

    public static RoundException invalidAnswer() {
        return new RoundException(ErrorCode.ROUND_INVALID_ANSWER);
    }

    public static RoundException invalidTiming() {
        return new RoundException(ErrorCode.ROUND_INVALID_TIMING);
    }
}
