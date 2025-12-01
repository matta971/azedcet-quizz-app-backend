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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CascadePlugin Tests")
class CascadePluginTest {

    @Mock
    private ScoringService scoringService;

    @Mock
    private AnswerValidationService validationService;

    private CascadePlugin plugin;
    private MatchContext context;
    private final UUID matchId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        plugin = new CascadePlugin(scoringService, validationService);
    }

    private MatchContext createContext(RoundState state) {
        return new MatchContext(
                matchId,
                MatchStatus.IN_PROGRESS,
                RoundType.CASCADE,
                0,
                Instant.now(),
                Map.of(TeamSide.A, 0, TeamSide.B, 0),
                Map.of(TeamSide.A, List.of(playerId), TeamSide.B, List.of(UUID.randomUUID())),
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
        @DisplayName("Should return CASCADE type")
        void shouldReturnCascadeType() {
            assertThat(plugin.type()).isEqualTo(RoundType.CASCADE);
        }
    }

    @Nested
    @DisplayName("init Tests")
    class InitTests {

        @Test
        @DisplayName("Should initialize with correct state")
        void shouldInitializeWithCorrectState() {
            MatchContext ctx = createContext(RoundState.initial(RoundType.CASCADE));

            RoundState state = plugin.init(ctx);

            assertThat(state.type()).isEqualTo(RoundType.CASCADE);
            assertThat(state.extra()).containsKey("consecutive");
            assertThat(state.extra()).containsKey("currentTeam");
            assertThat(state.extra()).containsKey("lastCorrect");
            assertThat(state.extra().get("consecutive")).isEqualTo(0);
            assertThat(state.extra().get("currentTeam")).isEqualTo(TeamSide.A);
        }
    }

    @Nested
    @DisplayName("onTick Tests")
    class OnTickTests {

        @Test
        @DisplayName("Should not change finished state")
        void shouldNotChangeFinishedState() {
            RoundState finishedState = RoundState.initial(RoundType.CASCADE).completed();
            MatchContext ctx = createContext(finishedState);

            RoundState result = plugin.onTick(ctx, Duration.ofMillis(100));

            assertThat(result.finished()).isTrue();
        }

        @Test
        @DisplayName("Should transition from TRANSITION to QUESTION_SHOWN")
        void shouldTransitionToQuestionShown() {
            RoundState state = RoundState.initial(RoundType.CASCADE)
                    .withPhase(RoundState.Phase.TRANSITION);
            MatchContext ctx = createContext(state);

            RoundState result = plugin.onTick(ctx, Duration.ofMillis(100));

            assertThat(result.phase()).isEqualTo(RoundState.Phase.QUESTION_SHOWN);
        }
    }

    @Nested
    @DisplayName("onAnswer Tests")
    class OnAnswerTests {

        @Test
        @DisplayName("Should not process answer when finished")
        void shouldNotProcessAnswerWhenFinished() {
            RoundState finishedState = RoundState.initial(RoundType.CASCADE).completed();
            MatchContext ctx = createContext(finishedState);

            AnswerPayload payload = createPayload(TeamSide.A, "Paris");

            RoundState result = plugin.onAnswer(ctx, payload);

            assertThat(result.finished()).isTrue();
        }

        @Test
        @DisplayName("Should ignore answer from wrong team")
        void shouldIgnoreAnswerFromWrongTeam() {
            RoundState state = plugin.init(createContext(RoundState.initial(RoundType.CASCADE)));
            MatchContext ctx = createContext(state);

            // Current team is A, but answer is from B
            AnswerPayload payload = createPayload(TeamSide.B, "Paris");

            RoundState result = plugin.onAnswer(ctx, payload);

            // State should be unchanged
            assertThat(result.extra().get("currentTeam")).isEqualTo(TeamSide.A);
        }

        @Test
        @DisplayName("Should increment consecutive on correct answer")
        void shouldIncrementConsecutiveOnCorrectAnswer() {
            when(validationService.isCorrect(eq("Paris"), any(), any())).thenReturn(true);

            RoundState state = plugin.init(createContext(RoundState.initial(RoundType.CASCADE)));
            MatchContext ctx = createContext(state);

            AnswerPayload payload = createPayload(TeamSide.A, "Paris");

            RoundState result = plugin.onAnswer(ctx, payload);

            assertThat(result.extra().get("consecutive")).isEqualTo(1);
            assertThat(result.extra().get("lastCorrect")).isEqualTo(true);
        }

        @Test
        @DisplayName("Should reset consecutive on wrong answer")
        void shouldResetConsecutiveOnWrongAnswer() {
            when(validationService.isCorrect(any(), any(), any())).thenReturn(false);

            // Start with consecutive = 2
            RoundState state = new RoundState(
                    RoundType.CASCADE,
                    RoundState.Phase.QUESTION_SHOWN,
                    0, null, null, 0L, false,
                    Map.of("consecutive", 2, "currentTeam", TeamSide.A, "lastCorrect", true)
            );
            MatchContext ctx = createContext(state);

            AnswerPayload payload = createPayload(TeamSide.A, "Wrong");

            RoundState result = plugin.onAnswer(ctx, payload);

            assertThat(result.extra().get("consecutive")).isEqualTo(0);
            assertThat(result.extra().get("lastCorrect")).isEqualTo(false);
        }

        @Test
        @DisplayName("Should switch team after answer")
        void shouldSwitchTeamAfterAnswer() {
            when(validationService.isCorrect(any(), any(), any())).thenReturn(true);

            RoundState state = plugin.init(createContext(RoundState.initial(RoundType.CASCADE)));
            MatchContext ctx = createContext(state);

            AnswerPayload payload = createPayload(TeamSide.A, "Paris");

            RoundState result = plugin.onAnswer(ctx, payload);

            assertThat(result.extra().get("currentTeam")).isEqualTo(TeamSide.B);
        }
    }

    @Nested
    @DisplayName("calculatePoints Tests")
    class CalculatePointsTests {

        @Test
        @DisplayName("Should return 0 for zero consecutive")
        void shouldReturnZeroForZeroConsecutive() {
            assertThat(plugin.calculatePoints(0)).isZero();
        }

        @Test
        @DisplayName("Should return 10 for first correct")
        void shouldReturn10ForFirst() {
            assertThat(plugin.calculatePoints(1)).isEqualTo(10);
        }

        @Test
        @DisplayName("Should calculate progressive points")
        void shouldCalculateProgressivePoints() {
            assertThat(plugin.calculatePoints(1)).isEqualTo(10);
            assertThat(plugin.calculatePoints(2)).isEqualTo(15);
            assertThat(plugin.calculatePoints(3)).isEqualTo(20);
            assertThat(plugin.calculatePoints(4)).isEqualTo(25);
            assertThat(plugin.calculatePoints(5)).isEqualTo(30);
        }
    }

    private AnswerPayload createPayload(TeamSide team, String answer) {
        return new AnswerPayload(
                matchId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                playerId,
                team,
                answer,
                Instant.now(),
                System.currentTimeMillis(),
                "key-" + UUID.randomUUID()
        );
    }
}
