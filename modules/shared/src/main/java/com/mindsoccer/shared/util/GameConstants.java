package com.mindsoccer.shared.util;

import java.time.Duration;

/**
 * Constantes du jeu MINDSOCCER.
 * Source de vérité pour les règles et valeurs de configuration.
 */
public final class GameConstants {

    private GameConstants() {
        // Utility class
    }

    // ==================== TIMERS ====================

    /** Intervalle du tick serveur (ms) */
    public static final long TICK_INTERVAL_MS = 100;

    /** Timeout pour annoncer une question SMASH (3 secondes) */
    public static final Duration SMASH_ANNOUNCE_TIMEOUT = Duration.ofSeconds(3);
    public static final long SMASH_ANNOUNCE_TIMEOUT_MS = 3000;

    /** Timeout pour le bonus RELAIS sans faute (40 secondes) */
    public static final Duration RELAIS_BONUS_TIMEOUT = Duration.ofSeconds(40);
    public static final long RELAIS_BONUS_TIMEOUT_MS = 40000;

    /** Temps de négociation CIME (30 secondes) */
    public static final Duration CIME_NEGOTIATION_TIMEOUT = Duration.ofSeconds(30);
    public static final long CIME_NEGOTIATION_TIMEOUT_MS = 30000;

    /** Timeout par défaut pour une réponse (10 secondes) */
    public static final Duration DEFAULT_ANSWER_TIMEOUT = Duration.ofSeconds(10);
    public static final long DEFAULT_ANSWER_TIMEOUT_MS = 10000;

    // ==================== POINTS ====================

    /** Points par défaut pour une bonne réponse */
    public static final int DEFAULT_POINTS = 10;

    /** Points SMASH - pénalité si timeout annonce */
    public static final int SMASH_TIMEOUT_PENALTY_POINTS = 10;

    /** Points RELAIS - bonus sans faute en temps */
    public static final int RELAIS_BONUS_POINTS = 20;

    /** Points ESTOCADE par question */
    public static final int ESTOCADE_POINTS_PER_QUESTION = 40;

    /** Points TIRS AU BUT - victoire de séance */
    public static final int TIRS_AU_BUT_VICTORY_POINTS = 40;

    /** Points CIME - seuil >4 bonnes réponses */
    public static final int CIME_THRESHOLD_4_BONUS = 20;

    /** Points CIME - seuil >7 bonnes réponses */
    public static final int CIME_THRESHOLD_7_BONUS = 40;

    /** Points IDENTIFICATION par indice (du plus flou au plus net) */
    public static final int[] IDENTIFICATION_POINTS = {40, 30, 20, 10};

    /** Cagnotte JACKPOT par équipe */
    public static final int JACKPOT_INITIAL_POOL = 100;

    // ==================== QUESTIONS ====================

    /** Nombre de questions CASCADE */
    public static final int CASCADE_QUESTION_COUNT = 10;

    /** Nombre de questions PANIER (par équipe) */
    public static final int PANIER_QUESTION_COUNT = 4;

    /** Nombre de questions RELAIS */
    public static final int RELAIS_QUESTION_COUNT = 4;

    /** Nombre de questions DUEL */
    public static final int DUEL_QUESTION_COUNT = 4;

    /** Nombre de questions SAUT PATRIOTIQUE */
    public static final int SAUT_PATRIOTIQUE_QUESTION_COUNT = 4;

    /** Nombre de questions ECHAPPEE (1 clé + 4 questions) */
    public static final int ECHAPPEE_QUESTION_COUNT = 5;

    /** Nombre de questions ESTOCADE */
    public static final int ESTOCADE_QUESTION_COUNT = 3;

    /** Nombre de questions MARATHON */
    public static final int MARATHON_QUESTION_COUNT = 10;

    /** Nombre de questions JACKPOT */
    public static final int JACKPOT_QUESTION_COUNT = 3;

    /** Nombre de questions TRANSALT */
    public static final int TRANSALT_QUESTION_COUNT = 10;

    /** Nombre de questions CROSS-COUNTRY */
    public static final int CROSS_COUNTRY_QUESTION_COUNT = 10;

    /** Nombre de questions CROSS-DICTIONARY */
    public static final int CROSS_DICTIONARY_QUESTION_COUNT = 4;

    /** Nombre de questions TIRS AU BUT (par équipe) */
    public static final int TIRS_AU_BUT_QUESTION_COUNT = 4;

    /** Nombre de questions CAPOEIRA */
    public static final int CAPOEIRA_QUESTION_COUNT = 4;

