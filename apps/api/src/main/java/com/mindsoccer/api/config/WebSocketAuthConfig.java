package com.mindsoccer.api.config;

import com.mindsoccer.api.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.UUID;

/**
 * Configuration pour l'authentification JWT sur les connexions WebSocket STOMP.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthConfig.class);

    private final JwtService jwtService;

    public WebSocketAuthConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new JwtAuthChannelInterceptor());
    }

    private class JwtAuthChannelInterceptor implements ChannelInterceptor {

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor == null) {
                return message;
            }

            // Authentifier sur CONNECT
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                log.info("WebSocket CONNECT - Authorization header raw: [{}]", authHeader);

                // Log all native headers for debugging
                if (accessor.getNativeHeader("Authorization") != null) {
                    log.info("All Authorization headers: {}", accessor.getNativeHeader("Authorization"));
                }

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    // Retirer tous les espaces blancs (y compris les retours à la ligne)
                    String token = authHeader.substring(7).replaceAll("\\s+", "");
                    log.info("Extracted token (first 50 chars): {}",
                        token.length() > 50 ? token.substring(0, 50) + "..." : token);
                    try {
                        UUID userId = jwtService.extractUserId(token);
                        log.info("WebSocket authenticated user: {}", userId);

                        // Créer un Principal simple avec l'ID utilisateur
                        Principal principal = () -> userId.toString();
                        accessor.setUser(principal);

                    } catch (JwtException e) {
                        log.warn("WebSocket JWT validation failed: {}", e.getMessage());
                    }
                } else {
                    log.debug("No valid Authorization header in WebSocket CONNECT");
                }
            }

            return message;
        }
    }
}
