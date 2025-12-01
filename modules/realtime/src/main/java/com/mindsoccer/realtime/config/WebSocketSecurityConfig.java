package com.mindsoccer.realtime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

/**
 * Configuration de sécurité WebSocket pour Spring Security 6.
 */
@Configuration
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                // Les souscriptions aux topics de match nécessitent une authentification
                .simpSubscribeDestMatchers("/topic/match/**").authenticated()
                .simpSubscribeDestMatchers("/user/queue/**").authenticated()
                // Les messages vers /app nécessitent une authentification
                .simpDestMatchers("/app/**").authenticated()
                // Tout le reste est autorisé
                .anyMessage().permitAll();

        return messages.build();
    }
}
