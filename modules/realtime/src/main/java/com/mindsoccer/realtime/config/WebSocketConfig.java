package com.mindsoccer.realtime.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket avec STOMP.
 */
@Configuration
@EnableWebSocketMessageBroker

public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Préfixes pour les destinations vers lesquelles le client s'abonne
        config.enableSimpleBroker("/topic", "/queue");

        // Préfixe pour les messages envoyés par le client vers le serveur
        config.setApplicationDestinationPrefixes("/app");

        // Préfixe pour les messages destinés à un utilisateur spécifique
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Point d'entrée WebSocket natif (sans SockJS)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
