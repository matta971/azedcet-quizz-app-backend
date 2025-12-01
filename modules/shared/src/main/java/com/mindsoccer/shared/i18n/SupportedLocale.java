package com.mindsoccer.shared.i18n;

import lombok.Getter;

import java.util.Locale;

/**
 * Langues supportées par MINDSOCCER.
 */
@Getter
public enum SupportedLocale {

    FR("fr", "Français", new Locale("fr")),
    EN("en", "English", Locale.ENGLISH),
    HT("ht", "Kreyòl Ayisyen", new Locale("ht")),  // Créole haïtien
    FON("fon", "Fɔngbè", new Locale("fon"));       // Fon-gbé (Bénin)

    private final String code;
    private final String displayName;
    private final Locale locale;

    SupportedLocale(String code, String displayName, Locale locale) {
        this.code = code;
        this.displayName = displayName;
        this.locale = locale;
    }

    /**
     * Trouve une locale par son code, ou retourne FR par défaut.
     */
    public static SupportedLocale fromCode(String code) {
        if (code == null || code.isBlank()) {
            return FR;
        }
        String normalized = code.toLowerCase().trim();
        for (SupportedLocale locale : values()) {
            if (locale.code.equals(normalized)) {
                return locale;
            }
        }
        return FR;
    }

    /**
     * Vérifie si un code de langue est supporté.
     */
    public static boolean isSupported(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String normalized = code.toLowerCase().trim();
        for (SupportedLocale locale : values()) {
            if (locale.code.equals(normalized)) {
                return true;
            }
        }
        return false;
    }
}
