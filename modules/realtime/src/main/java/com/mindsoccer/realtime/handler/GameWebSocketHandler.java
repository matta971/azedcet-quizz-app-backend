package com.mindsoccer.realtime.handler;

import com.mindsoccer.protocol.dto.websocket.WsEventType;
import com.mindsoccer.protocol.dto.websocket.WsMessage;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.realtime.service.GameBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public GameWebSocketHandler(GameBroadcastService broadcastService) {
        this.broadcastService = broadcastService;
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
}
