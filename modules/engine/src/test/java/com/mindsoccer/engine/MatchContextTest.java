package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MatchContext Tests")
class MatchContextTest {

    private UUID matchId;
    private UUID player1Id;
    private UUID player2Id;
    private MatchContext context;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();

        context = new MatchContext(
                matchId,
                MatchStatus.IN_PROGRESS,
                RoundType.CASCADE,
                2,
                Instant.now().minusSeconds(30),
                Map.of(TeamSide.A, 50, TeamSide.B, 40),
                Map.of(TeamSide.A, List.of(player1Id), TeamSide.B, List.of(player2Id)),
                Map.of(player1Id, 2, player2Id, 0),
                Map.of(player1Id, false, player2Id, false),
                TeamSide.A,
                RoundState.initial(RoundType.CASCADE)
        );
    }

    @Nested
    @DisplayName("Record Field Tests")
    class RecordFieldTests {

        @Test
        @DisplayName("Should have correct field values")
        void shouldHaveCorrectFieldValues() {
            assertThat(context.matchId()).isEqualTo(matchId);
            assertThat(context.status()).isEqualTo(MatchStatus.IN_PROGRESS);
            assertThat(context.currentRoundType()).isEqualTo(RoundType.CASCADE);
            assertThat(context.roundIndex()).isEqualTo(2);
            assertThat(context.scores()).containsEntry(TeamSide.A, 50);
            assertThat(context.scores()).containsEntry(TeamSide.B, 40);
            assertThat(context.leadingTeam()).isEqualTo(TeamSide.A);
        }

        @Test
        @DisplayName("Should have correct player lists")
        void shouldHaveCorrectPlayerLists() {
            assertThat(context.players().get(TeamSide.A)).contains(player1Id);
            assertThat(context.players().get(TeamSide.B)).contains(player2Id);
        }
    }

    @Nested
    @DisplayName("isPlayerSuspended Tests")
    class IsPlayerSuspendedTests {

        @Test
        @DisplayName("Should return false for non-suspended player")
        void shouldReturnFalseForNonSuspendedPlayer() {
            assertThat(context.isPlayerSuspended(player1Id)).isFalse();
            assertThat(context.isPlayerSuspended(player2Id)).isFalse();
        }

        @Test
        @DisplayName("Should return true for suspended player")
        void shouldReturnTrueForSuspendedPlayer() {
            MatchContext contextWithSuspension = new MatchContext(
                    matchId,
                    MatchStatus.IN_PROGRESS,
                    RoundType.CASCADE,
                    2,
                    Instant.now(),
                    Map.of(TeamSide.A, 50, TeamSide.B, 40),
                    Map.of(TeamSide.A, List.of(player1Id), TeamSide.B, List.of(player2Id)),
                    Map.of(player1Id, 5),
                    Map.of(player1Id, true),
                    TeamSide.A,
                    RoundState.initial(RoundType.CASCADE)
            );

            assertThat(contextWithSuspension.isPlayerSuspended(player1Id)).isTrue();
        }

        @Test
        @DisplayName("Should return false for unknown player")
        void shouldReturnFalseForUnknownPlayer() {
            UUID unknownPlayer = UUID.randomUUID();
            assertThat(context.isPlayerSuspended(unknownPlayer)).isFalse();
        }
    }

    @Nested
    @DisplayName("getPlayerPenaltyCount Tests")
    class GetPlayerPenaltyCountTests {

        @Test
        @DisplayName("Should return correct penalty count")
        void shouldReturnCorrectPenaltyCount() {
            assertThat(context.getPlayerPenaltyCount(player1Id)).isEqualTo(2);
            assertThat(context.getPlayerPenaltyCount(player2Id)).isZero();
        }

        @Test
        @DisplayName("Should return zero for unknown player")
        void shouldReturnZeroForUnknownPlayer() {
            UUID unknownPlayer = UUID.randomUUID();
            assertThat(context.getPlayerPenaltyCount(unknownPlayer)).isZero();
        }
    }

    @Nested
    @DisplayName("elapsedMillis Tests")
    class ElapsedMillisTests {

        @Test
        @DisplayName("Should return positive elapsed time")
        void shouldReturnPositiveElapsedTime() {
            // Context was created with roundStartedAt 30 seconds ago
            assertThat(context.elapsedMillis()).isGreaterThan(0);
            assertThat(context.elapsedMillis()).isGreaterThanOrEqualTo(30000);
        }

        @Test
        @DisplayName("Should calculate elapsed time correctly")
        void shouldCalculateElapsedTimeCorrectly() throws InterruptedException {
            Instant start = Instant.now();
            MatchContext freshContext = new MatchContext(
                    matchId,
                    MatchStatus.IN_PROGRESS,
                    RoundType.SMASH_A,
                    1,
                    start,
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    null,
                    RoundState.initial(RoundType.SMASH_A)
            );

            Thread.sleep(100);

            assertThat(freshContext.elapsedMillis()).isGreaterThanOrEqualTo(100);
            assertThat(freshContext.elapsedMillis()).isLessThan(500);
        }
    }

    @Nested
    @DisplayName("currentRoundState Tests")
    class CurrentRoundStateTests {

        @Test
        @DisplayName("Should return current round state")
        void shouldReturnCurrentRoundState() {
            RoundState state = context.currentRoundState();

            assertThat(state).isNotNull();
            assertThat(state.type()).isEqualTo(RoundType.CASCADE);
        }
    }
}
