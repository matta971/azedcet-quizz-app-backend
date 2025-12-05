package com.mindsoccer.shared.exception;

/**
 * Exception levée pour les erreurs liées aux matchs.
 */
public class MatchException extends GameException {

    public MatchException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MatchException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public static MatchException alreadyStarted() {
        return new MatchException(ErrorCode.MATCH_ALREADY_STARTED);
    }

    public static MatchException notStarted() {
        return new MatchException(ErrorCode.MATCH_NOT_STARTED);
    }

    public static MatchException alreadyFinished() {
        return new MatchException(ErrorCode.MATCH_ALREADY_FINISHED);
    }

    public static MatchException full() {
        return new MatchException(ErrorCode.MATCH_FULL);
    }

    public static MatchException notParticipant() {
        return new MatchException(ErrorCode.MATCH_NOT_PARTICIPANT);
    }

    public static MatchException refereeRequired() {
        return new MatchException(ErrorCode.MATCH_REFEREE_REQUIRED);
    }

    public static MatchException alreadyParticipant() {
        return new MatchException(ErrorCode.MATCH_ALREADY_PARTICIPANT);
    }

    public static MatchException teamsIncomplete() {
        return new MatchException(ErrorCode.MATCH_TEAMS_INCOMPLETE);
    }

    public static MatchException notAuthorized(String message) {
        return new MatchException(ErrorCode.MATCH_NOT_AUTHORIZED, message);
    }
}
