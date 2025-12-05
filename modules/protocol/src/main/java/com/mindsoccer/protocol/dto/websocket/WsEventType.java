package com.mindsoccer.protocol.dto.websocket;

/**
 * Types d'événements WebSocket.
 */
public enum WsEventType {

    // Connexion
    CONNECTED,
    PONG,

    // État du match
    STATE_UPDATE,
    MATCH_STARTED,
    MATCH_ENDED,
    MATCH_PAUSED,
    MATCH_RESUMED,
    SCORE_UPDATED,

    // Rubriques
    ROUND_STARTED,
    ROUND_ENDED,

    // Questions
    QUESTION,
    QUESTION_TIMEOUT,

    // Réponses
    ANSWER_RESULT,
    ANSWER_WINDOW_OPEN,
    ANSWER_WINDOW_CLOSED,

    // Buzzer
    BUZZER,

    // SMASH spécifique
    SMASH_TURN_START,       // Début du tour d'une équipe (attaquant)
    SMASH_CONCERTATION,     // Phase de concertation (SMASH A uniquement)
    SMASH_TOP,              // L'attaquant a cliqué TOP (3s pour poser)
    SMASH_QUESTION_SUBMIT,  // L'attaquant soumet sa question
    SMASH_VALIDATE_PROMPT,  // Demande de validation au défenseur (3s)
    SMASH_QUESTION_VALID,   // Le défenseur valide la question
    SMASH_QUESTION_INVALID, // Le défenseur invalide la question (raison requise)
    SMASH_ANSWER_PROMPT,    // Le défenseur doit répondre (10s)
    SMASH_ANSWER_SUBMIT,    // Le défenseur soumet sa réponse
    SMASH_RESULT_PROMPT,    // L'attaquant doit valider la réponse
    SMASH_ANSWER_CORRECT,   // L'attaquant valide: réponse correcte
    SMASH_ANSWER_INCORRECT, // L'attaquant valide: réponse incorrecte
    SMASH_TIMEOUT,          // Timeout générique SMASH
    SMASH_ANNOUNCE,         // Annonce SMASH (legacy)

    // Thèmes
    THEME_SELECTION,
    THEME_SELECTED,

    // JACKPOT spécifique
    AUCTION_START,
    AUCTION_BID,
    AUCTION_CLOSED,
    HINT_REVEALED,

    // CIME spécifique
    NEGOTIATION_START,
    NEGOTIATION_END,
    JOKER_USED,
    CLIMB_DECISION,

    // SAUT PATRIOTIQUE spécifique
    WANT_IT_PROMPT,
    WANT_IT_DECISION,

    // TIRS AU BUT spécifique
    SHOOTER_TURN,
    SHOT_RESULT,

    // Pénalités et suspensions
    PENALTY,
    PLAYER_SUSPENDED,
    SUSPENSION_ENDED,

    // Joueurs
    PLAYER_JOINED,
    PLAYER_LEFT,
    PLAYER_CONNECTED,
    PLAYER_DISCONNECTED,

    // Timer
    TIMER_TICK,
    TIMER_WARNING,

    // Système
    ERROR
}
