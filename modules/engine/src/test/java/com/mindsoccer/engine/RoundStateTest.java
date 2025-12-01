package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.RoundType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RoundState Tests")
class RoundStateTest {

    @Nested
    @DisplayName("initial Factory Method Tests")
    class InitialTests {

        @Test
        @DisplayName("Should create initial state with correct defaults")
        void shouldCreateInitialStateWithDefaults() {
            RoundState state = RoundState.initial(RoundType.CASCADE);

            assertThat(state.type()).isEqualTo(RoundType.CASCADE);
            assertThat(state.phase()).isEqualTo(RoundState.Phase.WAITING);
            assertThat(state.questionIndex()).isZero();
            assertThat(state.currentQuestionId()).isNull();
            assertThat(state.activePlayerId()).isNull();
            assertThat(state.remainingTimeMs()).isZero();
            assertThat(state.finished()).isFalse();
            assertThat(state.extra()).isEmpty();
        }

        @Test
        @DisplayName("Should create initial state for different round types")
        void shouldCreateInitialStateForDifferentTypes() {
            for (RoundType type : RoundType.values()) {
                RoundState state = RoundState.initial(type);
                assertThat(state.type()).isEqualTo(type);
            }
        }
    }

    @Nested
    @DisplayName("withPhase Tests")
    class WithPhaseTests {

        @Test
        @DisplayName("Should create new state with updated phase")
        void shouldCreateNewStateWithUpdatedPhase() {
            RoundState initial = RoundState.initial(RoundType.CASCADE);
            RoundState updated = initial.withPhase(RoundState.Phase.QUESTION_SHOWN);

            assertThat(updated.phase()).isEqualTo(RoundState.Phase.QUESTION_SHOWN);
            assertThat(updated.type()).isEqualTo(RoundType.CASCADE);
            assertThat(initial.phase()).isEqualTo(RoundState.Phase.WAITING); // Original unchanged
        }

        @Test
        @DisplayName("Should preserve all other fields when changing phase")
        void shouldPreserveOtherFields() {
            UUID questionId = UUID.randomUUID();
            UUID playerId = UUID.randomUUID();
            RoundState original = new RoundState(
                    RoundType.SMASH_A,
                    RoundState.Phase.ANNOUNCE,
                    3,
                    questionId,
                    playerId,
                    5000L,
                    false,
                    Map.of("key", "value")
            );

            RoundState updated = original.withPhase(RoundState.Phase.ANSWER_WINDOW);

            assertThat(updated.phase()).isEqualTo(RoundState.Phase.ANSWER_WINDOW);
            assertThat(updated.type()).isEqualTo(RoundType.SMASH_A);
            assertThat(updated.questionIndex()).isEqualTo(3);
            assertThat(updated.currentQuestionId()).isEqualTo(questionId);
            assertThat(updated.activePlayerId()).isEqualTo(playerId);
            assertThat(updated.remainingTimeMs()).isEqualTo(5000L);
            assertThat(updated.finished()).isFalse();
            assertThat(updated.extra()).containsEntry("key", "value");
        }
    }

    @Nested
    @DisplayName("completed Tests")
    class CompletedTests {

        @Test
        @DisplayName("Should create completed state")
        void shouldCreateCompletedState() {
            RoundState initial = RoundState.initial(RoundType.DUEL);
            RoundState completed = initial.completed();

            assertThat(completed.phase()).isEqualTo(RoundState.Phase.COMPLETED);
            assertThat(completed.finished()).isTrue();
            assertThat(completed.remainingTimeMs()).isZero();
        }

        @Test
        @DisplayName("Should preserve type and extra when completing")
        void shouldPreserveTypeAndExtra() {
            RoundState original = new RoundState(
                    RoundType.MARATHON,
                    RoundState.Phase.QUESTION_SHOWN,
                    5,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    10000L,
                    false,
                    Map.of("score", 100)
            );

            RoundState completed = original.completed();

            assertThat(completed.type()).isEqualTo(RoundType.MARATHON);
            assertThat(completed.questionIndex()).isEqualTo(5);
            assertThat(completed.extra()).containsEntry("score", 100);
        }
    }

    @Nested
    @DisplayName("Phase Enum Tests")
    class PhaseEnumTests {

        @Test
        @DisplayName("Should have all expected phases")
        void shouldHaveAllExpectedPhases() {
            RoundState.Phase[] phases = RoundState.Phase.values();

            assertThat(phases).hasSize(7);
            assertThat(phases).contains(
                    RoundState.Phase.WAITING,
                    RoundState.Phase.ANNOUNCE,
                    RoundState.Phase.QUESTION_SHOWN,
                    RoundState.Phase.ANSWER_WINDOW,
                    RoundState.Phase.VALIDATING,
                    RoundState.Phase.TRANSITION,
                    RoundState.Phase.COMPLETED
            );
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Equal states should be equal")
        void equalStatesShouldBeEqual() {
            RoundState state1 = RoundState.initial(RoundType.CASCADE);
            RoundState state2 = RoundState.initial(RoundType.CASCADE);

            assertThat(state1).isEqualTo(state2);
            assertThat(state1.hashCode()).isEqualTo(state2.hashCode());
        }

        @Test
        @DisplayName("Different states should not be equal")
        void differentStatesShouldNotBeEqual() {
            RoundState state1 = RoundState.initial(RoundType.CASCADE);
            RoundState state2 = RoundState.initial(RoundType.SMASH_A);

            assertThat(state1).isNotEqualTo(state2);
        }
    }
}
