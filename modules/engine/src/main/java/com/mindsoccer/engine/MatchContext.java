package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record MatchContext(
        UUID matchId,
        MatchStatus status,
        RoundType currentRoundType,
        int roundIndex,
        Instant roundStartedAt,
        Map<TeamSide, Integer> scores,
        Map<TeamSide, List<UUID>> players,
        Map<UUID, Integer> penaltyCounts,
        Map<UUID, Boolean> suspendedPlayers,
        TeamSide leadingTeam,
        RoundState currentRoundState
) {

    public boolean isPlayerSuspended(UUID playerId) {
        return suspendedPlayers.getOrDefault(playerId, false);
    }

    public int getPlayerPenaltyCount(UUID playerId) {
        return penaltyCounts.getOrDefault(playerId, 0);
    }

    public long elapsedMillis() {
        return Instant.now().toEpochMilli() - roundStartedAt.toEpochMilli();
    }
}
