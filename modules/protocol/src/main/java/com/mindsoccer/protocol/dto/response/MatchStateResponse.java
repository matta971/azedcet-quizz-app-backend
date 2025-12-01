package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

/**
 * État complet d'un match (pour synchronisation).
 */
@Schema(description = "État complet d'un match")
public record MatchStateResponse(

        @Schema(description = "ID du match")
        UUID matchId,

        @Schema(description = "Statut du match")
        MatchStatus status,

        @Schema(description = "Scores par équipe")
        Map<TeamSide, Integer> scores,

        @Schema(description = "Équipe en tête")
        TeamSide leadingTeam,

        @Schema(description = "État de la rubrique en cours")
        RoundStateInfo currentRound,

        @Schema(description = "Timestamp serveur")
        long serverTimestamp
) {

    @Schema(description = "État d'une rubrique")
    public record RoundStateInfo(
            UUID roundId,
            RoundType type,
            String phase,
            int questionIndex,
            int totalQuestions,
            QuestionInfo currentQuestion,
            UUID activePlayerId,
            long remainingTimeMs,
            Map<String, Object> extra
    ) {}

    @Schema(description = "Information sur la question en cours")
    public record QuestionInfo(
            UUID id,
            String statement,
            String[] choices,
            String hint,
            int pointsValue,
            String mediaUrl
    ) {}
}
