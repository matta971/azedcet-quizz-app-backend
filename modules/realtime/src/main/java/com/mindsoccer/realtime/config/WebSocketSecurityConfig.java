package com.mindsoccer.realtime.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

/**
 * Configuration de sécurité WebSocket.
 * Étend AbstractSecurityWebSocketMessageBrokerConfigurer pour configurer
 * les règles d'autorisation sur les messages STOMP.
 */
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                // Permettre à tous de se connecter au broker
                .nullDestMatcher().permitAll()
                // Les souscriptions aux topics de match nécessitent une authentification
                .simpSubscribeDestMatchers("/topic/match/**").authenticated()
                .simpSubscribeDestMatchers("/user/queue/**").authenticated()
                // Les messages vers /app nécessitent une authentification
                .simpDestMatchers("/app/**").authenticated()
                // Tout le reste est autorisé
                .anyMessage().permitAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Désactiver la protection CSRF pour les WebSockets (géré par JWT)
        return true;
    }
}
