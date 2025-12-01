package com.mindsoccer.shared.time;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Service centralisé pour la gestion du temps.
 * Utilise l'horloge serveur comme source de vérité unique.
 */
@Service
public class TimeService {

    private final Clock clock;

    public TimeService() {
        this.clock = Clock.systemUTC();
    }

    /**
     * Constructeur pour les tests (injection d'une horloge fixe).
     */
    public TimeService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Retourne l'instant actuel selon l'horloge serveur.
     */
    public Instant now() {
        return Instant.now(clock);
    }

    /**
     * Retourne le timestamp en millisecondes.
     */
    public long nowMillis() {
        return now().toEpochMilli();
    }

    /**
     * Calcule le temps écoulé depuis un instant donné.
     */
    public Duration elapsed(Instant since) {
        return Duration.between(since, now());
    }

    /**
     * Calcule le temps écoulé en millisecondes.
     */
    public long elapsedMillis(Instant since) {
        return elapsed(since).toMillis();
    }

    /**
     * Vérifie si un timeout est dépassé.
     */
    public boolean isTimeoutExceeded(Instant since, Duration timeout) {
        return elapsed(since).compareTo(timeout) > 0;
    }

    /**
     * Vérifie si un timeout en millisecondes est dépassé.
     */
    public boolean isTimeoutExceeded(Instant since, long timeoutMs) {
        return elapsedMillis(since) > timeoutMs;
    }

    /**
     * Calcule le temps restant avant un timeout.
     */
    public Duration remaining(Instant since, Duration timeout) {
        Duration elapsed = elapsed(since);
        Duration remaining = timeout.minus(elapsed);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * Calcule le temps restant en millisecondes.
     */
    public long remainingMillis(Instant since, long timeoutMs) {
        long elapsed = elapsedMillis(since);
        long remaining = timeoutMs - elapsed;
        return Math.max(0, remaining);
    }
}
