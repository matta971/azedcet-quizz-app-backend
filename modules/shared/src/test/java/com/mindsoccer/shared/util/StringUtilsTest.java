package com.mindsoccer.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StringUtils Tests")
class StringUtilsTest {

    @Nested
    @DisplayName("isBlank/isNotBlank Tests")
    class BlankTests {

        @ParameterizedTest
        @DisplayName("Should return true for blank strings")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        void shouldReturnTrueForBlank(String value) {
            assertThat(StringUtils.isBlank(value)).isTrue();
            assertThat(StringUtils.isNotBlank(value)).isFalse();
        }

        @ParameterizedTest
        @DisplayName("Should return false for non-blank strings")
        @ValueSource(strings = {"a", " a ", "hello"})
        void shouldReturnFalseForNonBlank(String value) {
            assertThat(StringUtils.isBlank(value)).isFalse();
            assertThat(StringUtils.isNotBlank(value)).isTrue();
        }
    }

    @Nested
    @DisplayName("normalizeAnswer Tests")
    class NormalizeAnswerTests {

        @Test
        @DisplayName("Should return empty string for blank input")
        void shouldReturnEmptyForBlank() {
            assertThat(StringUtils.normalizeAnswer(null)).isEmpty();
            assertThat(StringUtils.normalizeAnswer("")).isEmpty();
            assertThat(StringUtils.normalizeAnswer("   ")).isEmpty();
        }

        @Test
        @DisplayName("Should remove accents")
        void shouldRemoveAccents() {
            assertThat(StringUtils.normalizeAnswer("café")).isEqualTo("cafe");
            assertThat(StringUtils.normalizeAnswer("résumé")).isEqualTo("resume");
            assertThat(StringUtils.normalizeAnswer("naïve")).isEqualTo("naive");
            assertThat(StringUtils.normalizeAnswer("Éléphant")).isEqualTo("elephant");
        }

        @Test
        @DisplayName("Should convert to lowercase")
        void shouldConvertToLowercase() {
            assertThat(StringUtils.normalizeAnswer("HELLO")).isEqualTo("hello");
            assertThat(StringUtils.normalizeAnswer("World")).isEqualTo("world");
        }

        @Test
        @DisplayName("Should remove special characters")
        void shouldRemoveSpecialCharacters() {
            assertThat(StringUtils.normalizeAnswer("hello!")).isEqualTo("hello");
            assertThat(StringUtils.normalizeAnswer("test@123")).isEqualTo("test 123");
            assertThat(StringUtils.normalizeAnswer("a.b.c")).isEqualTo("a b c");
        }

        @Test
        @DisplayName("Should normalize whitespace")
        void shouldNormalizeWhitespace() {
            assertThat(StringUtils.normalizeAnswer("hello   world")).isEqualTo("hello world");
            assertThat(StringUtils.normalizeAnswer("  test  ")).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("answersMatch Tests")
    class AnswersMatchTests {

        @Test
        @DisplayName("Should return false for blank inputs")
        void shouldReturnFalseForBlank() {
            assertThat(StringUtils.answersMatch(null, "test")).isFalse();
            assertThat(StringUtils.answersMatch("test", null)).isFalse();
            assertThat(StringUtils.answersMatch("", "test")).isFalse();
        }

        @ParameterizedTest
        @DisplayName("Should match equivalent answers")
        @CsvSource({
            "Paris, Paris",
            "paris, PARIS",
            "café, cafe",
            "Éléphant, elephant",
            "New York, new york",
            "  test  , test"
        })
        void shouldMatchEquivalentAnswers(String given, String expected) {
            assertThat(StringUtils.answersMatch(given, expected)).isTrue();
        }

        @Test
        @DisplayName("Should not match different answers")
        void shouldNotMatchDifferentAnswers() {
            assertThat(StringUtils.answersMatch("Paris", "London")).isFalse();
            assertThat(StringUtils.answersMatch("chat", "chien")).isFalse();
        }
    }

    @Nested
    @DisplayName("answersMatchFuzzy Tests")
    class AnswersMatchFuzzyTests {

        @Test
        @DisplayName("Should match with small typos")
        void shouldMatchWithSmallTypos() {
            assertThat(StringUtils.answersMatchFuzzy("Pris", "Paris", 1)).isTrue();
            assertThat(StringUtils.answersMatchFuzzy("elefant", "elephant", 2)).isTrue();
        }

        @Test
        @DisplayName("Should not match beyond max distance")
        void shouldNotMatchBeyondMaxDistance() {
            assertThat(StringUtils.answersMatchFuzzy("xyz", "Paris", 1)).isFalse();
        }

        @Test
        @DisplayName("Should return false for blank inputs")
        void shouldReturnFalseForBlank() {
            assertThat(StringUtils.answersMatchFuzzy(null, "test", 2)).isFalse();
            assertThat(StringUtils.answersMatchFuzzy("test", null, 2)).isFalse();
        }
    }

    @Nested
    @DisplayName("levenshteinDistance Tests")
    class LevenshteinDistanceTests {

        @ParameterizedTest
        @DisplayName("Should calculate correct distances")
        @CsvSource({
            "kitten, sitting, 3",
            "flaw, lawn, 2",
            "abc, abc, 0",
            "'', abc, 3",
            "a, '', 1"
        })
        void shouldCalculateCorrectDistances(String s1, String s2, int expected) {
            assertThat(StringUtils.levenshteinDistance(s1, s2)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("truncate Tests")
    class TruncateTests {

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(StringUtils.truncate(null, 10)).isNull();
        }

        @Test
        @DisplayName("Should not truncate short strings")
        void shouldNotTruncateShortStrings() {
            assertThat(StringUtils.truncate("hello", 10)).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should truncate long strings")
        void shouldTruncateLongStrings() {
            assertThat(StringUtils.truncate("hello world", 5)).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("slugify Tests")
    class SlugifyTests {

        @Test
        @DisplayName("Should return empty for blank input")
        void shouldReturnEmptyForBlank() {
            assertThat(StringUtils.slugify(null)).isEmpty();
            assertThat(StringUtils.slugify("")).isEmpty();
        }

        @Test
        @DisplayName("Should create valid slugs")
        void shouldCreateValidSlugs() {
            assertThat(StringUtils.slugify("Hello World")).isEqualTo("hello-world");
            assertThat(StringUtils.slugify("Café Crème")).isEqualTo("cafe-creme");
            assertThat(StringUtils.slugify("Test!@#123")).isEqualTo("test-123");
        }
    }
}
