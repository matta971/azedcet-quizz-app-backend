package com.mindsoccer.realtime.handler;

import com.mindsoccer.protocol.dto.websocket.WsEventType;
import com.mindsoccer.protocol.dto.websocket.WsMessage;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.realtime.service.GameBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire WebSocket pour les événements de jeu.
 */
@Controller

public class GameWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GameWebSocketHandler.class);

    private final GameBroadcastService broadcastService;
    private final SmashGameHandler smashHandler;

    public GameWebSocketHandler(GameBroadcastService broadcastService,
                                 @Autowired(required = false) SmashGameHandler smashHandler) {
        this.broadcastService = broadcastService;
        this.smashHandler = smashHandler;
        log.info("GameWebSocketHandler initialized - smashHandler: {}", smashHandler != null ? smashHandler.getClass().getSimpleName() : "NULL");
    }

    /**
     * Gère les souscriptions à un match.
     */
    @SubscribeMapping("/match/{matchId}")
    public WsMessage<?> subscribeToMatch(@DestinationVariable UUID matchId, Principal principal) {
        log.info("User {} subscribed to match {}", principal != null ? principal.getName() : "anonymous", matchId);

        return WsMessage.of(WsEventType.CONNECTED, Map.of(
                "matchId", matchId,
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Gère les buzzers (premier à buzzer).
     */
    @MessageMapping("/match/{matchId}/buzzer")
    public void handleBuzzer(@DestinationVariable UUID matchId,
                             @Payload BuzzerRequest request,
                             Principal principal) {
        if (principal == null) {
            return;
        }

        UUID playerId = UUID.fromString(principal.getName());
        log.debug("Buzzer from {} for match {}", playerId, matchId);

        // Le traitement du buzzer sera géré par le GameEngine
        // Ici on diffuse juste l'événement
        broadcastService.broadcastBuzzer(matchId, playerId, request.team());
    }

    /**
     * Gère les réponses des joueurs.
     */
    @MessageMapping("/match/{matchId}/answer")
    public void handleAnswer(@DestinationVariable UUID matchId,
                             @Payload AnswerRequest request,
                             Principal principal) {
        if (principal == null) {
            return;
        }

        UUID playerId = UUID.fromString(principal.getName());
        log.debug("Answer from {} for match {}: {}", playerId, matchId, request.answer());

        // Le traitement de la réponse sera géré par le GameEngine via un service
        // Cette méthode sert de point d'entrée WebSocket
    }

    /**
     * Gère les sélections de thème (PANIER, CIME).
     */
    @MessageMapping("/match/{matchId}/select-theme")
    public void handleThemeSelection(@DestinationVariable UUID matchId,
                                     @Payload ThemeSelectionRequest request,
                                     Principal principal) {
        if (principal == null) {
            return;
        }

        UUID playerId = UUID.fromString(principal.getName());
        log.debug("Theme selection from {} for match {}: {}", playerId, matchId, request.themeId());
    }

    /**
     * Gère les décisions de suspension (choix 4x10 ou 1x40).
     */
    @MessageMapping("/match/{matchId}/suspension-choice")
    public void handleSuspensionChoice(@DestinationVariable UUID matchId,
                                       @Payload SuspensionChoiceRequest request,
                                       Principal principal) {
        if (principal == null) {
            return;
        }

        UUID playerId = UUID.fromString(principal.getName());
        log.debug("Suspension choice from {} for match {}: {}", playerId, matchId, request.choice());
    }

    /**
     * Gère le ping pour maintenir la connexion.
     */
    @MessageMapping("/ping")
    public WsMessage<?> handlePing(Principal principal) {
        return WsMessage.of(WsEventType.PONG, Map.of(
                "timestamp", System.currentTimeMillis()
        ));
    }

    // === SMASH handlers ===

    /**
     * Gère le bouton TOP (SMASH A uniquement).
     */
    @MessageMapping("/match/{matchId}/smash/top")
    public void handleSmashTop(@DestinationVariable UUID matchId,
                                Principal principal) {
        log.info("SMASH TOP handler invoked for match {} - principal: {}, smashHandler: {}",
                matchId, principal != null ? principal.getName() : "null", smashHandler != null ? "present" : "NULL");

        if (principal == null) {
            log.warn("SMASH TOP rejected: principal is null");
            return;
        }
        if (smashHandler == null) {
            log.error("SMASH TOP rejected: smashHandler is NULL - bean injection failed!");
            return;
        }

        UUID playerId = UUID.fromString(principal.getName());
        log.info("SMASH TOP from {} for match {}", playerId, matchId);
        smashHandler.handleTop(matchId, playerId);
    }

    /**
     * Gère la soumission d'une question SMASH.
     */
    @MessageMapping("/match/{matchId}/smash/question")
    public void handleSmashQuestion(@DestinationVariable UUID matchId,
                                     @Payload SmashQuestionRequest request,
                                     Principal principal) {
        if (principal == null || smashHandler == null) {
            return;
        }

        UUID playerId = UUID.fromString(principal.getName());
        log.debug("SMASH question from {} for match {}: {}", playerId, matchId, request.questionText());
        smashHandler.handleQuestionSubmit(matchId, playerId, request.questionText());
    }

    /**
     * Gère la validation/invalidation d'une question SMASH.
     */
    @MessageMapping("/match/{matchId}/smash/validate")
    public void handleSmashValidate(@DestinationVariable UUID matchId,
                                     @Payload SmashValidateRequest request,
                                     Principal principal) {
        if (principal == null || smashHandler == null) {
            return;
        }

        UUID playerId = UUID.fromString(principal.getName());
        log.debug("SMASH validate from {} for match {}: valid={}", playerId, matchId, request.valid());
        smashHandler.handleValidation(matchId, playerId, request.valid(), request.reason());
    }

    /**
     * Gère la soumission d'une réponse SMASH.
     */
    @MessageMapping("/match/{matchId}/smash/answer")
    public void handleSmashAnswer(@DestinationVariable UUID matchId,
                                   @Payload SmashAnswerRequest request,
                                   Principal principal) {
        if (principal == null || smashHandler == null) {
            return;
        }

        UUID playerId = UUID.fromString(principal.getName());
        log.debug("SMASH answer from {} for match {}: {}", playerId, matchId, request.answer());
        smashHandler.handleAnswerSubmit(matchId, playerId, request.answer());
    }

    /**
     * Gère la validation du résultat SMASH (correct/incorrect).
     */
    @MessageMapping("/match/{matchId}/smash/result")
    public void handleSmashResult(@DestinationVariable UUID matchId,
                                   @Payload SmashResultRequest request,
                                   Principal principal) {
        if (principal == null || smashHandler == null) {
            return;
        }

        UUID playerId = UUID.fromString(principal.getName());
        log.debug("SMASH result from {} for match {}: correct={}", playerId, matchId, request.correct());
        smashHandler.handleResultValidation(matchId, playerId, request.correct());
    }

    // === DTOs pour les requêtes ===

    public record BuzzerRequest(TeamSide team, long clientTimestamp) {}

    public record AnswerRequest(
            String answer,
            UUID questionId,
            TeamSide team,
            long clientTimestamp,
            String idempotencyKey
    ) {}

    public record ThemeSelectionRequest(UUID themeId, TeamSide team) {}

    public record SuspensionChoiceRequest(String choice, TeamSide team) {}

    // === SMASH DTOs ===

    public record SmashQuestionRequest(String questionText, TeamSide team) {}

    public record SmashValidateRequest(boolean valid, String reason, TeamSide team) {}

    public record SmashAnswerRequest(String answer, TeamSide team) {}

    public record SmashResultRequest(boolean correct, TeamSide team) {}
}
