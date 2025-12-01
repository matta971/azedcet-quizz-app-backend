package com.mindsoccer.scoring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AnswerValidationService Tests")
class AnswerValidationServiceTest {

    private AnswerValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new AnswerValidationService();
    }

    @Nested
    @DisplayName("validate Tests")
    class ValidateTests {

        @ParameterizedTest
        @DisplayName("Should reject blank answers")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void shouldRejectBlankAnswers(String answer) {
            var result = validationService.validate(answer, "correct", null);

            assertThat(result.isCorrect()).isFalse();
            assertThat(result.message()).contains("vide");
        }

        @Test
        @DisplayName("Should accept exact match")
        void shouldAcceptExactMatch() {
            var result = validationService.validate("Paris", "Paris", null);

            assertThat(result.isCorrect()).isTrue();
        }

        @ParameterizedTest
        @DisplayName("Should accept normalized matches")
        @CsvSource({
            "paris, Paris",
            "PARIS, paris",
            "café, cafe",
            "Éléphant, elephant",
            "  test  , test"
        })
        void shouldAcceptNormalizedMatches(String given, String expected) {
            var result = validationService.validate(given, expected, null);

            assertThat(result.isCorrect()).isTrue();
        }

        @Test
        @DisplayName("Should accept alternatives")
        void shouldAcceptAlternatives() {
            Set<String> alternatives = Set.of("NYC", "New York City", "Big Apple");

            assertThat(validationService.validate("NYC", "New York", alternatives).isCorrect()).isTrue();
            assertThat(validationService.validate("Big Apple", "New York", alternatives).isCorrect()).isTrue();
        }

        @Test
        @DisplayName("Should reject incorrect answers")
        void shouldRejectIncorrectAnswers() {
            var result = validationService.validate("London", "Paris", null);

            assertThat(result.isCorrect()).isFalse();
            assertThat(result.message()).contains("incorrecte");
        }
    }

    @Nested
    @DisplayName("isCorrect Tests")
    class IsCorrectTests {

        @Test
        @DisplayName("Simple version should work")
        void simpleVersionShouldWork() {
            assertThat(validationService.isCorrect("Paris", "Paris")).isTrue();
            assertThat(validationService.isCorrect("London", "Paris")).isFalse();
        }

        @Test
        @DisplayName("Version with alternatives should work")
        void versionWithAlternativesShouldWork() {
            Set<String> alternatives = Set.of("FR", "République française");

            assertThat(validationService.isCorrect("FR", "France", alternatives)).isTrue();
        }
    }

    @Nested
    @DisplayName("Fuzzy Matching Tests")
    class FuzzyMatchingTests {

        @Test
        @DisplayName("Should tolerate small typos in long answers")
        void shouldTolerateSmallTyposInLongAnswers() {
            // For answers longer than 10 chars, 1 error should be tolerated
            var result = validationService.validate("Christophe Colomb", "Christoph Colomb", null);

            // The validation uses distance based on length, should allow small mistakes
            assertThat(result.isCorrect()).isTrue();
        }

        @Test
        @DisplayName("Should be strict with short answers")
        void shouldBeStrictWithShortAnswers() {
            // For short answers (< 5 chars), no tolerance
            var result = validationService.validate("Pari", "Paris", null);

            // Short answers should be strict
            assertThat(result.isCorrect()).isFalse();
        }
    }

    @Nested
    @DisplayName("calculateSimilarity Tests")
    class CalculateSimilarityTests {

        @Test
        @DisplayName("Should return 1.0 for identical strings")
        void shouldReturnOneForIdentical() {
            double similarity = validationService.calculateSimilarity("test", "test");

            assertThat(similarity).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return 0.0 for null inputs")
        void shouldReturnZeroForNull() {
            assertThat(validationService.calculateSimilarity(null, "test")).isZero();
            assertThat(validationService.calculateSimilarity("test", null)).isZero();
        }

        @Test
        @DisplayName("Should return 0.0 for empty strings")
        void shouldReturnZeroForEmpty() {
            assertThat(validationService.calculateSimilarity("", "test")).isZero();
            assertThat(validationService.calculateSimilarity("test", "")).isZero();
        }

        @Test
        @DisplayName("Should return value between 0 and 1 for different strings")
        void shouldReturnValueBetweenZeroAndOne() {
            double similarity = validationService.calculateSimilarity("kitten", "sitting");

            assertThat(similarity).isBetween(0.0, 1.0);
        }

        @Test
        @DisplayName("Similar strings should have high similarity")
        void similarStringsShouldHaveHighSimilarity() {
            double similarity = validationService.calculateSimilarity("elephant", "elefant");

            assertThat(similarity).isGreaterThan(0.7);
        }
    }

    @Nested
    @DisplayName("ValidationResult Tests")
    class ValidationResultTests {

        @Test
        @DisplayName("Correct result should have proper values")
        void correctResultShouldHaveProperValues() {
            var result = AnswerValidationService.ValidationResult.correct();

            assertThat(result.correct()).isTrue();
            assertThat(result.isCorrect()).isTrue();
            assertThat(result.message()).isEqualTo("Correct");
            assertThat(result.similarity()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Incorrect result should have proper values")
        void incorrectResultShouldHaveProperValues() {
            var result = AnswerValidationService.ValidationResult.incorrect("Test message");

            assertThat(result.correct()).isFalse();
            assertThat(result.isCorrect()).isFalse();
            assertThat(result.message()).isEqualTo("Test message");
            assertThat(result.similarity()).isZero();
        }
    }
}
