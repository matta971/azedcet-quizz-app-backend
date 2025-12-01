package com.mindsoccer.protocol.enums;

public enum MatchStatus {
    /** Match créé, en attente de joueurs */
    WAITING,
    /** Match en lobby, joueurs rejoignent */
    LOBBY,
    /** Match en cours (alias pour PLAYING) */
    IN_PROGRESS,
    /** Match en cours */
    PLAYING,
    /** Match en pause */
    PAUSED,
    /** Match terminé */
    FINISHED,
    /** Match annulé */
    CANCELLED
}
