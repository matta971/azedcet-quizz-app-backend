package com.mindsoccer.realtime.service;

import com.mindsoccer.protocol.dto.websocket.*;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.shared.util.GameConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Service de diffusion des événements de jeu en temps réel.
 */
@Service

public class GameBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(GameBroadcastService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public GameBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Diffuse un événement à tous les participants d'un match.
     */
    public void broadcastToMatch(UUID matchId, WsMessage<?> message) {
        String destination = "/topic/match/" + matchId;
        messagingTemplate.convertAndSend(destination, message);
        log.debug("Broadcast to {}: {}", destination, message.type());
    }

    /**
     * Envoie un message à un utilisateur spécifique.
     */
    public void sendToUser(UUID userId, WsMessage<?> message) {
        String destination = "/queue/events";
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, message);
        log.debug("Sent to user {}: {}", userId, message.type());
    }

    // === Événements de Match ===

    /**
     * Diffuse le démarrage d'un match.
     */
    public void broadcastMatchStarted(UUID matchId) {
        WsMessage<MatchStartedPayload> message = WsMessage.of(
                WsEventType.MATCH_STARTED,
                new MatchStartedPayload(matchId, System.currentTimeMillis())
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse la fin d'un match.
     */
    public void broadcastMatchEnded(UUID matchId, UUID winnerId, int scoreA, int scoreB) {
        WsMessage<MatchEndedPayload> message = WsMessage.of(
                WsEventType.MATCH_ENDED,
                new MatchEndedPayload(matchId, winnerId, scoreA, scoreB)
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse la mise à jour des scores.
     */
    public void broadcastScoreUpdate(UUID matchId, int scoreA, int scoreB) {
        WsMessage<ScoreUpdatePayload> message = WsMessage.of(
                WsEventType.SCORE_UPDATED,
                new ScoreUpdatePayload(scoreA, scoreB)
        );
        broadcastToMatch(matchId, message);
    }

    // === Événements de Round ===

    /**
     * Diffuse le début d'une rubrique.
     */
    public void broadcastRoundStarted(UUID matchId, RoundType roundType, int roundNumber) {
        WsMessage<RoundStartedPayload> message = WsMessage.of(
                WsEventType.ROUND_STARTED,
                new RoundStartedPayload(
                        UUID.randomUUID(),  // roundId
                        roundType,          // type
                        roundNumber,        // roundIndex
                        null,               // instruction
                        0,                  // totalQuestions
                        0L                  // durationMs
                )
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse la fin d'une rubrique.
     */
    public void broadcastRoundEnded(UUID matchId, RoundType roundType, int teamAPoints, int teamBPoints) {
        WsMessage<RoundEndedPayload> message = WsMessage.of(
                WsEventType.ROUND_ENDED,
                new RoundEndedPayload(roundType, teamAPoints, teamBPoints)
        );
        broadcastToMatch(matchId, message);
    }

    // === Événements de Question ===

    /**
     * Diffuse une nouvelle question.
     */
    public void broadcastQuestion(UUID matchId, UUID questionId, String text, long timeLimitMs,
                                   TeamSide targetTeam, int questionIndex) {
        WsMessage<QuestionPayload> message = WsMessage.of(
                WsEventType.QUESTION,
                new QuestionPayload(questionId, text, null, timeLimitMs, targetTeam, questionIndex)
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse le résultat d'une réponse.
     */
    public void broadcastAnswerResult(UUID matchId, UUID playerId, TeamSide team,
                                       boolean correct, int points, String correctAnswer) {
        WsMessage<AnswerResultPayload> message = WsMessage.of(
                WsEventType.ANSWER_RESULT,
                new AnswerResultPayload(
                        null,           // questionId
                        playerId,       // playerId
                        null,           // playerHandle
                        team,           // team
                        correct,        // correct
                        null,           // givenAnswer
                        correctAnswer,  // expectedAnswer
                        points,         // pointsAwarded
                        0L,             // responseTimeMs
                        0,              // newTeamScore
                        false           // hasReplyRight
                )
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse un timeout de question.
     */
    public void broadcastQuestionTimeout(UUID matchId, String correctAnswer) {
        WsMessage<Map<String, Object>> message = WsMessage.of(
                WsEventType.QUESTION_TIMEOUT,
                Map.of("correctAnswer", correctAnswer, "timestamp", System.currentTimeMillis())
        );
        broadcastToMatch(matchId, message);
    }

    // === Événements de Buzzer ===

    /**
     * Diffuse un buzzer.
     */
    public void broadcastBuzzer(UUID matchId, UUID playerId, TeamSide team) {
        WsMessage<BuzzerPayload> message = WsMessage.of(
                WsEventType.BUZZER,
                new BuzzerPayload(playerId, team, System.currentTimeMillis())
        );
        broadcastToMatch(matchId, message);
    }

    // === Événements de Joueur ===

    /**
     * Diffuse une pénalité.
     */
    public void broadcastPenalty(UUID matchId, UUID playerId, TeamSide team, int penaltyCount, String reason) {
        boolean suspended = penaltyCount >= 5;
        WsMessage<PenaltyPayload> message = WsMessage.of(
                WsEventType.PENALTY,
                new PenaltyPayload(
                        playerId,       // playerId
                        null,           // playerHandle
                        team,           // team
                        reason,         // reason
                        penaltyCount,   // penaltyCount
                        suspended,      // suspended
                        suspended ? 40 : 0,  // suspensionPointsRemaining
                        false           // bonusQuestionForOpponent
                )
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse une suspension.
     */
    public void broadcastSuspension(UUID matchId, UUID playerId, TeamSide team, int pointsToRecover) {
        WsMessage<Map<String, Object>> message = WsMessage.of(
                WsEventType.PLAYER_SUSPENDED,
                Map.of(
                        "playerId", playerId,
                        "team", team,
                        "pointsToRecover", pointsToRecover
                )
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse la fin d'une suspension.
     */
    public void broadcastSuspensionEnded(UUID matchId, UUID playerId, TeamSide team) {
        WsMessage<Map<String, Object>> message = WsMessage.of(
                WsEventType.SUSPENSION_ENDED,
                Map.of("playerId", playerId, "team", team)
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse qu'un joueur a rejoint.
     */
    public void broadcastPlayerJoined(UUID matchId, UUID playerId, String handle, TeamSide team) {
        WsMessage<PlayerJoinedPayload> message = WsMessage.of(
                WsEventType.PLAYER_JOINED,
                new PlayerJoinedPayload(playerId, handle, team)
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse qu'un joueur a quitté.
     */
    public void broadcastPlayerLeft(UUID matchId, UUID playerId, TeamSide team) {
        WsMessage<Map<String, Object>> message = WsMessage.of(
                WsEventType.PLAYER_LEFT,
                Map.of("playerId", playerId, "team", team)
        );
        broadcastToMatch(matchId, message);
    }

    // === Événements spéciaux ===

    /**
     * Diffuse un tick timer (pour la synchronisation).
     */
    public void broadcastTimerTick(UUID matchId, long remainingMs) {
        WsMessage<TimerTickPayload> message = WsMessage.of(
                WsEventType.TIMER_TICK,
                new TimerTickPayload(remainingMs, System.currentTimeMillis())
        );
        broadcastToMatch(matchId, message);
    }

    /**
     * Diffuse une erreur.
     */
    public void sendError(UUID userId, String errorCode, String message) {
        WsMessage<ErrorPayload> wsMessage = WsMessage.of(
                WsEventType.ERROR,
                new ErrorPayload(errorCode, message)
        );
        sendToUser(userId, wsMessage);
    }

    // === Événements SMASH spécifiques ===

    /**
     * Diffuse le début d'un tour SMASH.
     */
    public void broadcastSmashTurnStart(UUID matchId, int turnNumber, TeamSide attacker, RoundType roundType) {
        WsMessage<SmashTurnPayload> message = WsMessage.of(
                WsEventType.SMASH_TURN_START,
                SmashTurnPayload.of(matchId, turnNumber, attacker, roundType)
        );
        broadcastToMatch(matchId, message);
        log.info("SMASH turn {} started: {} attacks ({})", turnNumber, attacker, roundType);
    }

    /**
     * Diffuse le début de la phase de concertation (SMASH A).
     */
    public void broadcastSmashConcertation(UUID matchId, TeamSide attacker) {
        WsMessage<Map<String, Object>> message = WsMessage.of(
                WsEventType.SMASH_CONCERTATION,
                Map.of(
                        "matchId", matchId.toString(),
                        "attackerTeam", attacker.name(),
                        "message", "Concertation phase - press TOP when ready",
                        "timestamp", System.currentTimeMillis()
                )
        );
        broadcastToMatch(matchId, message);
        log.debug("SMASH concertation phase started for {}", attacker);
    }

    /**
     * Diffuse que l'attaquant a appuyé sur TOP (3s pour poser la question).
     */
    public void broadcastSmashTop(UUID matchId, TeamSide attacker) {
        WsMessage<Map<String, Object>> message = WsMessage.of(
                WsEventType.SMASH_TOP,
                Map.of(
                        "matchId", matchId.toString(),
                        "attackerTeam", attacker.name(),
                        "timeoutMs", GameConstants.SMASH_QUESTION_TIMEOUT_MS,
                        "timestamp", System.currentTimeMillis()
                )
        );
        broadcastToMatch(matchId, message);
        log.info("SMASH TOP pressed by team {}", attacker);
    }

    /**
     * Diffuse une question soumise par l'attaquant.
     */
    public void broadcastSmashQuestionSubmit(UUID matchId, String questionText, TeamSide attacker) {
        WsMessage<SmashQuestionPayload> message = WsMessage.of(
                WsEventType.SMASH_QUESTION_SUBMIT,
                SmashQuestionPayload.of(matchId, questionText, attacker, GameConstants.SMASH_VALIDATE_TIMEOUT_MS)
        );
        broadcastToMatch(matchId, message);
        log.info("SMASH question submitted by team {}: {}", attacker, questionText);
    }

    /**
     * Diffuse la demande de validation au défenseur.
     */
    public void broadcastSmashValidatePrompt(UUID matchId, String questionText, TeamSide defender) {
        WsMessage<Map<String, Object>> message = WsMessage.of(
                WsEventType.SMASH_VALIDATE_PROMPT,
                Map.of(
                        "matchId", matchId.toString(),
                        "questionText", questionText,
                        "defenderTeam", defender.name(),
                        "timeoutMs", GameConstants.SMASH_VALIDATE_TIMEOUT_MS,
                        "timestamp", System.currentTimeMillis()
                )
        );
        broadcastToMatch(matchId, message);
        log.debug("SMASH validate prompt sent to team {}", defender);
    }

    /**
     * Diffuse que la question est validée.
     */
    public void broadcastSmashQuestionValid(UUID matchId, TeamSide validator) {
        WsMessage<SmashValidationPayload> message = WsMessage.of(
                WsEventType.SMASH_QUESTION_VALID,
                SmashValidationPayload.valid(matchId, validator)
        );
        broadcastToMatch(matchId, message);
        log.info("SMASH question validated by team {}", validator);
    }

    /**
     * Diffuse que la question est invalidée.
     */
    public void broadcastSmashQuestionInvalid(UUID matchId, TeamSide validator, String reason, int points, int scoreA, int scoreB) {
        WsMessage<SmashValidationPayload> message = WsMessage.of(
                WsEventType.SMASH_QUESTION_INVALID,
                SmashValidationPayload.invalid(matchId, validator, reason, points)
        );
        broadcastToMatch(matchId, message);
        log.info("SMASH question invalidated by team {}: {} (+{} pts)", validator, reason, points);
    }

    /**
     * Diffuse la demande de réponse au défenseur.
     */
    public void broadcastSmashAnswerPrompt(UUID matchId, String questionText, TeamSide defender) {
        WsMessage<Map<String, Object>> message = WsMessage.of(
                WsEventType.SMASH_ANSWER_PROMPT,
                Map.of(
                        "matchId", matchId.toString(),
                        "questionText", questionText,
                        "defenderTeam", defender.name(),
                        "timeoutMs", GameConstants.SMASH_ANSWER_TIMEOUT_MS,
                        "timestamp", System.currentTimeMillis()
                )
        );
        broadcastToMatch(matchId, message);
        log.debug("SMASH answer prompt sent to team {}", defender);
    }

    /**
     * Diffuse une réponse soumise par le défenseur.
     */
    public void broadcastSmashAnswerSubmit(UUID matchId, String answer, TeamSide defender) {
        WsMessage<SmashAnswerPayload> message = WsMessage.of(
                WsEventType.SMASH_ANSWER_SUBMIT,
                SmashAnswerPayload.of(matchId, answer, defender)
        );
        broadcastToMatch(matchId, message);
        log.info("SMASH answer submitted by team {}: {}", defender, answer);
    }

    /**
     * Diffuse la demande de validation de réponse à l'attaquant.
     */
    public void broadcastSmashResultPrompt(UUID matchId, String answer, TeamSide attacker) {
        WsMessage<Map<String, Object>> message = WsMessage.of(
                WsEventType.SMASH_RESULT_PROMPT,
                Map.of(
                        "matchId", matchId.toString(),
                        "answerText", answer,
                        "attackerTeam", attacker.name(),
                        "timestamp", System.currentTimeMillis()
                )
        );
        broadcastToMatch(matchId, message);
        log.debug("SMASH result prompt sent to team {}", attacker);
    }

    /**
     * Diffuse que la réponse est correcte.
     */
    public void broadcastSmashAnswerCorrect(UUID matchId, TeamSide defender, int points, int scoreA, int scoreB) {
        WsMessage<SmashResultPayload> message = WsMessage.of(
                WsEventType.SMASH_ANSWER_CORRECT,
                SmashResultPayload.correct(matchId, defender, points, scoreA, scoreB)
        );
        broadcastToMatch(matchId, message);
        log.info("SMASH answer correct - team {} wins {} pts (scores: {}-{})", defender, points, scoreA, scoreB);
    }

    /**
     * Diffuse que la réponse est incorrecte.
     */
    public void broadcastSmashAnswerIncorrect(UUID matchId, int scoreA, int scoreB) {
        WsMessage<SmashResultPayload> message = WsMessage.of(
                WsEventType.SMASH_ANSWER_INCORRECT,
                SmashResultPayload.incorrect(matchId, scoreA, scoreB)
        );
        broadcastToMatch(matchId, message);
        log.info("SMASH answer incorrect (scores: {}-{})", scoreA, scoreB);
    }

    /**
     * Diffuse un timeout SMASH.
     */
    public void broadcastSmashTimeout(UUID matchId, String phase, TeamSide faultTeam, TeamSide winner, int points, int scoreA, int scoreB) {
        WsMessage<SmashTimeoutPayload> message = WsMessage.of(
                WsEventType.SMASH_TIMEOUT,
                new SmashTimeoutPayload(matchId, phase, faultTeam, winner, points, scoreA, scoreB, System.currentTimeMillis())
        );
        broadcastToMatch(matchId, message);
        log.info("SMASH timeout in {} phase - {} at fault (scores: {}-{})", phase, faultTeam, scoreA, scoreB);
    }
}
