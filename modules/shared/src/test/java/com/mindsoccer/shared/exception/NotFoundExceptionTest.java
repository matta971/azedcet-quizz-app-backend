package com.mindsoccer.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotFoundException Tests")
class NotFoundExceptionTest {

    @Test
    @DisplayName("Should create match not found exception")
    void shouldCreateMatchNotFoundException() {
        NotFoundException ex = NotFoundException.match();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MATCH_NOT_FOUND);
    }

    @Test
    @DisplayName("Should create player not found exception")
    void shouldCreatePlayerNotFoundException() {
        NotFoundException ex = NotFoundException.player();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PLAYER_NOT_FOUND);
    }

    @Test
    @DisplayName("Should create round not found exception")
    void shouldCreateRoundNotFoundException() {
        NotFoundException ex = NotFoundException.round();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROUND_NOT_FOUND);
    }

    @Test
    @DisplayName("Should create team not found exception")
    void shouldCreateTeamNotFoundException() {
        NotFoundException ex = NotFoundException.team();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TEAM_NOT_FOUND);
    }

    @Test
    @DisplayName("Should create question not found exception")
    void shouldCreateQuestionNotFoundException() {
        NotFoundException ex = NotFoundException.question();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("Should create theme not found exception")
    void shouldCreateThemeNotFoundException() {
        NotFoundException ex = NotFoundException.theme();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.THEME_NOT_FOUND);
    }

    @Test
    @DisplayName("Should create media not found exception")
    void shouldCreateMediaNotFoundException() {
        NotFoundException ex = NotFoundException.media();

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEDIA_NOT_FOUND);
    }
}
