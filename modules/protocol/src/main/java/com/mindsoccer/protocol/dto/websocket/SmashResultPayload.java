package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

/**
 * Payload pour le résultat d'une réponse SMASH (validée par l'attaquant).
 */
public record SmashResultPayload(
        UUID matchId,
        boolean correct,
        TeamSide winnerTeam,
        int pointsAwarded,
        int scoreTeamA,
        int scoreTeamB,
        long timestamp
) {
    public static SmashResultPayload correct(UUID matchId, TeamSide winner, int points, int scoreA, int scoreB) {
        return new SmashResultPayload(matchId, true, winner, points, scoreA, scoreB, System.currentTimeMillis());
    }

    public static SmashResultPayload incorrect(UUID matchId, int scoreA, int scoreB) {
        return new SmashResultPayload(matchId, false, null, 0, scoreA, scoreB, System.currentTimeMillis());
    }
}
