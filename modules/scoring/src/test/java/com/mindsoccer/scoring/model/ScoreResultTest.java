package com.mindsoccer.scoring.model;

import com.mindsoccer.protocol.enums.TeamSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ScoreResult Tests")
class ScoreResultTest {

    private final UUID playerId = UUID.randomUUID();

    @Nested
    @DisplayName("forTeam Factory Method Tests")
    class ForTeamTests {

        @Test
        @DisplayName("Should create team score result")
        void shouldCreateTeamScoreResult() {
            ScoreResult result = ScoreResult.forTeam(50, TeamSide.HOME, "Victory bonus");

            assertThat(result.points()).isEqualTo(50);
            assertThat(result.teamSide()).isEqualTo(TeamSide.HOME);
            assertThat(result.playerId()).isNull();
            assertThat(result.reason()).isEqualTo("Victory bonus");
            assertThat(result.isBonus()).isFalse();
            assertThat(result.isPenalty()).isFalse();
        }
    }

    @Nested
    @DisplayName("forPlayer Factory Method Tests")
    class ForPlayerTests {

        @Test
        @DisplayName("Should create player score result")
        void shouldCreatePlayerScoreResult() {
            ScoreResult result = ScoreResult.forPlayer(10, TeamSide.AWAY, playerId, "Correct answer");

            assertThat(result.points()).isEqualTo(10);
            assertThat(result.teamSide()).isEqualTo(TeamSide.AWAY);
            assertThat(result.playerId()).isEqualTo(playerId);
            assertThat(result.reason()).isEqualTo("Correct answer");
            assertThat(result.isBonus()).isFalse();
            assertThat(result.isPenalty()).isFalse();
        }
    }

    @Nested
    @DisplayName("bonus Factory Method Tests")
    class BonusTests {

        @Test
        @DisplayName("Should create team bonus result")
        void shouldCreateTeamBonusResult() {
            ScoreResult result = ScoreResult.bonus(20, TeamSide.HOME, "Speed bonus");

            assertThat(result.points()).isEqualTo(20);
            assertThat(result.teamSide()).isEqualTo(TeamSide.HOME);
            assertThat(result.playerId()).isNull();
            assertThat(result.reason()).isEqualTo("Speed bonus");
            assertThat(result.isBonus()).isTrue();
            assertThat(result.isPenalty()).isFalse();
        }

        @Test
        @DisplayName("Should create player bonus result")
        void shouldCreatePlayerBonusResult() {
            ScoreResult result = ScoreResult.bonus(15, TeamSide.HOME, playerId, "MVP bonus");

            assertThat(result.points()).isEqualTo(15);
            assertThat(result.playerId()).isEqualTo(playerId);
            assertThat(result.isBonus()).isTrue();
        }
    }

    @Nested
    @DisplayName("penalty Factory Method Tests")
    class PenaltyTests {

        @Test
        @DisplayName("Should create penalty result with negative points")
        void shouldCreatePenaltyResultWithNegativePoints() {
            ScoreResult result = ScoreResult.penalty(10, TeamSide.AWAY, playerId, "Foul");

            assertThat(result.points()).isEqualTo(-10);
            assertThat(result.teamSide()).isEqualTo(TeamSide.AWAY);
            assertThat(result.playerId()).isEqualTo(playerId);
            assertThat(result.reason()).isEqualTo("Foul");
            assertThat(result.isBonus()).isFalse();
            assertThat(result.isPenalty()).isTrue();
        }
    }

    @Nested
    @DisplayName("zero Factory Method Tests")
    class ZeroTests {

        @Test
        @DisplayName("Should create zero score result")
        void shouldCreateZeroScoreResult() {
            ScoreResult result = ScoreResult.zero();

            assertThat(result.points()).isZero();
            assertThat(result.teamSide()).isNull();
            assertThat(result.playerId()).isNull();
            assertThat(result.reason()).isNull();
            assertThat(result.isBonus()).isFalse();
            assertThat(result.isPenalty()).isFalse();
        }
    }
}