    /** Nombre de questions CIME */
    public static final int CIME_QUESTION_COUNT = 10;

    /** Nombre de questions RANDONNEE LEXICALE */
    public static final int RANDONNEE_LEXICALE_QUESTION_COUNT = 10;

    /** Nombre d'indices IDENTIFICATION */
    public static final int IDENTIFICATION_HINT_COUNT = 4;

    /** Nombre de questions SPRINT FINAL */
    public static final int SPRINT_FINAL_QUESTION_COUNT = 20;

    /** Nombre de questions éclairs en cas d'égalité TIRS AU BUT */
    public static final int TIRS_AU_BUT_TIEBREAKER_COUNT = 3;

    // ==================== PENALTIES ====================

    /** Nombre de pénalités pour suspension */
    public static final int PENALTY_SUSPENSION_THRESHOLD = 5;

    /** Points de suspension option 1 (4×10) */
    public static final int SUSPENSION_OPTION_4X10 = 40;

    /** Points de suspension option 2 (1×40) */
    public static final int SUSPENSION_OPTION_1X40 = 40;

    // ==================== CIME ====================

    /** Nombre de jokers disponibles en CIME */
    public static final int CIME_JOKER_COUNT = 3;

    /** Seuil de bonnes réponses pour bonus +20 */
    public static final int CIME_BONUS_THRESHOLD_LOW = 4;

    /** Seuil de bonnes réponses pour bonus +40 */
    public static final int CIME_BONUS_THRESHOLD_HIGH = 7;

    // ==================== THÈMES ====================

    /** Nombre de thèmes PANIER */
    public static final int PANIER_THEME_COUNT = 4;

    /** Nombre de thèmes proposés en CIME */
    public static final int CIME_THEME_COUNT = 3;

    // ==================== ÉQUIPES ====================

    /** Taille minimale d'une équipe */
    public static final int TEAM_MIN_SIZE = 2;

    /** Taille maximale d'une équipe */
    public static final int TEAM_MAX_SIZE = 5;

    /** Taille équipe en mode duo (1v1) */
    public static final int TEAM_DUO_SIZE = 1;

    /** Taille équipe en mode solo */
    public static final int TEAM_SIZE_SOLO = 1;

    // ==================== MATCH ====================

    /** Longueur du code de match (6 caractères) */
    public static final int MATCH_CODE_LENGTH = 6;

    /** Timeout buzzer (ms) */
    public static final long BUZZER_TIMEOUT_MS = 5000;

    // ==================== SMASH ====================

    /** Nombre de questions SMASH (2 questions: 1 par équipe) */
    public static final int SMASH_QUESTION_COUNT = 2;

    /** Points pour bonne réponse SMASH */
    public static final int SMASH_CORRECT_POINTS = 10;

    /** Points pour vol SMASH (steal) */
    public static final int SMASH_STEAL_POINTS = 10;

    /** Timeout pour poser la question après TOP (3 secondes) */
    public static final long SMASH_QUESTION_TIMEOUT_MS = 3000;
    public static final Duration SMASH_QUESTION_TIMEOUT = Duration.ofSeconds(3);

    /** Timeout pour valider la question (3 secondes) */
    public static final long SMASH_VALIDATE_TIMEOUT_MS = 3000;
    public static final Duration SMASH_VALIDATE_TIMEOUT = Duration.ofSeconds(3);

    /** Timeout pour répondre à la question (10 secondes) */
    public static final long SMASH_ANSWER_TIMEOUT_MS = 10000;
    public static final Duration SMASH_ANSWER_TIMEOUT = Duration.ofSeconds(10);

    /** Points pour timeout de l'attaquant (3s dépassé) */
    public static final int SMASH_ATTACKER_TIMEOUT_POINTS = 10;

    /** Points pour question invalide */
    public static final int SMASH_INVALID_QUESTION_POINTS = 10;

    // ==================== CASCADE ====================

    /** Points de base CASCADE (première question) */
    public static final int CASCADE_BASE_POINTS = 10;

    /** Incrément de points CASCADE */
    public static final int CASCADE_INCREMENT = 10;

    // ==================== SPRINT FINAL ====================

    /** Nombre de questions SPRINT (alias SPRINT_FINAL) */
    public static final int SPRINT_QUESTION_COUNT = SPRINT_FINAL_QUESTION_COUNT;

    /** Points par question SPRINT FINAL */
    public static final int SPRINT_POINTS_PER_QUESTION = 10;

    // ==================== PENALTIES (compléments) ====================

    /** Points de pénalité suspension */
    public static final int PENALTY_SUSPENSION_POINTS = 40;
}
