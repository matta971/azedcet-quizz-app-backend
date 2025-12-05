package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.UUID;

/**
 * Payload pour un timeout SMASH.
 */
public record SmashTimeoutPayload(
        UUID matchId,
        String phase,
        TeamSide faultTeam,
        TeamSide winnerTeam,
        int pointsAwarded,
        int scoreTeamA,
        int scoreTeamB,
        long timestamp
) {
    public static SmashTimeoutPayload questionTimeout(UUID matchId, TeamSide attacker, int points, int scoreA, int scoreB) {
        TeamSide defender = attacker == TeamSide.A ? TeamSide.B : TeamSide.A;
        return new SmashTimeoutPayload(matchId, "QUESTION", attacker, defender, points, scoreA, scoreB, System.currentTimeMillis());
    }

    public static SmashTimeoutPayload validateTimeout(UUID matchId, TeamSide defender, int scoreA, int scoreB) {
        // Si le défenseur ne valide pas à temps, la question est considérée valide par défaut
        return new SmashTimeoutPayload(matchId, "VALIDATE", defender, null, 0, scoreA, scoreB, System.currentTimeMillis());
    }

    public static SmashTimeoutPayload answerTimeout(UUID matchId, TeamSide defender, int scoreA, int scoreB) {
        // Pas de points pour timeout de réponse, juste fin du tour
        return new SmashTimeoutPayload(matchId, "ANSWER", defender, null, 0, scoreA, scoreB, System.currentTimeMillis());
    }
}
