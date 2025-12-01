package com.mindsoccer.content.entity;

import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.RoundType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("QuestionEntity Tests")
class QuestionEntityTest {

    private QuestionEntity question;

    @BeforeEach
    void setUp() {
        question = new QuestionEntity("Quelle est la capitale de la France ?", "Paris");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create question with text and answer")
        void shouldCreateQuestionWithTextAndAnswer() {
            assertThat(question.getTextFr()).isEqualTo("Quelle est la capitale de la France ?");
            assertThat(question.getAnswer()).isEqualTo("Paris");
        }

        @Test
        @DisplayName("Should have default values")
        void shouldHaveDefaultValues() {
            assertThat(question.getQuestionFormat()).isEqualTo(QuestionFormat.TEXT);
            assertThat(question.getDifficulty()).isEqualTo(Difficulty.MEDIUM);
            assertThat(question.isActive()).isTrue();
            assertThat(question.getUsageCount()).isZero();
            assertThat(question.getSuccessCount()).isZero();
        }

        @Test
        @DisplayName("Should have empty alternative answers by default")
        void shouldHaveEmptyAlternativeAnswers() {
            assertThat(question.getAlternativeAnswers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Alternative Answers Tests")
    class AlternativeAnswersTests {

        @Test
        @DisplayName("Should add alternative answer")
        void shouldAddAlternativeAnswer() {
            question.addAlternativeAnswer("paris");
            question.addAlternativeAnswer("PARIS");

            assertThat(question.getAlternativeAnswers()).hasSize(2);
            assertThat(question.getAlternativeAnswers()).contains("paris", "PARIS");
        }
    }

    @Nested
    @DisplayName("Localization Tests")
    class LocalizationTests {

        @BeforeEach
        void setUpLocalizations() {
            question.setTextEn("What is the capital of France?");
            question.setTextHt("Ki kapital Lafrans?");
            question.setTextFon("Akɔ́ntɛ Fránsi tɔn ɖé?");
        }

        @Test
        @DisplayName("Should return French text for fr locale")
        void shouldReturnFrenchText() {
            assertThat(question.getLocalizedText("fr")).isEqualTo("Quelle est la capitale de la France ?");
        }

        @Test
        @DisplayName("Should return English text for en locale")
        void shouldReturnEnglishText() {
            assertThat(question.getLocalizedText("en")).isEqualTo("What is the capital of France?");
        }

        @Test
        @DisplayName("Should return Haitian text for ht locale")
        void shouldReturnHaitianText() {
            assertThat(question.getLocalizedText("ht")).isEqualTo("Ki kapital Lafrans?");
        }

        @Test
        @DisplayName("Should return Fon text for fon locale")
        void shouldReturnFonText() {
            assertThat(question.getLocalizedText("fon")).isEqualTo("Akɔ́ntɛ Fránsi tɔn ɖé?");
        }

        @Test
        @DisplayName("Should fallback to French for unknown locale")
        void shouldFallbackToFrench() {
            assertThat(question.getLocalizedText("unknown")).isEqualTo("Quelle est la capitale de la France ?");
        }

        @Test
        @DisplayName("Should fallback to French when translation is null")
        void shouldFallbackWhenNull() {
            QuestionEntity q = new QuestionEntity("French text", "answer");
            assertThat(q.getLocalizedText("en")).isEqualTo("French text");
        }
    }

    @Nested
    @DisplayName("Usage Statistics Tests")
    class UsageStatisticsTests {

        @Test
        @DisplayName("Should increment usage count")
        void shouldIncrementUsageCount() {
            question.incrementUsageCount();
            question.incrementUsageCount();
            question.incrementUsageCount();

            assertThat(question.getUsageCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should increment success count")
        void shouldIncrementSuccessCount() {
            question.incrementSuccessCount();
            question.incrementSuccessCount();

            assertThat(question.getSuccessCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should calculate success rate correctly")
        void shouldCalculateSuccessRate() {
            question.incrementUsageCount();
            question.incrementUsageCount();
            question.incrementUsageCount();
            question.incrementUsageCount();
            question.incrementSuccessCount();
            question.incrementSuccessCount();

            assertThat(question.getSuccessRate()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("Should return 0.0 for no usage")
        void shouldReturnZeroForNoUsage() {
            assertThat(question.getSuccessRate()).isZero();
        }
    }

    @Nested
    @DisplayName("Properties Tests")
    class PropertiesTests {

        @Test
        @DisplayName("Should set and get round type")
        void shouldSetRoundType() {
            question.setRoundType(RoundType.CASCADE);
            assertThat(question.getRoundType()).isEqualTo(RoundType.CASCADE);
        }

        @Test
        @DisplayName("Should set and get difficulty")
        void shouldSetDifficulty() {
            question.setDifficulty(Difficulty.HARD);
            assertThat(question.getDifficulty()).isEqualTo(Difficulty.HARD);
        }

        @Test
        @DisplayName("Should set and get active status")
        void shouldSetActiveStatus() {
            question.setActive(false);
            assertThat(question.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should set and get imposed letter")
        void shouldSetImposedLetter() {
            question.setImposedLetter("A");
            assertThat(question.getImposedLetter()).isEqualTo("A");
        }

        @Test
        @DisplayName("Should set and get points")
        void shouldSetPoints() {
            question.setPoints(100);
            assertThat(question.getPoints()).isEqualTo(100);
        }
    }
}
