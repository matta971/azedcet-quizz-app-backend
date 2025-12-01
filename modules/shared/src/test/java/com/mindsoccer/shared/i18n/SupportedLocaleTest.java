package com.mindsoccer.shared.i18n;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SupportedLocale Tests")
class SupportedLocaleTest {

    @Test
    @DisplayName("Should have four supported locales")
    void shouldHaveFourSupportedLocales() {
        assertThat(SupportedLocale.values()).hasSize(4);
    }

    @Test
    @DisplayName("All locales should have valid properties")
    void allLocalesShouldHaveValidProperties() {
        for (SupportedLocale locale : SupportedLocale.values()) {
            assertThat(locale.getCode()).isNotBlank();
            assertThat(locale.getDisplayName()).isNotBlank();
            assertThat(locale.getLocale()).isNotNull();
        }
    }

    @Test
    @DisplayName("French should be the default locale")
    void frenchShouldBeDefault() {
        assertThat(SupportedLocale.fromCode(null)).isEqualTo(SupportedLocale.FR);
        assertThat(SupportedLocale.fromCode("")).isEqualTo(SupportedLocale.FR);
        assertThat(SupportedLocale.fromCode("unknown")).isEqualTo(SupportedLocale.FR);
    }

    @ParameterizedTest
    @DisplayName("Should find locale by code")
    @ValueSource(strings = {"fr", "en", "ht", "fon"})
    void shouldFindLocaleByCode(String code) {
        SupportedLocale locale = SupportedLocale.fromCode(code);
        assertThat(locale.getCode()).isEqualTo(code);
    }

    @ParameterizedTest
    @DisplayName("Should handle case-insensitive codes")
    @ValueSource(strings = {"FR", "En", "HT", "FON", "  fr  "})
    void shouldHandleCaseInsensitiveCodes(String code) {
        SupportedLocale locale = SupportedLocale.fromCode(code);
        assertThat(locale).isNotNull();
    }

    @Test
    @DisplayName("Should verify supported codes")
    void shouldVerifySupportedCodes() {
        assertThat(SupportedLocale.isSupported("fr")).isTrue();
        assertThat(SupportedLocale.isSupported("en")).isTrue();
        assertThat(SupportedLocale.isSupported("ht")).isTrue();
        assertThat(SupportedLocale.isSupported("fon")).isTrue();
        assertThat(SupportedLocale.isSupported("de")).isFalse();
        assertThat(SupportedLocale.isSupported("es")).isFalse();
    }

    @ParameterizedTest
    @DisplayName("Should return false for null or blank codes")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldReturnFalseForNullOrBlank(String code) {
        assertThat(SupportedLocale.isSupported(code)).isFalse();
    }

    @Test
    @DisplayName("French locale should have correct properties")
    void frenchLocaleShouldHaveCorrectProperties() {
        SupportedLocale fr = SupportedLocale.FR;
        assertThat(fr.getCode()).isEqualTo("fr");
        assertThat(fr.getDisplayName()).isEqualTo("Français");
        assertThat(fr.getLocale().getLanguage()).isEqualTo("fr");
    }

    @Test
    @DisplayName("Haitian Creole locale should have correct properties")
    void haitianCreoleLocaleShouldHaveCorrectProperties() {
        SupportedLocale ht = SupportedLocale.HT;
        assertThat(ht.getCode()).isEqualTo("ht");
        assertThat(ht.getDisplayName()).isEqualTo("Kreyòl Ayisyen");
    }

    @Test
    @DisplayName("Fon locale should have correct properties")
    void fonLocaleShouldHaveCorrectProperties() {
        SupportedLocale fon = SupportedLocale.FON;
        assertThat(fon.getCode()).isEqualTo("fon");
        assertThat(fon.getDisplayName()).isEqualTo("Fɔngbè");
    }
}
