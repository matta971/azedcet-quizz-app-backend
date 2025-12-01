package com.mindsoccer.match.repository;

import com.mindsoccer.match.entity.MatchEntity;
import com.mindsoccer.protocol.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, UUID> {

    Optional<MatchEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<MatchEntity> findByStatus(MatchStatus status);

    Page<MatchEntity> findByStatus(MatchStatus status, Pageable pageable);

    @Query("SELECT m FROM MatchEntity m WHERE m.status = :status ORDER BY m.createdAt DESC")
    List<MatchEntity> findRecentByStatus(@Param("status") MatchStatus status, Pageable pageable);

    @Query("SELECT m FROM MatchEntity m JOIN m.teams t JOIN t.players p " +
            "WHERE p.userId = :userId ORDER BY m.createdAt DESC")
    Page<MatchEntity> findByPlayer(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT m FROM MatchEntity m JOIN m.teams t JOIN t.players p " +
            "WHERE p.userId = :userId AND m.status = :status")
    List<MatchEntity> findByPlayerAndStatus(@Param("userId") UUID userId, @Param("status") MatchStatus status);

    @Query("SELECT m FROM MatchEntity m WHERE m.status = 'PLAYING' AND m.updatedAt < :threshold")
    List<MatchEntity> findStaleMatches(@Param("threshold") Instant threshold);

    @Query("SELECT COUNT(m) FROM MatchEntity m WHERE m.status = :status")
    long countByStatus(@Param("status") MatchStatus status);

    @Query("SELECT m FROM MatchEntity m WHERE m.refereeId = :refereeId AND m.status = :status")
    List<MatchEntity> findByRefereeAndStatus(@Param("refereeId") UUID refereeId, @Param("status") MatchStatus status);

    @Query("SELECT m FROM MatchEntity m WHERE m.status = 'WAITING' AND m.ranked = :ranked ORDER BY m.createdAt ASC")
    List<MatchEntity> findWaitingMatches(@Param("ranked") boolean ranked, Pageable pageable);
}
