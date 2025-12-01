package com.mindsoccer.engine.plugin;

import com.mindsoccer.engine.AnswerPayload;
import com.mindsoccer.engine.MatchContext;
import com.mindsoccer.engine.RoundState;
import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.service.AnswerValidationService;
import com.mindsoccer.scoring.service.ScoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdentificationPlugin Tests")
class IdentificationPluginTest {

    @Mock
    private ScoringService scoringService;

    @Mock
    private AnswerValidationService validationService;

    private IdentificationPlugin plugin;
    private final UUID matchId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        plugin = new IdentificationPlugin(scoringService, validationService);
    }

    private MatchContext createContext(RoundState state) {
        return new MatchContext(
                matchId,
                MatchStatus.IN_PROGRESS,
                RoundType.IDENTIFICATION,
                0,
                Instant.now(),
                Map.of(TeamSide.HOME, 0, TeamSide.AWAY, 0),
                Map.of(TeamSide.HOME, List.of(playerId), TeamSide.AWAY, List.of(UUID.randomUUID())),
                Map.of(),
                Map.of(),
                null,
                state
        );
    }

    @Nested
    @DisplayName("type Tests")
    class TypeTests {

        @Test
        @DisplayName("Should return IDENTIFICATION type")
        void shouldReturnIdentificationType() {
            assertThat(plugin.type()).isEqualTo(RoundType.IDENTIFICATION);
        }
    }

    @Nested
    @DisplayName("init Tests")
    class InitTests {

        @Test
        @DisplayName("Should initialize with correct state")
        void shouldInitializeWithCorrectState() {
            MatchContext ctx = createContext(RoundState.initial(RoundType.IDENTIFICATION));

            RoundState state = plugin.init(ctx);

            assertThat(state.type()).isEqualTo(RoundType.IDENTIFICATION);
            assertThat(state.extra()).containsKey("phase");
            assertThat(state.extra()).containsKey("currentClue");
            assertThat(state.extra()).containsKey("buzzerWinner");
            assertThat(state.extra()).containsKey("eliminatedPlayers");
            assertThat(state.extra()).containsKey("answered");

            assertThat(state.extra().get("phase")).isEqualTo("CLUE_DISPLAY");
            assertThat(state.extra().get("currentClue")).isEqualTo(1);
            assertThat(state.extra().get("answered")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("onTick Tests")
    class OnTickTests {

        @Test
        @DisplayName("Should not change finished state")
        void shouldNotChangeFinishedState() {
            RoundState finishedState = RoundState.initial(RoundType.IDENTIFICATION).completed();
            MatchContext ctx = createContext(finishedState);

            RoundState result = plugin.onTick(ctx, Duration.ofMillis(100));

            assertThat(result.finished()).isTrue();
        }

        @Test
        @DisplayName("Should decrement time")
        void shouldDecrementTime() {
            RoundState state = new RoundState(
                    RoundType.IDENTIFICATION,
                    RoundState.Phase.ANSWER_WINDOW,
                    0, null, null, 5000L, false,
                    Map.of("phase", "BUZZER", "currentClue", 1, "eliminatedPlayers", new ArrayList<String>())
            );
            MatchContext ctx = createContext(state);

            RoundState result = plugin.onTick(ctx, Duration.ofMillis(1000));

            assertThat(result.remainingTimeMs()).isEqualTo(4000L);
        }
    }

    @Nested
    @DisplayName("onAnswer Tests")
    class OnAnswerTests {

        @Test
        @DisplayName("Should not process answer when finished")
        void shouldNotProcessAnswerWhenFinished() {
            RoundState finishedState = RoundState.initial(RoundType.IDENTIFICATION).completed();
            MatchContext ctx = createContext(finishedState);

            AnswerPayload payload = createPayload("Paris");

            RoundState result = plugin.onAnswer(ctx, payload);

            assertThat(result.finished()).isTrue();
        }

        @Test
        @DisplayName("Should ignore eliminated player")
        void shouldIgnoreEliminatedPlayer() {
            List<String> eliminated = new ArrayList<>();
            eliminated.add(playerId.toString());

            RoundState state = new RoundState(
                    RoundType.IDENTIFICATION,
                    RoundState.Phase.ANSWER_WINDOW,
                    0, null, null, 5000L, false,
                    Map.of("phase", "BUZZER", "currentClue", 1,
                            "eliminatedPlayers", eliminated, "buzzerWinner", "")
            );
            MatchContext ctx = createContext(state);

            AnswerPayload payload = createPayload("Paris");

            RoundState result = plugin.onAnswer(ctx, payload);

            // State should be unchanged
            assertThat(result.extra().get("phase")).isEqualTo("BUZZER");
        }

        @Test
        @DisplayName("Should handle buzzer in BUZZER phase")
        void shouldHandleBuzzerInBuzzerPhase() {
            RoundState state = new RoundState(
                    RoundType.IDENTIFICATION,
                    RoundState.Phase.ANSWER_WINDOW,
                    0, null, null, 5000L, false,
                    Map.of("phase", "BUZZER", "currentClue", 1,
                            "eliminatedPlayers", new ArrayList<String>(), "buzzerWinner", "")
            );
            MatchContext ctx = createContext(state);

            AnswerPayload payload = createPayload("Paris");

            RoundState result = plugin.onAnswer(ctx, payload);

            assertThat(result.extra().get("phase")).isEqualTo("ANSWER");
            assertThat(result.extra().get("buzzerWinner")).isEqualTo(playerId.toString());
        }

        @Test
        @DisplayName("Should complete on correct answer")
        void shouldCompleteOnCorrectAnswer() {
            when(validationService.isCorrect(any(), any(), any())).thenReturn(true);

            RoundState state = new RoundState(
                    RoundType.IDENTIFICATION,
                    RoundState.Phase.ANSWER_WINDOW,
                    0, null, null, 5000L, false,
                    Map.of("phase", "ANSWER", "currentClue", 1,
                            "eliminatedPlayers", new ArrayList<String>(),
                            "buzzerWinner", playerId.toString(),
                            "expectedAnswer", "Paris")
            );
            MatchContext ctx = createContext(state);

            AnswerPayload payload = createPayload("Paris");

            RoundState result = plugin.onAnswer(ctx, payload);

            assertThat(result.finished()).isTrue();
            assertThat(result.extra().get("answered")).isEqualTo(true);
            assertThat(result.extra().get("winnerPlayerId")).isEqualTo(playerId.toString());
        }

        @Test
        @DisplayName("Should eliminate player on wrong answer")
        void shouldEliminatePlayerOnWrongAnswer() {
            when(validationService.isCorrect(any(), any(), any())).thenReturn(false);

            RoundState state = new RoundState(
                    RoundType.IDENTIFICATION,
                    RoundState.Phase.ANSWER_WINDOW,
                    0, null, null, 5000L, false,
                    Map.of("phase", "ANSWER", "currentClue", 1,
                            "eliminatedPlayers", new ArrayList<String>(),
                            "buzzerWinner", playerId.toString(),
                            "expectedAnswer", "Paris")
            );
            MatchContext ctx = createContext(state);

            AnswerPayload payload = createPayload("Wrong");

            RoundState result = plugin.onAnswer(ctx, payload);

            @SuppressWarnings("unchecked")
            List<String> eliminated = (List<String>) result.extra().get("eliminatedPlayers");
            assertThat(eliminated).contains(playerId.toString());
            assertThat(result.extra().get("phase")).isEqualTo("BUZZER");
        }
    }

    @Nested
    @DisplayName("Clue Points Tests")
    class CluePointsTests {

        @Test
        @DisplayName("Should have 5 clues")
        void shouldHave5Clues() {
            assertThat(plugin.getTotalClues()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should have decreasing points")
        void shouldHaveDecreasingPoints() {
            int[] points = plugin.getCluePoints();

            assertThat(points).hasSize(5);
            assertThat(points[0]).isGreaterThan(points[1]);
            assertThat(points[1]).isGreaterThan(points[2]);
            assertThat(points[2]).isGreaterThan(points[3]);
            assertThat(points[3]).isGreaterThan(points[4]);
        }

        @Test
        @DisplayName("Should have correct point values")
        void shouldHaveCorrectPointValues() {
            int[] points = plugin.getCluePoints();

            assertThat(points).containsExactly(25, 20, 15, 10, 5);
        }
    }

    private AnswerPayload createPayload(String answer) {
        return new AnswerPayload(
                matchId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                playerId,
                TeamSide.HOME,
                answer,
                Instant.now(),
                System.currentTimeMillis(),
                "key-" + UUID.randomUUID()
        );
    }
}
