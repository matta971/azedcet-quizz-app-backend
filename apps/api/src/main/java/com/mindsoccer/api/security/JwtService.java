package com.mindsoccer.api.security;

import com.mindsoccer.protocol.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Service de gestion des tokens JWT.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${mindsoccer.security.jwt.secret}") String secret,
            @Value("${mindsoccer.security.jwt.expiration-ms}") long accessTokenExpirationMs,
            @Value("${mindsoccer.security.jwt.refresh-expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Génère un token d'accès.
     */
    public String generateAccessToken(UUID userId, String handle, UserRole role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("handle", handle)
                .claim("role", role.name())
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Génère un token de rafraîchissement.
     */
    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Valide un token et retourne les claims.
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.debug("Invalid token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extrait l'ID utilisateur d'un token.
     */
    public UUID extractUserId(String token) {
        Claims claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extrait le rôle d'un token.
     */
    public UserRole extractRole(String token) {
        Claims claims = validateToken(token);
        return UserRole.valueOf(claims.get("role", String.class));
    }

    /**
     * Vérifie si c'est un token d'accès.
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = validateToken(token);
            return "access".equals(claims.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Vérifie si c'est un token de rafraîchissement.
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = validateToken(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Retourne la durée de validité du token d'accès en secondes.
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }
}
