package com.mindsoccer.protocol.enums;

/**
 * Langues supportées par le système.
 */
public enum Language {
    FR("Français"),
    EN("English"),
    PT("Português"),
    ES("Español"),
    AR("العربية"),
    ZH("中文"),
    DE("Deutsch"),
    IT("Italiano"),
    FON("Fɔngbè"),
    CR("Créole");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
