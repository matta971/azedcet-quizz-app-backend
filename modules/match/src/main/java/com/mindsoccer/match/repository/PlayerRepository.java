package com.mindsoccer.match.repository;

import com.mindsoccer.match.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, UUID> {

    List<PlayerEntity> findByTeamId(UUID teamId);

    Optional<PlayerEntity> findByTeamIdAndUserId(UUID teamId, UUID userId);

    @Query("SELECT p FROM PlayerEntity p WHERE p.team.match.id = :matchId AND p.userId = :userId")
    Optional<PlayerEntity> findByMatchAndUser(@Param("matchId") UUID matchId, @Param("userId") UUID userId);

    @Query("SELECT p FROM PlayerEntity p WHERE p.team.match.id = :matchId")
    List<PlayerEntity> findByMatchId(@Param("matchId") UUID matchId);

    @Query("SELECT p FROM PlayerEntity p WHERE p.team.match.id = :matchId AND p.suspended = true")
    List<PlayerEntity> findSuspendedByMatch(@Param("matchId") UUID matchId);

    @Query("SELECT COUNT(p) FROM PlayerEntity p WHERE p.team.match.id = :matchId")
    int countByMatchId(@Param("matchId") UUID matchId);

    @Query("SELECT p FROM PlayerEntity p WHERE p.team.id = :teamId AND p.suspended = false")
    List<PlayerEntity> findActiveByTeam(@Param("teamId") UUID teamId);
}
