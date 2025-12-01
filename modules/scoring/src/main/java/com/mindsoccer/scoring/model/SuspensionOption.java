package com.mindsoccer.scoring.model;

/**
 * Options de suspension après 5 pénalités.
 * L'équipe doit choisir entre perdre 40 points en une fois
 * ou répondre à 4 questions où chaque erreur coûte 10 points.
 */
public enum SuspensionOption {
    /**
     * Perdre 40 points immédiatement.
     */
    IMMEDIATE_40,

    /**
     * Répondre à 4 questions (4 × 10 points potentiels).
     */
    FOUR_QUESTIONS
}
