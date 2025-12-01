package com.mindsoccer.shared.exception;

/**
 * Exception levée quand une ressource n'est pas trouvée.
 */
public class NotFoundException extends GameException {

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static NotFoundException match() {
        return new NotFoundException(ErrorCode.MATCH_NOT_FOUND);
    }

    public static NotFoundException round() {
        return new NotFoundException(ErrorCode.ROUND_NOT_FOUND);
    }

    public static NotFoundException player() {
        return new NotFoundException(ErrorCode.PLAYER_NOT_FOUND);
    }

    public static NotFoundException team() {
        return new NotFoundException(ErrorCode.TEAM_NOT_FOUND);
    }

    public static NotFoundException question() {
        return new NotFoundException(ErrorCode.QUESTION_NOT_FOUND);
    }

    public static NotFoundException theme() {
        return new NotFoundException(ErrorCode.THEME_NOT_FOUND);
    }

    public static NotFoundException media() {
        return new NotFoundException(ErrorCode.MEDIA_NOT_FOUND);
    }
}
