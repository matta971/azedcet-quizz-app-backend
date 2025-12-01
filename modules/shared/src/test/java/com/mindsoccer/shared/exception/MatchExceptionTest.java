package com.mindsoccer.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MatchException Tests")
class MatchExceptionTest {

    @Test
    @DisplayName("Should create exception with error code")
    void shouldCreateExceptionWithErrorCode() {
        MatchException ex = new MatchException(ErrorCode.MATCH_NOT_FOUND);

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_NOT_FOUND);
    }

    @Test
    @DisplayName("Should create exception with error code and args")
    void shouldCreateExceptionWithErrorCodeAndArgs() {
        MatchException ex = new MatchException(ErrorCode.MATCH_FULL, "Match123");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_FULL);
        assertThat(ex.getArgs()).containsExactly("Match123");
    }

    @Test
    @DisplayName("Should create already started exception")
    void shouldCreateAlreadyStartedException() {
        MatchException ex = MatchException.alreadyStarted();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_ALREADY_STARTED);
    }

    @Test
    @DisplayName("Should create not started exception")
    void shouldCreateNotStartedException() {
        MatchException ex = MatchException.notStarted();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_NOT_STARTED);
    }

    @Test
    @DisplayName("Should create already finished exception")
    void shouldCreateAlreadyFinishedException() {
        MatchException ex = MatchException.alreadyFinished();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_ALREADY_FINISHED);
    }

    @Test
    @DisplayName("Should create full exception")
    void shouldCreateFullException() {
        MatchException ex = MatchException.full();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_FULL);
    }

    @Test
    @DisplayName("Should create not participant exception")
    void shouldCreateNotParticipantException() {
        MatchException ex = MatchException.notParticipant();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_NOT_PARTICIPANT);
    }

    @Test
    @DisplayName("Should create referee required exception")
    void shouldCreateRefereeRequiredException() {
        MatchException ex = MatchException.refereeRequired();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_REFEREE_REQUIRED);
    }

    @Test
    @DisplayName("Should create already participant exception")
    void shouldCreateAlreadyParticipantException() {
        MatchException ex = MatchException.alreadyParticipant();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_ALREADY_PARTICIPANT);
    }

    @Test
    @DisplayName("Should create teams incomplete exception")
    void shouldCreateTeamsIncompleteException() {
        MatchException ex = MatchException.teamsIncomplete();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_TEAMS_INCOMPLETE);
    }
}
