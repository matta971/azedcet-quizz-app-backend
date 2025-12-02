package com.mindsoccer.realtime.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * Configuration de sécurité WebSocket pour Spring Security 6.
 * Pour l'instant, nous permettons toutes les connexions WebSocket.
 * L'authentification est gérée au niveau HTTP (JWT) avant l'upgrade WebSocket.
 */
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        // Permettre toutes les connexions pour l'instant
        // L'authentification est vérifiée au niveau HTTP/JWT
        messages.anyMessage().permitAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Désactiver la vérification CSRF pour les WebSockets (nécessaire pour SockJS)
        return true;
    }
}
