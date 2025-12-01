package com.mindsoccer.shared.exception;

/**
 * Exception levée pour les erreurs liées aux joueurs.
 */
public class PlayerException extends GameException {

    public PlayerException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PlayerException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static PlayerException suspended(int pointsRemaining) {
        return new PlayerException(ErrorCode.PLAYER_SUSPENDED, pointsRemaining);
    }

    public static PlayerException notInTeam() {
        return new PlayerException(ErrorCode.PLAYER_NOT_IN_TEAM);
    }
}
