package com.mindsoccer.match.service;

import com.mindsoccer.match.entity.AnswerHistoryEntity;
import com.mindsoccer.match.repository.AnswerHistoryRepository;
import com.mindsoccer.protocol.enums.RoundType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service de gestion de l'historique des réponses.
 */
@Service
public class AnswerHistoryService {

    private final AnswerHistoryRepository repository;

    public AnswerHistoryService(AnswerHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AnswerHistoryEntity save(AnswerHistoryEntity answer) {
        return repository.save(answer);
    }

    @Transactional(readOnly = true)
    public List<AnswerHistoryEntity> getByMatch(UUID matchId) {
        return repository.findByMatchId(matchId);
    }

    @Transactional(readOnly = true)
    public Page<AnswerHistoryEntity> getByMatch(UUID matchId, Pageable pageable) {
        return repository.findByMatchId(matchId, pageable);
    }

    @Transactional(readOnly = true)
    public List<AnswerHistoryEntity> getByMatchAndPlayer(UUID matchId, UUID playerId) {
        return repository.findByMatchIdAndPlayerId(matchId, playerId);
    }

    @Transactional(readOnly = true)
    public List<AnswerHistoryEntity> getByRound(UUID matchId, int roundNumber) {
        return repository.findByMatchIdAndRoundNumber(matchId, roundNumber);
    }

    @Transactional(readOnly = true)
    public List<AnswerHistoryEntity> getByRoundType(UUID matchId, RoundType roundType) {
        return repository.findByMatchIdAndRoundType(matchId, roundType);
    }

    @Transactional(readOnly = true)
    public boolean hasAnswered(UUID matchId, UUID questionId, UUID playerId) {
        return repository.existsByMatchIdAndQuestionIdAndPlayerId(matchId, questionId, playerId);
    }

    @Transactional(readOnly = true)
    public MatchStats getMatchStats(UUID matchId) {
        List<Object[]> playerStats = repository.getPlayerStatsByMatch(matchId);
        Double avgResponseTime = repository.getAverageResponseTime(matchId);
        long totalCorrect = repository.countCorrectByMatch(matchId);

        return new MatchStats(totalCorrect, avgResponseTime, playerStats);
    }

    @Transactional(readOnly = true)
    public PlayerStats getPlayerStats(UUID matchId, UUID playerId) {
        List<AnswerHistoryEntity> answers = repository.findByMatchIdAndPlayerId(matchId, playerId);

        long totalAnswers = answers.size();
        long correctAnswers = answers.stream().filter(AnswerHistoryEntity::isCorrect).count();
        int totalPoints = answers.stream().mapToInt(AnswerHistoryEntity::getPointsAwarded).sum();
        double avgResponseTime = answers.stream()
                .filter(a -> a.getResponseTimeMs() != null)
                .mapToLong(AnswerHistoryEntity::getResponseTimeMs)
                .average()
                .orElse(0.0);

        return new PlayerStats(playerId, totalAnswers, correctAnswers, totalPoints, avgResponseTime);
    }

    @Transactional(readOnly = true)
    public Integer getTeamTotalPoints(UUID matchId, UUID teamId) {
        return repository.sumPointsByTeam(matchId, teamId);
    }

    public record MatchStats(long totalCorrect, Double averageResponseTimeMs, List<Object[]> playerStats) {
    }

    public record PlayerStats(UUID playerId, long totalAnswers, long correctAnswers, int totalPoints, double avgResponseTimeMs) {
        public double accuracy() {
            return totalAnswers > 0 ? (double) correctAnswers / totalAnswers : 0.0;
        }
    }
}
