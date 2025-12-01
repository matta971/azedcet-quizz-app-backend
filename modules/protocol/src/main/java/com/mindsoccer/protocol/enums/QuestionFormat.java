package com.mindsoccer.protocol.enums;

/**
 * Format des questions.
 */
public enum QuestionFormat {
    /** Question texte avec réponse texte libre */
    TEXT,

    /** Question à choix multiples (QCM) */
    MULTIPLE_CHOICE,

    /** Question avec média (image, audio, vidéo) */
    MEDIA,

    /** Question d'identification progressive (indices successifs) */
    IDENTIFICATION,

    /** Question de type anagramme */
    ANAGRAM,

    /** Question géographique (ville/pays) */
    GEOGRAPHY,

    /** Question lexicale (définition à trouver) */
    LEXICAL
}
