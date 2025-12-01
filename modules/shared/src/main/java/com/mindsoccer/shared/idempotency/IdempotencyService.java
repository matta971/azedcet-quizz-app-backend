package com.mindsoccer.shared.idempotency;

import com.mindsoccer.shared.exception.IdempotencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service de gestion de l'idempotence via Redis.
 * Garantit qu'une action avec la même clé ne soit exécutée qu'une seule fois.
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Vérifie si une clé d'idempotence existe déjà.
     * @param key la clé d'idempotence
     * @return true si la clé existe (action déjà effectuée)
     */
    public boolean exists(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        Boolean exists = redisTemplate.hasKey(buildKey(key));
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Tente de réserver une clé d'idempotence.
     * @param key la clé d'idempotence
     * @return true si la réservation a réussi (première fois)
     */
    public boolean tryReserve(String key) {
        return tryReserve(key, DEFAULT_TTL);
    }

    /**
     * Tente de réserver une clé d'idempotence avec un TTL personnalisé.
     * @param key la clé d'idempotence
     * @param ttl durée de vie de la clé
     * @return true si la réservation a réussi (première fois)
     */
    public boolean tryReserve(String key, Duration ttl) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String redisKey = buildKey(key);
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "reserved", ttl.toMillis(), TimeUnit.MILLISECONDS);

        if (Boolean.TRUE.equals(success)) {
            log.debug("Idempotency key reserved: {}", key);
            return true;
        }
        log.debug("Idempotency key already exists: {}", key);
        return false;
    }

    /**
     * Réserve une clé ou lève une exception si elle existe déjà.
     * @param key la clé d'idempotence
     * @throws IdempotencyException si la clé existe déjà
     */
    public void reserveOrThrow(String key) {
        reserveOrThrow(key, DEFAULT_TTL);
    }

    /**
     * Réserve une clé ou lève une exception si elle existe déjà.
     * @param key la clé d'idempotence
     * @param ttl durée de vie de la clé
     * @throws IdempotencyException si la clé existe déjà
     */
    public void reserveOrThrow(String key, Duration ttl) {
        if (key == null || key.isBlank()) {
            throw IdempotencyException.keyRequired();
        }
        if (!tryReserve(key, ttl)) {
            throw IdempotencyException.duplicate();
        }
    }

    /**
     * Stocke un résultat associé à une clé d'idempotence.
     * @param key la clé d'idempotence
     * @param result le résultat à stocker
     */
    public void storeResult(String key, String result) {
        storeResult(key, result, DEFAULT_TTL);
    }

    /**
     * Stocke un résultat associé à une clé d'idempotence.
     * @param key la clé d'idempotence
     * @param result le résultat à stocker
     * @param ttl durée de vie
     */
    public void storeResult(String key, String result, Duration ttl) {
        if (key == null || key.isBlank()) {
            return;
        }
        redisTemplate.opsForValue().set(buildKey(key), result, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Récupère le résultat stocké pour une clé.
     * @param key la clé d'idempotence
     * @return le résultat si présent
     */
    public Optional<String> getResult(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        String result = redisTemplate.opsForValue().get(buildKey(key));
        return Optional.ofNullable(result);
    }

    /**
     * Supprime une clé d'idempotence (rollback).
     * @param key la clé d'idempotence
     */
    public void release(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        redisTemplate.delete(buildKey(key));
        log.debug("Idempotency key released: {}", key);
    }

    private String buildKey(String key) {
        return KEY_PREFIX + key;
    }
}
