package com.mindsoccer.api.service;

import com.mindsoccer.match.entity.MatchEntity;
import com.mindsoccer.match.service.MatchService;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.realtime.handler.SmashGameHandler;
import com.mindsoccer.realtime.service.GameBroadcastService;
import com.mindsoccer.shared.util.GameConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * Service d'orchestration pour les rounds SMASH A et SMASH B.
 *
 * Workflow SMASH A:
 * 1. CONCERTATION - L'équipe attaquante se concerte (temps illimité)
 * 2. TOP - L'attaquant clique TOP (démarre 3s pour poser la question)
 * 3. QUESTION - L'attaquant pose sa question (3s max, sinon défenseur +10pts)
 * 4. VALIDATE - Le défenseur valide/invalide (3s max, sinon question valide par défaut)
 * 5. ANSWER - Le défenseur répond (10s max)
 * 6. RESULT - L'attaquant valide la réponse (correct = défenseur +10pts)
 *
 * Workflow SMASH B: Même que A mais sans concertation (directement 3s pour poser)
 */
@Service
public class SmashOrchestratorService implements SmashGameHandler {

    private static final Logger log = LoggerFactory.getLogger(SmashOrchestratorService.class);

    private final GameBroadcastService broadcastService;
    private final MatchService matchService;

    private final ConcurrentHashMap<UUID, SmashGameState> gameStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public SmashOrchestratorService(GameBroadcastService broadcastService, MatchService matchService) {
        this.broadcastService = broadcastService;
        this.matchService = matchService;
    }

    /**
     * Démarre un round SMASH pour un match.
     */
    @Override
    public void startSmashRound(UUID matchId, RoundType roundType) {
        log.info("Starting {} round for match {}", roundType, matchId);

        SmashGameState state = new SmashGameState(matchId, roundType);
        gameStates.put(matchId, state);

        // Broadcast round started
        broadcastService.broadcastRoundStarted(matchId, roundType, 1);

        // Start first turn (Team A attacks first - the team that started the match)
        scheduler.schedule(() -> startTurn(matchId), 2, TimeUnit.SECONDS);
    }

    /**
     * Démarre un tour SMASH.
     */
    private void startTurn(UUID matchId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        state.turnNumber++;
        state.currentPhase = SmashPhase.TURN_START;

        // Determine attacker: first turn = Team A, second turn = Team B
        TeamSide attacker = state.turnNumber == 1 ? TeamSide.A : TeamSide.B;
        state.currentAttacker = attacker;

        log.info("SMASH turn {} starting - {} attacks", state.turnNumber, attacker);

        broadcastService.broadcastSmashTurnStart(matchId, state.turnNumber, attacker, state.roundType);

        // Wait a moment then start the appropriate phase
        scheduler.schedule(() -> {
            if (state.roundType == RoundType.SMASH_A) {
                startConcertationPhase(matchId);
            } else {
                // SMASH B: directly start question phase (3s to submit)
                startQuestionPhaseAfterTop(matchId);
            }
        }, 1, TimeUnit.SECONDS);
    }

    /**
     * Phase de concertation (SMASH A uniquement).
     */
    private void startConcertationPhase(UUID matchId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        state.currentPhase = SmashPhase.CONCERTATION;
        broadcastService.broadcastSmashConcertation(matchId, state.currentAttacker);
        log.debug("Concertation phase started for match {}", matchId);
        // No timeout - waiting for TOP button
    }

    /**
     * L'attaquant appuie sur TOP (appelé depuis le contrôleur).
     */
    @Override
    public void handleTop(UUID matchId, UUID playerId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) {
            log.warn("No SMASH state for match {}", matchId);
            return;
        }

        if (state.currentPhase != SmashPhase.CONCERTATION) {
            log.warn("TOP received in wrong phase: {} for match {}", state.currentPhase, matchId);
            return;
        }

        log.info("TOP pressed for match {} by player {}", matchId, playerId);
        broadcastService.broadcastSmashTop(matchId, state.currentAttacker);

