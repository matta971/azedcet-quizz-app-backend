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
    SMASH_ANNOUNCE,
    SMASH_TIMEOUT,

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
