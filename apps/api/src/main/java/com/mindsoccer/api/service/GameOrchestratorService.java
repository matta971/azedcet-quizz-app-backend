package com.mindsoccer.api.service;

import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.content.service.QuestionService;
import com.mindsoccer.match.entity.MatchEntity;
import com.mindsoccer.match.entity.PlayerEntity;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.realtime.service.GameBroadcastService;
import com.mindsoccer.shared.util.GameConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Service d'orchestration qui gère le cycle de vie complet d'un match.
 * Version MVP simplifiée sans dépendance sur GameEngine.
 */
@Service
public class GameOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(GameOrchestratorService.class);

    private final GameBroadcastService broadcastService;
    private final QuestionService questionService;
    private final SmashOrchestratorService smashOrchestratorService;

    // Track active match timers
    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> matchTimers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, MatchGameState> matchStates = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public GameOrchestratorService(GameBroadcastService broadcastService,
                                    QuestionService questionService,
                                    SmashOrchestratorService smashOrchestratorService) {
        this.broadcastService = broadcastService;
        this.questionService = questionService;
        this.smashOrchestratorService = smashOrchestratorService;
    }

    /**
     * Initialise et démarre le jeu pour un match.
     * Appelé après que le MatchService ait passé le match en status PLAYING.
     */
    @Async
    public void initializeAndStartGame(MatchEntity match) {
        UUID matchId = match.getId();
        log.info("Initializing game for match: {} ({})", match.getCode(), matchId);

        try {
            // Extract player IDs from teams
            List<UUID> teamAPlayers = match.getTeamA().getPlayers().stream()
                    .map(PlayerEntity::getUserId)
                    .toList();
            List<UUID> teamBPlayers = match.getTeamB().getPlayers().stream()
                    .map(PlayerEntity::getUserId)
                    .toList();

            log.info("Match {} has teams: A={}, B={}", matchId, teamAPlayers, teamBPlayers);

            // Initialize match state
            MatchGameState state = new MatchGameState(matchId, match.isDuo());
            matchStates.put(matchId, state);

            // Small delay to let clients connect to WebSocket
            scheduler.schedule(() -> {
                startFirstRound(matchId);
            }, 2, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("Failed to initialize game for match {}: {}", matchId, e.getMessage(), e);
        }
    }

    private void startFirstRound(UUID matchId) {
        MatchGameState state = matchStates.get(matchId);
        if (state == null) {
            log.warn("No game state found for match: {}", matchId);
            return;
        }

        // Start with SMASH_A round for testing
        RoundType roundType = RoundType.SMASH_A;
        state.currentRoundType = roundType;
        state.currentRoundIndex = 1;

        log.info("Starting round {} for match {}", roundType, matchId);

        // Delegate to SmashOrchestratorService for SMASH rounds
        if (roundType == RoundType.SMASH_A || roundType == RoundType.SMASH_B) {
            smashOrchestratorService.startSmashRound(matchId, roundType);
            return;
        }

        // For other round types (CASCADE, etc.)
        // Broadcast round started
        broadcastService.broadcastRoundStarted(matchId, roundType, 1);

        // Send first question after a short delay
        scheduler.schedule(() -> {
            sendNextQuestion(matchId);
        }, 1, TimeUnit.SECONDS);
    }

    private void sendNextQuestion(UUID matchId) {
        MatchGameState state = matchStates.get(matchId);
        if (state == null) {
            log.warn("No game state for match: {}", matchId);
            return;
        }

        state.currentQuestionIndex++;

        // Check if round is complete
        int maxQuestions = getMaxQuestionsForRound(state.currentRoundType);
        if (state.currentQuestionIndex > maxQuestions) {
            endRound(matchId);
            return;
        }

        try {
            // Get a random question for CASCADE round
            List<QuestionEntity> questions = questionService.getRandomByRoundType(
                    state.currentRoundType, 1, null);
            if (questions.isEmpty()) {
                log.warn("No questions available for match {}, using demo questions", matchId);
                // Send a placeholder question for demo
                sendDemoQuestion(matchId, state);
                return;
            }

            QuestionEntity question = questions.get(0);
            state.currentQuestionId = question.getId();
            state.currentCorrectAnswer = question.getAnswer();

            // Broadcast question to all players
            broadcastService.broadcastQuestion(
                    matchId,
                    question.getId(),
                    question.getTextFr(),
                    GameConstants.DEFAULT_ANSWER_TIMEOUT_MS,
                    null,  // Both teams can answer in CASCADE
                    state.currentQuestionIndex
            );

            log.info("Question {} sent for match {}: {}", state.currentQuestionIndex, matchId, question.getTextFr());

            // Schedule timeout
            scheduleQuestionTimeout(matchId, state.currentQuestionId);

        } catch (Exception e) {
            log.error("Error sending question for match {}: {}", matchId, e.getMessage(), e);
            // Send demo question as fallback
            sendDemoQuestion(matchId, state);
        }
    }

    private void sendDemoQuestion(UUID matchId, MatchGameState state) {
        UUID questionId = UUID.randomUUID();
        state.currentQuestionId = questionId;
        state.currentCorrectAnswer = "Paris";

        String[] demoQuestions = {
            "Quelle est la capitale de la France ?",
            "Combien font 2 + 2 ?",
            "Quel est le plus grand océan ?",
            "En quelle année a eu lieu la Révolution française ?",
            "Qui a peint la Joconde ?"
        };
        String[] demoAnswers = {"Paris", "4", "Pacifique", "1789", "Leonard de Vinci"};

        int idx = (state.currentQuestionIndex - 1) % demoQuestions.length;
        state.currentCorrectAnswer = demoAnswers[idx];

        broadcastService.broadcastQuestion(
                matchId,
                questionId,
                demoQuestions[idx],
                GameConstants.DEFAULT_ANSWER_TIMEOUT_MS,
                null,
                state.currentQuestionIndex
        );

        log.info("Demo question {} sent for match {}", state.currentQuestionIndex, matchId);
        scheduleQuestionTimeout(matchId, questionId);
    }

    private void scheduleQuestionTimeout(UUID matchId, UUID questionId) {
        // Cancel any existing timer
        ScheduledFuture<?> existing = matchTimers.get(matchId);
        if (existing != null) {
            existing.cancel(false);
        }

        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            handleQuestionTimeout(matchId, questionId);
        }, GameConstants.DEFAULT_ANSWER_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        matchTimers.put(matchId, timer);
    }

    private void handleQuestionTimeout(UUID matchId, UUID questionId) {
        MatchGameState state = matchStates.get(matchId);
        if (state == null || !questionId.equals(state.currentQuestionId)) {
            return;  // Question already answered or match ended
        }

        log.info("Question timeout for match {}", matchId);

        // Broadcast timeout with correct answer
        broadcastService.broadcastQuestionTimeout(matchId, state.currentCorrectAnswer);

        // Move to next question after a delay
        scheduler.schedule(() -> {
            sendNextQuestion(matchId);
        }, 2, TimeUnit.SECONDS);
    }

    /**
     * Process a player's answer.
     */
    public void processAnswer(UUID matchId, UUID playerId, TeamSide team, String answer) {
        MatchGameState state = matchStates.get(matchId);
        if (state == null || state.currentQuestionId == null) {
            log.warn("No active question for match {} or invalid state", matchId);
            return;
        }

        // Cancel timeout timer
        ScheduledFuture<?> timer = matchTimers.remove(matchId);
        if (timer != null) {
            timer.cancel(false);
        }

        boolean correct = isAnswerCorrect(answer, state.currentCorrectAnswer);
        int points = 0;

        if (correct) {
            // Calculate points based on round type
            points = calculatePoints(state.currentRoundType, state.currentQuestionIndex);
            if (team == TeamSide.A) {
                state.scoreA += points;
            } else {
                state.scoreB += points;
            }
        }

        log.info("Answer from {} (team {}): {} - correct: {}, points: {}",
                playerId, team, answer, correct, points);

        // Broadcast answer result
        broadcastService.broadcastAnswerResult(matchId, playerId, team, correct, points, state.currentCorrectAnswer);

        // Broadcast score update
        broadcastService.broadcastScoreUpdate(matchId, state.scoreA, state.scoreB);

        // Move to next question after a delay
        scheduler.schedule(() -> {
            sendNextQuestion(matchId);
        }, 2, TimeUnit.SECONDS);
    }

    private boolean isAnswerCorrect(String given, String expected) {
        if (given == null || expected == null) return false;
        // Simple comparison, normalize both strings
        return normalize(given).equals(normalize(expected));
    }

    private String normalize(String s) {
        return s.toLowerCase()
                .trim()
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[îï]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ç]", "c");
    }

    private int calculatePoints(RoundType roundType, int questionIndex) {
        return switch (roundType) {
            case CASCADE -> GameConstants.CASCADE_BASE_POINTS + ((questionIndex - 1) * GameConstants.CASCADE_INCREMENT);
            case SMASH_A, SMASH_B -> GameConstants.SMASH_CORRECT_POINTS;
            case SPRINT_FINAL -> GameConstants.SPRINT_POINTS_PER_QUESTION;
            default -> GameConstants.DEFAULT_POINTS;
        };
    }

    private int getMaxQuestionsForRound(RoundType roundType) {
        return switch (roundType) {
            case CASCADE -> GameConstants.CASCADE_QUESTION_COUNT;
            case SMASH_A, SMASH_B -> GameConstants.SMASH_QUESTION_COUNT;
            case SPRINT_FINAL -> GameConstants.SPRINT_FINAL_QUESTION_COUNT;
            case PANIER -> GameConstants.PANIER_QUESTION_COUNT;
            case RELAIS -> GameConstants.RELAIS_QUESTION_COUNT;
            case DUEL -> GameConstants.DUEL_QUESTION_COUNT;
            default -> 5;  // Default for demo
        };
    }

    private void endRound(UUID matchId) {
        MatchGameState state = matchStates.get(matchId);
        if (state == null) return;

        log.info("Round {} ended for match {}. Scores: A={}, B={}",
                state.currentRoundType, matchId, state.scoreA, state.scoreB);

        // Broadcast round ended
        broadcastService.broadcastRoundEnded(matchId, state.currentRoundType, state.scoreA, state.scoreB);

        // For MVP, end the match after first round
        endMatch(matchId);
    }

    private void endMatch(UUID matchId) {
        MatchGameState state = matchStates.remove(matchId);
        if (state == null) return;

        // Cancel any pending timer
        ScheduledFuture<?> timer = matchTimers.remove(matchId);
        if (timer != null) {
            timer.cancel(false);
        }

        // Determine winner
        UUID winnerId = null;
        if (state.scoreA > state.scoreB) {
            winnerId = UUID.randomUUID();  // Team A's ID would go here
        } else if (state.scoreB > state.scoreA) {
            winnerId = UUID.randomUUID();  // Team B's ID would go here
        }

        log.info("Match {} ended. Final scores: A={}, B={}", matchId, state.scoreA, state.scoreB);

        // Broadcast match ended
        broadcastService.broadcastMatchEnded(matchId, winnerId, state.scoreA, state.scoreB);
    }

    /**
     * Internal state for a match game.
     */
    private static class MatchGameState {
        final UUID matchId;
        final boolean duo;
        RoundType currentRoundType;
        int currentRoundIndex = 0;
        int currentQuestionIndex = 0;
        UUID currentQuestionId;
        String currentCorrectAnswer;
        int scoreA = 0;
        int scoreB = 0;

        MatchGameState(UUID matchId, boolean duo) {
            this.matchId = matchId;
            this.duo = duo;
        }
    }
}
