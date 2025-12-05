package com.mindsoccer.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GameConstants Tests")
class GameConstantsTest {

    @Test
    @DisplayName("Team size should be positive")
    void teamSizeShouldBePositive() {
        assertThat(GameConstants.TEAM_SIZE_SOLO).isPositive();
        assertThat(GameConstants.TEAM_DUO_SIZE).isPositive();
        // Duo is now 1v1, same as solo team size
        assertThat(GameConstants.TEAM_DUO_SIZE).isGreaterThanOrEqualTo(GameConstants.TEAM_SIZE_SOLO);
    }

    @Test
    @DisplayName("Question counts should be positive")
    void questionCountsShouldBePositive() {
        assertThat(GameConstants.CASCADE_QUESTION_COUNT).isPositive();
        assertThat(GameConstants.SMASH_QUESTION_COUNT).isPositive();
        assertThat(GameConstants.SPRINT_QUESTION_COUNT).isPositive();
    }

    @Test
    @DisplayName("Match code length should be reasonable")
    void matchCodeLengthShouldBeReasonable() {
        assertThat(GameConstants.MATCH_CODE_LENGTH).isBetween(4, 10);
    }

    @Test
    @DisplayName("Penalty thresholds should be positive")
    void penaltyThresholdsShouldBePositive() {
        assertThat(GameConstants.PENALTY_SUSPENSION_THRESHOLD).isPositive();
        assertThat(GameConstants.PENALTY_SUSPENSION_POINTS).isPositive();
    }

    @Test
    @DisplayName("Points values should be non-negative")
    void pointsValuesShouldBeNonNegative() {
        assertThat(GameConstants.CASCADE_BASE_POINTS).isPositive();
        assertThat(GameConstants.CASCADE_INCREMENT).isPositive();
        assertThat(GameConstants.SMASH_CORRECT_POINTS).isPositive();
        assertThat(GameConstants.SMASH_STEAL_POINTS).isPositive();
        assertThat(GameConstants.SPRINT_POINTS_PER_QUESTION).isPositive();
    }

    @Test
    @DisplayName("Timeouts should be positive")
    void timeoutsShouldBePositive() {
        assertThat(GameConstants.DEFAULT_ANSWER_TIMEOUT_MS).isPositive();
        assertThat(GameConstants.BUZZER_TIMEOUT_MS).isPositive();
    }
}
