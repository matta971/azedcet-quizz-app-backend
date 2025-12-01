package com.mindsoccer.match.repository;

import com.mindsoccer.match.entity.AnswerHistoryEntity;
import com.mindsoccer.protocol.enums.RoundType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerHistoryRepository extends JpaRepository<AnswerHistoryEntity, UUID> {

    List<AnswerHistoryEntity> findByMatchId(UUID matchId);

    Page<AnswerHistoryEntity> findByMatchId(UUID matchId, Pageable pageable);

    List<AnswerHistoryEntity> findByMatchIdAndPlayerId(UUID matchId, UUID playerId);

    List<AnswerHistoryEntity> findByMatchIdAndRoundNumber(UUID matchId, int roundNumber);

    List<AnswerHistoryEntity> findByMatchIdAndRoundType(UUID matchId, RoundType roundType);

    @Query("SELECT COUNT(a) FROM AnswerHistoryEntity a WHERE a.matchId = :matchId AND a.correct = true")
    long countCorrectByMatch(@Param("matchId") UUID matchId);

    @Query("SELECT COUNT(a) FROM AnswerHistoryEntity a WHERE a.matchId = :matchId AND a.playerId = :playerId AND a.correct = true")
    long countCorrectByPlayer(@Param("matchId") UUID matchId, @Param("playerId") UUID playerId);

    @Query("SELECT SUM(a.pointsAwarded) FROM AnswerHistoryEntity a WHERE a.matchId = :matchId AND a.teamId = :teamId")
    Integer sumPointsByTeam(@Param("matchId") UUID matchId, @Param("teamId") UUID teamId);

    @Query("SELECT a.playerId, COUNT(a), SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END), SUM(a.pointsAwarded) " +
            "FROM AnswerHistoryEntity a WHERE a.matchId = :matchId GROUP BY a.playerId")
    List<Object[]> getPlayerStatsByMatch(@Param("matchId") UUID matchId);

    @Query("SELECT AVG(a.responseTimeMs) FROM AnswerHistoryEntity a WHERE a.matchId = :matchId AND a.correct = true AND a.responseTimeMs IS NOT NULL")
    Double getAverageResponseTime(@Param("matchId") UUID matchId);

    boolean existsByMatchIdAndQuestionIdAndPlayerId(UUID matchId, UUID questionId, UUID playerId);
}
