package com.mindsoccer.shared.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service d'internationalisation pour MINDSOCCER.
 * Supporte : Français (fr), English (en), Créole haïtien (ht), Fon-gbé (fon).
 */
@Service
public class I18nService {

    private final MessageSource messageSource;
    private static final SupportedLocale DEFAULT_LOCALE = SupportedLocale.FR;

    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Récupère un message traduit pour la locale par défaut (FR).
     */
    public String getMessage(String key) {
        return getMessage(key, DEFAULT_LOCALE);
    }

    /**
     * Récupère un message traduit pour une locale donnée.
     */
    public String getMessage(String key, SupportedLocale locale) {
        return getMessage(key, locale, new Object[]{});
    }

    /**
     * Récupère un message traduit avec des arguments.
     */
    public String getMessage(String key, SupportedLocale locale, Object... args) {
        try {
            return messageSource.getMessage(key, args, locale.getLocale());
        } catch (NoSuchMessageException e) {
            // Fallback vers français si la clé n'existe pas dans la langue demandée
            try {
                return messageSource.getMessage(key, args, DEFAULT_LOCALE.getLocale());
            } catch (NoSuchMessageException e2) {
                return key; // Retourne la clé si aucune traduction trouvée
            }
        }
    }

    /**
     * Récupère un message traduit à partir d'un code de langue.
     */
    public String getMessage(String key, String localeCode) {
        return getMessage(key, SupportedLocale.fromCode(localeCode));
    }

    /**
     * Récupère un message traduit à partir d'un code de langue avec arguments.
     */
    public String getMessage(String key, String localeCode, Object... args) {
        return getMessage(key, SupportedLocale.fromCode(localeCode), args);
    }

    /**
     * Récupère un message traduit à partir d'une Locale Java.
     */
    public String getMessage(String key, Locale locale) {
        return getMessage(key, SupportedLocale.fromCode(locale.getLanguage()));
    }

    /**
     * Vérifie si une clé de message existe.
     */
    public boolean hasMessage(String key, SupportedLocale locale) {
        try {
            messageSource.getMessage(key, null, locale.getLocale());
            return true;
        } catch (NoSuchMessageException e) {
            return false;
        }
    }
}
