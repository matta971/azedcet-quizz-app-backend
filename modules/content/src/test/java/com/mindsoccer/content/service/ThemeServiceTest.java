package com.mindsoccer.content.service;

import com.mindsoccer.content.entity.ThemeEntity;
import com.mindsoccer.content.repository.ThemeRepository;
import com.mindsoccer.shared.exception.NotFoundException;
import com.mindsoccer.shared.exception.ValidationException;
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
@DisplayName("ThemeService Tests")
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeRepository);
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return theme when found")
        void shouldReturnThemeWhenFound() {
            UUID id = UUID.randomUUID();
            ThemeEntity theme = new ThemeEntity();
            theme.setCode("SPORT");
            when(themeRepository.findById(id)).thenReturn(Optional.of(theme));

            ThemeEntity result = themeService.getById(id);

            assertThat(result).isEqualTo(theme);
        }

        @Test
        @DisplayName("Should throw NotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(themeRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> themeService.getById(id))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByCode Tests")
    class GetByCodeTests {

        @Test
        @DisplayName("Should return theme when found")
        void shouldReturnThemeWhenFound() {
            ThemeEntity theme = new ThemeEntity();
            theme.setCode("GEOGRAPHY");
            when(themeRepository.findByCode("GEOGRAPHY")).thenReturn(Optional.of(theme));

            ThemeEntity result = themeService.getByCode("GEOGRAPHY");

            assertThat(result.getCode()).isEqualTo("GEOGRAPHY");
        }

        @Test
        @DisplayName("Should throw when code not found")
        void shouldThrowWhenCodeNotFound() {
            when(themeRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> themeService.getByCode("UNKNOWN"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create theme with unique code")
        void shouldCreateThemeWithUniqueCode() {
            ThemeEntity theme = new ThemeEntity();
            theme.setCode("NEW_THEME");
            theme.setNameFr("Nouveau Thème");

            when(themeRepository.existsByCode("NEW_THEME")).thenReturn(false);
            when(themeRepository.save(theme)).thenReturn(theme);

            ThemeEntity result = themeService.create(theme);

            assertThat(result).isEqualTo(theme);
            verify(themeRepository).save(theme);
        }

        @Test
        @DisplayName("Should throw when code already exists")
        void shouldThrowWhenCodeExists() {
            ThemeEntity theme = new ThemeEntity();
            theme.setCode("EXISTING");

            when(themeRepository.existsByCode("EXISTING")).thenReturn(true);

            assertThatThrownBy(() -> themeService.create(theme))
                    .isInstanceOf(ValidationException.class);

            verify(themeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update theme")
        void shouldUpdateTheme() {
            UUID id = UUID.randomUUID();
            ThemeEntity existing = new ThemeEntity();
            existing.setCode("OLD_CODE");
            existing.setNameFr("Old Name");

            ThemeEntity updates = new ThemeEntity();
            updates.setCode("NEW_CODE");
            updates.setNameFr("New Name");

            when(themeRepository.findById(id)).thenReturn(Optional.of(existing));
            when(themeRepository.existsByCode("NEW_CODE")).thenReturn(false);
            when(themeRepository.save(any())).thenReturn(existing);

            themeService.update(id, updates);

            assertThat(existing.getCode()).isEqualTo("NEW_CODE");
            assertThat(existing.getNameFr()).isEqualTo("New Name");
            verify(themeRepository).save(existing);
        }

        @Test
        @DisplayName("Should allow same code on update")
        void shouldAllowSameCodeOnUpdate() {
            UUID id = UUID.randomUUID();
            ThemeEntity existing = new ThemeEntity();
            existing.setCode("SAME_CODE");
            existing.setNameFr("Old Name");

            ThemeEntity updates = new ThemeEntity();
            updates.setCode("SAME_CODE");
            updates.setNameFr("New Name");

            when(themeRepository.findById(id)).thenReturn(Optional.of(existing));
            when(themeRepository.save(any())).thenReturn(existing);

            themeService.update(id, updates);

            // Should not check for code existence if same
            verify(themeRepository, never()).existsByCode(any());
        }
    }

    @Nested
    @DisplayName("activate/deactivate Tests")
    class ActivationTests {

        @Test
        @DisplayName("Should activate theme")
        void shouldActivateTheme() {
            UUID id = UUID.randomUUID();
            ThemeEntity theme = new ThemeEntity();
            theme.setActive(false);

            when(themeRepository.findById(id)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any())).thenReturn(theme);

            themeService.activate(id);

            assertThat(theme.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should deactivate theme")
        void shouldDeactivateTheme() {
            UUID id = UUID.randomUUID();
            ThemeEntity theme = new ThemeEntity();
            theme.setActive(true);

            when(themeRepository.findById(id)).thenReturn(Optional.of(theme));
            when(themeRepository.save(any())).thenReturn(theme);

            themeService.deactivate(id);

            assertThat(theme.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete theme with no questions")
        void shouldDeleteThemeWithNoQuestions() {
            UUID id = UUID.randomUUID();
            ThemeEntity theme = new ThemeEntity();

            when(themeRepository.findById(id)).thenReturn(Optional.of(theme));
            when(themeRepository.countActiveQuestionsByThemeId(id)).thenReturn(0L);

            themeService.delete(id);

            verify(themeRepository).delete(theme);
        }

        @Test
        @DisplayName("Should throw when theme has questions")
        void shouldThrowWhenThemeHasQuestions() {
            UUID id = UUID.randomUUID();
            ThemeEntity theme = new ThemeEntity();

            when(themeRepository.findById(id)).thenReturn(Optional.of(theme));
            when(themeRepository.countActiveQuestionsByThemeId(id)).thenReturn(5L);

            assertThatThrownBy(() -> themeService.delete(id))
                    .isInstanceOf(ValidationException.class);

            verify(themeRepository, never()).delete(any());
        }
    }
}
