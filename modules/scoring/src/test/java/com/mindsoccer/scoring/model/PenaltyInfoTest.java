package com.mindsoccer.scoring.model;

import com.mindsoccer.protocol.enums.TeamSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PenaltyInfo Tests")
class PenaltyInfoTest {

    private final UUID playerId = UUID.randomUUID();

    @Test
    @DisplayName("Should create penalty info with all fields")
    void shouldCreatePenaltyInfoWithAllFields() {
        PenaltyInfo penalty = PenaltyInfo.create(playerId, TeamSide.HOME, "Test reason", 3);

        assertThat(penalty.playerId()).isEqualTo(playerId);
        assertThat(penalty.teamSide()).isEqualTo(TeamSide.HOME);
        assertThat(penalty.reason()).isEqualTo("Test reason");
        assertThat(penalty.penaltyNumber()).isEqualTo(3);
        assertThat(penalty.timestamp()).isNotNull();
        assertThat(penalty.timestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("Should not trigger suspension for penalty number < 5")
    void shouldNotTriggerSuspensionBelowThreshold() {
        PenaltyInfo penalty1 = PenaltyInfo.create(playerId, TeamSide.HOME, "Test", 1);
        PenaltyInfo penalty4 = PenaltyInfo.create(playerId, TeamSide.HOME, "Test", 4);

        assertThat(penalty1.triggersSuspension()).isFalse();
        assertThat(penalty4.triggersSuspension()).isFalse();
    }

    @Test
    @DisplayName("Should trigger suspension for penalty number >= 5")
    void shouldTriggerSuspensionAtOrAboveThreshold() {
        PenaltyInfo penalty5 = PenaltyInfo.create(playerId, TeamSide.HOME, "Test", 5);
        PenaltyInfo penalty6 = PenaltyInfo.create(playerId, TeamSide.AWAY, "Test", 6);

        assertThat(penalty5.triggersSuspension()).isTrue();
        assertThat(penalty6.triggersSuspension()).isTrue();
    }

    @Test
    @DisplayName("Should work with both team sides")
    void shouldWorkWithBothTeamSides() {
        PenaltyInfo homePenalty = PenaltyInfo.create(playerId, TeamSide.HOME, "Home foul", 2);
        PenaltyInfo awayPenalty = PenaltyInfo.create(playerId, TeamSide.AWAY, "Away foul", 3);

        assertThat(homePenalty.teamSide()).isEqualTo(TeamSide.HOME);
        assertThat(awayPenalty.teamSide()).isEqualTo(TeamSide.AWAY);
    }
}
