package com.mindsoccer.protocol.enums;

/**
 * Type de question selon le mode de jeu.
 * Définit le comportement et la structure attendue de la question.
 */
public enum QuestionType {
    /**
     * Question standard avec réponse textuelle libre.
     * Utilisé dans : SMASH, DUEL, MARATHON, SPRINT_FINAL, PANIER, RELAIS, CIME
     */
    STANDARD,

    /**
     * Question à choix multiples (QCM).
     * La réponse est sélectionnée parmi plusieurs options.
     */
    QCM,

    /**
     * Question avec indices progressifs.
     * Utilisé dans : JACKPOT (3 indices), ESTOCADE (3 indices)
     * Points dégressifs selon le nombre d'indices révélés.
     */
    INDICES,

    /**
     * Question d'identification avec 4 indices.
     * Utilisé dans : IDENTIFICATION
     * Points : 40 → 30 → 20 → 10 selon l'indice.
     */
    IDENTIFICATION,

    /**
     * Question avec réponse commençant par une lettre imposée.
     * Utilisé dans : RANDONNEE_LEXICALE, CROSS_DICTIONARY
     * La réponse doit obligatoirement commencer par la lettre indiquée.
     */
    ALPHABETIQUE,

    /**
     * Question rapide (5 secondes max).
     * Utilisé dans : SPRINT_FINAL, questions éclairs
     * Temps de réponse très court.
     */
    ECLAIR,

    /**
     * Question géographique liée à un pays.
     * Utilisé dans : SAUT_PATRIOTIQUE, ECHAPPEE
     * Associée à un pays spécifique.
     */
    GEOGRAPHIQUE,

    /**
     * Question sur le thème musique.
     * Utilisé dans : CAPOEIRA
     * Questions exclusivement sur la musique.
     */
    MUSIQUE,

    /**
     * Question de type énigme avec un mot à deviner.
     * Utilisé dans : TIRS_AU_BUT
     * Le gardien donne des indices, le tireur devine.
     */
    ENIGME
}
