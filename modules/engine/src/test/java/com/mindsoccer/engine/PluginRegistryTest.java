package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.RoundType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PluginRegistry Tests")
class PluginRegistryTest {

    private PluginRegistry registry;
    private RulePlugin cascadePlugin;
    private RulePlugin smashPlugin;

    @BeforeEach
    void setUp() {
        // Create mock plugins
        cascadePlugin = new TestPlugin(RoundType.CASCADE);
        smashPlugin = new TestPlugin(RoundType.SMASH);

        registry = new PluginRegistry(List.of(cascadePlugin, smashPlugin));
    }

    @Nested
    @DisplayName("getPlugin Tests")
    class GetPluginTests {

        @Test
        @DisplayName("Should return plugin for registered type")
        void shouldReturnPluginForRegisteredType() {
            var plugin = registry.getPlugin(RoundType.CASCADE);

            assertThat(plugin).isPresent();
            assertThat(plugin.get().type()).isEqualTo(RoundType.CASCADE);
        }

        @Test
        @DisplayName("Should return empty for unregistered type")
        void shouldReturnEmptyForUnregisteredType() {
            var plugin = registry.getPlugin(RoundType.DUEL);

            assertThat(plugin).isEmpty();
        }
    }

    @Nested
    @DisplayName("getPluginOrThrow Tests")
    class GetPluginOrThrowTests {

        @Test
        @DisplayName("Should return plugin for registered type")
        void shouldReturnPluginForRegisteredType() {
            RulePlugin plugin = registry.getPluginOrThrow(RoundType.SMASH);

            assertThat(plugin.type()).isEqualTo(RoundType.SMASH);
        }

        @Test
        @DisplayName("Should throw for unregistered type")
        void shouldThrowForUnregisteredType() {
            assertThatThrownBy(() -> registry.getPluginOrThrow(RoundType.MARATHON))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No plugin registered");
        }
    }

    @Nested
    @DisplayName("hasPlugin Tests")
    class HasPluginTests {

        @Test
        @DisplayName("Should return true for registered type")
        void shouldReturnTrueForRegisteredType() {
            assertThat(registry.hasPlugin(RoundType.CASCADE)).isTrue();
            assertThat(registry.hasPlugin(RoundType.SMASH)).isTrue();
        }

        @Test
        @DisplayName("Should return false for unregistered type")
        void shouldReturnFalseForUnregisteredType() {
            assertThat(registry.hasPlugin(RoundType.JACKPOT)).isFalse();
        }
    }

    @Nested
    @DisplayName("getSupportedTypes Tests")
    class GetSupportedTypesTests {

        @Test
        @DisplayName("Should return all registered types")
        void shouldReturnAllRegisteredTypes() {
            List<RoundType> types = registry.getSupportedTypes();

            assertThat(types).hasSize(2);
            assertThat(types).contains(RoundType.CASCADE, RoundType.SMASH);
        }

        @Test
        @DisplayName("Should return immutable list")
        void shouldReturnImmutableList() {
            List<RoundType> types = registry.getSupportedTypes();

            assertThatThrownBy(() -> types.add(RoundType.DUEL))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("getPluginCount Tests")
    class GetPluginCountTests {

        @Test
        @DisplayName("Should return correct count")
        void shouldReturnCorrectCount() {
            assertThat(registry.getPluginCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Empty registry should have zero count")
        void emptyRegistryShouldHaveZeroCount() {
            PluginRegistry emptyRegistry = new PluginRegistry(List.of());

            assertThat(emptyRegistry.getPluginCount()).isZero();
        }
    }

    /**
     * Simple test plugin for testing registry functionality.
     */
    static class TestPlugin implements RulePlugin {
        private final RoundType type;

        TestPlugin(RoundType type) {
            this.type = type;
        }

        @Override
        public RoundType type() {
            return type;
        }

        @Override
        public RoundState init(MatchContext ctx) {
            return RoundState.initial(type);
        }

        @Override
        public RoundState onTick(MatchContext ctx, Duration dt) {
            return ctx.currentRoundState();
        }

        @Override
        public RoundState onAnswer(MatchContext ctx, AnswerPayload payload) {
            return ctx.currentRoundState();
        }

        @Override
        public void applyScoring(MatchContext ctx) {
            // No-op for tests
        }
    }
}
