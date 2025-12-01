package com.mindsoccer.match.repository;

import com.mindsoccer.match.entity.TeamEntity;
import com.mindsoccer.protocol.enums.TeamSide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {

    List<TeamEntity> findByMatchId(UUID matchId);

    Optional<TeamEntity> findByMatchIdAndSide(UUID matchId, TeamSide side);

    @Query("SELECT t FROM TeamEntity t JOIN t.players p WHERE p.userId = :userId AND t.match.id = :matchId")
    Optional<TeamEntity> findByMatchAndPlayer(@Param("matchId") UUID matchId, @Param("userId") UUID userId);

    @Query("SELECT t FROM TeamEntity t WHERE t.match.id = :matchId AND SIZE(t.players) < :maxSize")
    List<TeamEntity> findAvailableTeams(@Param("matchId") UUID matchId, @Param("maxSize") int maxSize);
}
