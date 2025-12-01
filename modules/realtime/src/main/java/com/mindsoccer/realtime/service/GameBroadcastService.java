package com.mindsoccer.realtime.service;

import com.mindsoccer.protocol.dto.websocket.*;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
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
                new RoundStartedPayload(roundType, roundNumber, System.currentTimeMillis())
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
                new AnswerResultPayload(playerId, team, correct, points, correctAnswer)
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
        WsMessage<PenaltyPayload> message = WsMessage.of(
                WsEventType.PENALTY,
                new PenaltyPayload(playerId, team, penaltyCount, reason, penaltyCount >= 5)
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
}