        startQuestionPhaseAfterTop(matchId);
    }

    /**
     * Démarre la phase de question (3s pour soumettre).
     */
    private void startQuestionPhaseAfterTop(UUID matchId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        state.currentPhase = SmashPhase.QUESTION;
        state.questionStartTime = System.currentTimeMillis();

        // Schedule timeout for question
        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            handleQuestionTimeout(matchId);
        }, GameConstants.SMASH_QUESTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        timers.put(matchId, timer);

        log.debug("Question phase started - {} has 3s to submit", state.currentAttacker);
    }

    /**
     * L'attaquant soumet sa question (appelé depuis le contrôleur).
     */
    @Override
    public void handleQuestionSubmit(UUID matchId, UUID playerId, String questionText) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        if (state.currentPhase != SmashPhase.QUESTION) {
            log.warn("Question submitted in wrong phase: {} for match {}", state.currentPhase, matchId);
            return;
        }

        // Cancel timeout
        cancelTimer(matchId);

        state.currentQuestion = questionText;
        TeamSide defender = getDefender(state.currentAttacker);

        broadcastService.broadcastSmashQuestionSubmit(matchId, questionText, state.currentAttacker);

        // Start validation phase
        startValidationPhase(matchId, questionText);
    }

    /**
     * Timeout de la phase question: l'attaquant n'a pas posé à temps.
     */
    private void handleQuestionTimeout(UUID matchId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null || state.currentPhase != SmashPhase.QUESTION) return;

        log.info("Question timeout for match {} - defender wins 10 pts", matchId);

        TeamSide defender = getDefender(state.currentAttacker);
        addPoints(state, defender, GameConstants.SMASH_ATTACKER_TIMEOUT_POINTS);

        broadcastService.broadcastSmashTimeout(matchId, "QUESTION", state.currentAttacker, defender,
                GameConstants.SMASH_ATTACKER_TIMEOUT_POINTS, state.scoreA, state.scoreB);
        broadcastService.broadcastScoreUpdate(matchId, state.scoreA, state.scoreB);

        // End turn, move to next
        endTurn(matchId);
    }

    /**
     * Démarre la phase de validation (3s pour valider/invalider).
     */
    private void startValidationPhase(UUID matchId, String questionText) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        state.currentPhase = SmashPhase.VALIDATE;
        TeamSide defender = getDefender(state.currentAttacker);

        broadcastService.broadcastSmashValidatePrompt(matchId, questionText, defender);

        // Schedule timeout - if no validation, question is valid by default
        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            handleValidateTimeout(matchId);
        }, GameConstants.SMASH_VALIDATE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        timers.put(matchId, timer);

        log.debug("Validation phase started - {} has 3s to validate", defender);
    }

    /**
     * Le défenseur valide ou invalide la question (appelé depuis le contrôleur).
     */
    @Override
    public void handleValidation(UUID matchId, UUID playerId, boolean valid, String reason) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        if (state.currentPhase != SmashPhase.VALIDATE) {
            log.warn("Validation received in wrong phase: {} for match {}", state.currentPhase, matchId);
            return;
        }

        // Cancel timeout
        cancelTimer(matchId);

        TeamSide defender = getDefender(state.currentAttacker);

        if (valid) {
            broadcastService.broadcastSmashQuestionValid(matchId, defender);
            // Start answer phase
            startAnswerPhase(matchId);
        } else {
            // Invalid question: defender wins 10 pts
            addPoints(state, defender, GameConstants.SMASH_INVALID_QUESTION_POINTS);
            broadcastService.broadcastSmashQuestionInvalid(matchId, defender, reason,
                    GameConstants.SMASH_INVALID_QUESTION_POINTS, state.scoreA, state.scoreB);
            broadcastService.broadcastScoreUpdate(matchId, state.scoreA, state.scoreB);

            // End turn
            endTurn(matchId);
        }
    }

    /**
     * Timeout de validation: question considérée valide par défaut.
     */
    private void handleValidateTimeout(UUID matchId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null || state.currentPhase != SmashPhase.VALIDATE) return;

        log.info("Validate timeout for match {} - question valid by default", matchId);

        TeamSide defender = getDefender(state.currentAttacker);
        broadcastService.broadcastSmashQuestionValid(matchId, defender);

        // Start answer phase
        startAnswerPhase(matchId);
    }

    /**
     * Démarre la phase de réponse (10s pour répondre).
     */
    private void startAnswerPhase(UUID matchId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        state.currentPhase = SmashPhase.ANSWER;
        TeamSide defender = getDefender(state.currentAttacker);

        broadcastService.broadcastSmashAnswerPrompt(matchId, state.currentQuestion, defender);

        // Schedule timeout
        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            handleAnswerTimeout(matchId);
        }, GameConstants.SMASH_ANSWER_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        timers.put(matchId, timer);

        log.debug("Answer phase started - {} has 10s to answer", defender);
    }

    /**
     * Le défenseur soumet sa réponse (appelé depuis le contrôleur).
     */
    @Override
    public void handleAnswerSubmit(UUID matchId, UUID playerId, String answer) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        if (state.currentPhase != SmashPhase.ANSWER) {
            log.warn("Answer submitted in wrong phase: {} for match {}", state.currentPhase, matchId);
            return;
        }

        // Cancel timeout
        cancelTimer(matchId);

        state.currentAnswer = answer;
        TeamSide defender = getDefender(state.currentAttacker);

        broadcastService.broadcastSmashAnswerSubmit(matchId, answer, defender);

        // Start result phase
        startResultPhase(matchId);
    }

    /**
     * Timeout de réponse: pas de points, fin du tour.
     */
    private void handleAnswerTimeout(UUID matchId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null || state.currentPhase != SmashPhase.ANSWER) return;

        log.info("Answer timeout for match {} - no points awarded", matchId);

        TeamSide defender = getDefender(state.currentAttacker);
        broadcastService.broadcastSmashTimeout(matchId, "ANSWER", defender, null, 0, state.scoreA, state.scoreB);

        // End turn
        endTurn(matchId);
    }

    /**
     * Démarre la phase de résultat (l'attaquant valide la réponse).
     */
    private void startResultPhase(UUID matchId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        state.currentPhase = SmashPhase.RESULT;

        broadcastService.broadcastSmashResultPrompt(matchId, state.currentAnswer, state.currentAttacker);

        log.debug("Result phase started - {} validates the answer", state.currentAttacker);
        // No timeout - attacker must validate
    }

    /**
     * L'attaquant valide la réponse comme correcte ou incorrecte (appelé depuis le contrôleur).
     */
    @Override
    public void handleResultValidation(UUID matchId, UUID playerId, boolean correct) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        if (state.currentPhase != SmashPhase.RESULT) {
            log.warn("Result validation in wrong phase: {} for match {}", state.currentPhase, matchId);
            return;
        }

        TeamSide defender = getDefender(state.currentAttacker);

        if (correct) {
            // Defender wins 10 pts
            addPoints(state, defender, GameConstants.SMASH_CORRECT_POINTS);
            broadcastService.broadcastSmashAnswerCorrect(matchId, defender,
                    GameConstants.SMASH_CORRECT_POINTS, state.scoreA, state.scoreB);
        } else {
            // No points
            broadcastService.broadcastSmashAnswerIncorrect(matchId, state.scoreA, state.scoreB);
        }

        broadcastService.broadcastScoreUpdate(matchId, state.scoreA, state.scoreB);

        // End turn
        endTurn(matchId);
    }

    /**
     * Fin d'un tour.
     */
    private void endTurn(UUID matchId) {
        SmashGameState state = gameStates.get(matchId);
        if (state == null) return;

        // Reset turn state
        state.currentQuestion = null;
        state.currentAnswer = null;

        // Check if round is complete (2 turns = each team attacked once)
        if (state.turnNumber >= 2) {
            endRound(matchId);
        } else {
            // Start next turn after a delay
            scheduler.schedule(() -> startTurn(matchId), 2, TimeUnit.SECONDS);
        }
    }

    /**
     * Fin du round SMASH.
     */
    private void endRound(UUID matchId) {
        SmashGameState state = gameStates.remove(matchId);
        if (state == null) return;

        cancelTimer(matchId);

        log.info("SMASH round ended for match {}. Final scores: A={}, B={}",
                matchId, state.scoreA, state.scoreB);

        broadcastService.broadcastRoundEnded(matchId, state.roundType, state.scoreA, state.scoreB);

        // Update match scores
        matchService.updateScore(matchId, TeamSide.A, state.scoreA);
        matchService.updateScore(matchId, TeamSide.B, state.scoreB);
    }

    /**
     * Retourne l'équipe adverse.
     */
    private TeamSide getDefender(TeamSide attacker) {
        return attacker == TeamSide.A ? TeamSide.B : TeamSide.A;
    }

    /**
     * Ajoute des points à une équipe.
     */
    private void addPoints(SmashGameState state, TeamSide team, int points) {
        if (team == TeamSide.A) {
            state.scoreA += points;
        } else {
            state.scoreB += points;
        }
    }

    /**
     * Annule le timer courant.
     */
    private void cancelTimer(UUID matchId) {
        ScheduledFuture<?> timer = timers.remove(matchId);
        if (timer != null) {
            timer.cancel(false);
        }
    }

    /**
     * Vérifie si un match a un round SMASH actif.
     */
    @Override
    public boolean hasActiveSmashRound(UUID matchId) {
        return gameStates.containsKey(matchId);
    }

    /**
     * Récupère l'état SMASH actuel d'un match.
     */
    public SmashGameState getState(UUID matchId) {
        return gameStates.get(matchId);
    }

    // === Enum et classes internes ===

    public enum SmashPhase {
        TURN_START,
        CONCERTATION,
        QUESTION,
        VALIDATE,
        ANSWER,
        RESULT
    }

    public static class SmashGameState {
        public final UUID matchId;
        public final RoundType roundType;
        public int turnNumber = 0;
        public TeamSide currentAttacker;
        public SmashPhase currentPhase;
        public String currentQuestion;
        public String currentAnswer;
        public long questionStartTime;
        public int scoreA = 0;
        public int scoreB = 0;

        public SmashGameState(UUID matchId, RoundType roundType) {
            this.matchId = matchId;
            this.roundType = roundType;
        }
    }
}
