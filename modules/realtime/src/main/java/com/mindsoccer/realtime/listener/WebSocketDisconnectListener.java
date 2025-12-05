package com.mindsoccer.realtime.listener;

import com.mindsoccer.match.entity.MatchEntity;
import com.mindsoccer.match.service.MatchService;
import com.mindsoccer.protocol.enums.MatchStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * Listener pour les déconnexions WebSocket.
 * Gère automatiquement le départ des joueurs qui se déconnectent sans quitter proprement.
 */
@Component
public class WebSocketDisconnectListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketDisconnectListener.class);

    private final MatchService matchService;

    public WebSocketDisconnectListener(MatchService matchService) {
        this.matchService = matchService;
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();

        if (principal == null) {
            log.debug("WebSocket disconnect with no principal (anonymous session)");
            return;
        }

        String sessionId = event.getSessionId();
        String userName = principal.getName();

        try {
            UUID userId = UUID.fromString(userName);
            log.info("WebSocket disconnect detected for user {} (session: {})", userId, sessionId);

            handlePlayerDisconnect(userId);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid user ID format in principal: {}", userName);
        }
    }

    private void handlePlayerDisconnect(UUID userId) {
        // Trouver les matchs WAITING où le joueur est présent
        List<MatchEntity> waitingMatches = matchService.getPlayerMatches(userId, MatchStatus.WAITING);

        for (MatchEntity match : waitingMatches) {
            try {
                log.info("Auto-leaving WAITING match {} for disconnected user {}", match.getCode(), userId);
                matchService.leaveMatch(match.getId(), userId);
            } catch (Exception e) {
                log.warn("Failed to auto-leave match {} for user {}: {}",
                        match.getCode(), userId, e.getMessage());
            }
        }

        // Pour les matchs PLAYING, on log juste pour le moment
        // Une logique plus complexe (pause, forfait) pourrait être ajoutée plus tard
        List<MatchEntity> playingMatches = matchService.getPlayerMatches(userId, MatchStatus.PLAYING);

        for (MatchEntity match : playingMatches) {
            log.warn("Player {} disconnected from PLAYING match {} - match continues",
                    userId, match.getCode());
            // TODO: Implémenter une logique de pause/timeout/forfait si nécessaire
        }

        if (!waitingMatches.isEmpty() || !playingMatches.isEmpty()) {
            log.info("Disconnect cleanup complete for user {}: {} waiting matches left, {} playing matches affected",
                    userId, waitingMatches.size(), playingMatches.size());
        }
    }
}
