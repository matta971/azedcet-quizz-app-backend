package com.mindsoccer.content.service;

import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.content.entity.ThemeEntity;
import com.mindsoccer.content.repository.QuestionRepository;
import com.mindsoccer.content.repository.ThemeRepository;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionService Tests")
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ThemeRepository themeRepository;

    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        questionService = new QuestionService(questionRepository, themeRepository);
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return question when found")
        void shouldReturnQuestionWhenFound() {
            UUID id = UUID.randomUUID();
            QuestionEntity question = new QuestionEntity("Test", "Answer");
            when(questionRepository.findById(id)).thenReturn(Optional.of(question));

            QuestionEntity result = questionService.getById(id);

            assertThat(result).isEqualTo(question);
        }

        @Test
        @DisplayName("Should throw NotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(questionRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.getById(id))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should save and return question")
        void shouldSaveAndReturnQuestion() {
            QuestionEntity question = new QuestionEntity("Test", "Answer");
            when(questionRepository.save(question)).thenReturn(question);

            QuestionEntity result = questionService.create(question);

            assertThat(result).isEqualTo(question);
            verify(questionRepository).save(question);
        }

        @Test
        @DisplayName("Should create question with theme")
        void shouldCreateQuestionWithTheme() {
            UUID themeId = UUID.randomUUID();
            QuestionEntity question = new QuestionEntity("Test", "Answer");
            ThemeEntity theme = new ThemeEntity();
            theme.setId(themeId);

            when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));
            when(questionRepository.save(any())).thenReturn(question);

            questionService.create(question, themeId);

            verify(themeRepository).findById(themeId);
            verify(questionRepository).save(question);
        }

        @Test
        @DisplayName("Should throw when theme not found")
        void shouldThrowWhenThemeNotFound() {
            UUID themeId = UUID.randomUUID();
            QuestionEntity question = new QuestionEntity("Test", "Answer");
            when(themeRepository.findById(themeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.create(question, themeId))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update question fields")
        void shouldUpdateQuestionFields() {
            UUID id = UUID.randomUUID();
            QuestionEntity existing = new QuestionEntity("Old text", "Old answer");
            QuestionEntity updates = new QuestionEntity("New text", "New answer");
            updates.setDifficulty(Difficulty.HARD);
            updates.setRoundType(RoundType.SMASH_A);

            when(questionRepository.findById(id)).thenReturn(Optional.of(existing));
            when(questionRepository.save(any())).thenReturn(existing);

            questionService.update(id, updates);

            verify(questionRepository).save(existing);
            assertThat(existing.getTextFr()).isEqualTo("New text");
            assertThat(existing.getAnswer()).isEqualTo("New answer");
            assertThat(existing.getDifficulty()).isEqualTo(Difficulty.HARD);
        }
    }

    @Nested
    @DisplayName("activate/deactivate Tests")
    class ActivationTests {

        @Test
        @DisplayName("Should activate question")
        void shouldActivateQuestion() {
            UUID id = UUID.randomUUID();
            QuestionEntity question = new QuestionEntity("Test", "Answer");
            question.setActive(false);

            when(questionRepository.findById(id)).thenReturn(Optional.of(question));
            when(questionRepository.save(any())).thenReturn(question);

            questionService.activate(id);

            assertThat(question.isActive()).isTrue();
            verify(questionRepository).save(question);
        }

        @Test
        @DisplayName("Should deactivate question")
        void shouldDeactivateQuestion() {
            UUID id = UUID.randomUUID();
            QuestionEntity question = new QuestionEntity("Test", "Answer");
            question.setActive(true);

            when(questionRepository.findById(id)).thenReturn(Optional.of(question));
            when(questionRepository.save(any())).thenReturn(question);

            questionService.deactivate(id);

            assertThat(question.isActive()).isFalse();
            verify(questionRepository).save(question);
        }
    }

    @Nested
    @DisplayName("recordUsage Tests")
    class RecordUsageTests {

        @Test
        @DisplayName("Should record usage and success")
        void shouldRecordUsageAndSuccess() {
            UUID id = UUID.randomUUID();

            questionService.recordUsage(id, true);

            verify(questionRepository).incrementUsageCount(id);
            verify(questionRepository).incrementSuccessCount(id);
        }

        @Test
        @DisplayName("Should record usage without success")
        void shouldRecordUsageWithoutSuccess() {
            UUID id = UUID.randomUUID();

            questionService.recordUsage(id, false);

            verify(questionRepository).incrementUsageCount(id);
            verify(questionRepository, never()).incrementSuccessCount(id);
        }
    }

    @Nested
    @DisplayName("QuestionSearchCriteria Tests")
    class SearchCriteriaTests {

        @Test
        @DisplayName("Should build search criteria with builder")
        void shouldBuildSearchCriteria() {
            UUID themeId = UUID.randomUUID();

            QuestionService.QuestionSearchCriteria criteria = QuestionService.QuestionSearchCriteria.builder()
                    .themeId(themeId)
                    .roundType(RoundType.CASCADE)
                    .difficulty(Difficulty.EASY)
                    .format(QuestionFormat.MULTIPLE_CHOICE)
                    .searchText("capital")
                    .build();

            assertThat(criteria.themeId()).isEqualTo(themeId);
            assertThat(criteria.roundType()).isEqualTo(RoundType.CASCADE);
            assertThat(criteria.difficulty()).isEqualTo(Difficulty.EASY);
            assertThat(criteria.format()).isEqualTo(QuestionFormat.MULTIPLE_CHOICE);
            assertThat(criteria.searchText()).isEqualTo("capital");
        }
    }
}
