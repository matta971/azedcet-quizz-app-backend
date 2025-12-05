package com.mindsoccer.match.scheduler;

import com.mindsoccer.match.entity.MatchEntity;
import com.mindsoccer.match.repository.MatchRepository;
import com.mindsoccer.protocol.enums.MatchStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduler pour nettoyer les matchs orphelins.
 * - Matchs WAITING depuis plus de 30 minutes : annulés
 * - Matchs PLAYING depuis plus de 2 heures sans activité : annulés
 */
@Component
public class MatchCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchCleanupScheduler.class);

    private static final long WAITING_TIMEOUT_MINUTES = 30;
    private static final long PLAYING_TIMEOUT_HOURS = 2;

    private final MatchRepository matchRepository;

    public MatchCleanupScheduler(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    /**
     * Nettoyage des matchs orphelins toutes les 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cleanupOrphanMatches() {
        int waitingCleaned = cleanupWaitingMatches();
        int playingCleaned = cleanupStalePlayingMatches();

        if (waitingCleaned > 0 || playingCleaned > 0) {
            log.info("Match cleanup completed: {} waiting matches cancelled, {} stale playing matches cancelled",
                    waitingCleaned, playingCleaned);
        }
    }

    private int cleanupWaitingMatches() {
        Instant threshold = Instant.now().minus(WAITING_TIMEOUT_MINUTES, ChronoUnit.MINUTES);
        List<MatchEntity> staleWaiting = matchRepository.findByStatus(MatchStatus.WAITING);

        int count = 0;
        for (MatchEntity match : staleWaiting) {
            if (match.getCreatedAt().isBefore(threshold)) {
                match.setStatus(MatchStatus.CANCELLED);
                matchRepository.save(match);
                log.info("Cancelled stale WAITING match {} (created {})", match.getCode(), match.getCreatedAt());
                count++;
            }
        }
        return count;
    }

    private int cleanupStalePlayingMatches() {
        Instant threshold = Instant.now().minus(PLAYING_TIMEOUT_HOURS, ChronoUnit.HOURS);
        List<MatchEntity> staleMatches = matchRepository.findStaleMatches(threshold);

        for (MatchEntity match : staleMatches) {
            match.setStatus(MatchStatus.CANCELLED);
            matchRepository.save(match);
            log.warn("Cancelled stale PLAYING match {} (last update {})", match.getCode(), match.getUpdatedAt());
        }
        return staleMatches.size();
    }
}
