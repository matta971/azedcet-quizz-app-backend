package com.mindsoccer.scoring.service;

import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.model.PenaltyInfo;
import com.mindsoccer.scoring.model.SuspensionOption;
import com.mindsoccer.shared.util.GameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PenaltyService Tests")
class PenaltyServiceTest {

    private PenaltyService penaltyService;
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        penaltyService = new PenaltyService();
    }

    @Nested
    @DisplayName("recordPenalty Tests")
    class RecordPenaltyTests {

        @Test
        @DisplayName("Should create penalty info with incremented count")
        void shouldCreatePenaltyInfoWithIncrementedCount() {
            PenaltyInfo penalty = penaltyService.recordPenalty(
                    playerId, TeamSide.A, 2, "Faute de jeu");

            assertThat(penalty.playerId()).isEqualTo(playerId);
            assertThat(penalty.teamSide()).isEqualTo(TeamSide.A);
            assertThat(penalty.reason()).isEqualTo("Faute de jeu");
            assertThat(penalty.penaltyNumber()).isEqualTo(3);
            assertThat(penalty.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should not trigger suspension below threshold")
        void shouldNotTriggerSuspensionBelowThreshold() {
            PenaltyInfo penalty = penaltyService.recordPenalty(
                    playerId, TeamSide.A, 3, "Test");

            assertThat(penalty.penaltyNumber()).isEqualTo(4);
            assertThat(penalty.triggersSuspension()).isFalse();
        }

        @Test
        @DisplayName("Should trigger suspension at threshold")
        void shouldTriggerSuspensionAtThreshold() {
            PenaltyInfo penalty = penaltyService.recordPenalty(
                    playerId, TeamSide.A, 4, "Test");

            assertThat(penalty.penaltyNumber()).isEqualTo(5);
            assertThat(penalty.triggersSuspension()).isTrue();
        }
    }

    @Nested
    @DisplayName("triggersSuspension Tests")
    class TriggersSuspensionTests {

        @Test
        @DisplayName("Should return false below threshold")
        void shouldReturnFalseBelowThreshold() {
            assertThat(penaltyService.triggersSuspension(1)).isFalse();
            assertThat(penaltyService.triggersSuspension(4)).isFalse();
        }

        @Test
        @DisplayName("Should return true at and above threshold")
        void shouldReturnTrueAtAndAboveThreshold() {
            assertThat(penaltyService.triggersSuspension(GameConstants.PENALTY_SUSPENSION_THRESHOLD)).isTrue();
            assertThat(penaltyService.triggersSuspension(GameConstants.PENALTY_SUSPENSION_THRESHOLD + 1)).isTrue();
        }
    }

    @Nested
    @DisplayName("getSuspensionPoints Tests")
    class GetSuspensionPointsTests {

        @Test
        @DisplayName("Should return correct points for immediate option")
        void shouldReturnCorrectPointsForImmediate() {
            int points = penaltyService.getSuspensionPoints(SuspensionOption.IMMEDIATE_40);

            assertThat(points).isEqualTo(GameConstants.SUSPENSION_OPTION_1X40);
        }

        @Test
        @DisplayName("Should return correct points for four questions option")
        void shouldReturnCorrectPointsForFourQuestions() {
            int points = penaltyService.getSuspensionPoints(SuspensionOption.FOUR_QUESTIONS);

            assertThat(points).isEqualTo(GameConstants.SUSPENSION_OPTION_4X10);
        }
    }

    @Nested
    @DisplayName("getSuspensionPointsPerQuestion Tests")
    class GetSuspensionPointsPerQuestionTests {

        @Test
        @DisplayName("Should return 10 points per question")
        void shouldReturn10PointsPerQuestion() {
            int points = penaltyService.getSuspensionPointsPerQuestion();

            assertThat(points).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("canReleaseSuspension Tests")
    class CanReleaseSuspensionTests {

        @Test
        @DisplayName("Should return true when points remaining is zero or less")
        void shouldReturnTrueWhenZeroOrLess() {
            assertThat(penaltyService.canReleaseSuspension(0)).isTrue();
            assertThat(penaltyService.canReleaseSuspension(-5)).isTrue();
        }

        @Test
        @DisplayName("Should return false when points remaining is positive")
        void shouldReturnFalseWhenPositive() {
            assertThat(penaltyService.canReleaseSuspension(10)).isFalse();
            assertThat(penaltyService.canReleaseSuspension(1)).isFalse();
        }
    }

    @Nested
    @DisplayName("getPenaltiesBeforeSuspension Tests")
    class GetPenaltiesBeforeSuspensionTests {

        @Test
        @DisplayName("Should return suspension threshold")
        void shouldReturnSuspensionThreshold() {
            int threshold = penaltyService.getPenaltiesBeforeSuspension();

            assertThat(threshold).isEqualTo(GameConstants.PENALTY_SUSPENSION_THRESHOLD);
        }
    }
}
