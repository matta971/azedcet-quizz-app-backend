package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.TeamSide;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Réponse match.
 */
@Schema(description = "Informations d'un match")
public record MatchResponse(

        @Schema(description = "ID du match")
        UUID id,

        @Schema(description = "Code du match")
        String code,

        @Schema(description = "Statut du match")
        MatchStatus status,

        @Schema(description = "Match classé")
        boolean ranked,

        @Schema(description = "Mode duo (2v2)")
        boolean duo,

        @Schema(description = "Équipe A")
        TeamResponse teamA,

        @Schema(description = "Équipe B")
        TeamResponse teamB,

        @Schema(description = "Score équipe A")
        int scoreTeamA,

        @Schema(description = "Score équipe B")
        int scoreTeamB,

        @Schema(description = "Numéro de la rubrique en cours")
        Integer currentRound,

        @Schema(description = "Type de rubrique en cours")
        String currentRoundType,

        @Schema(description = "Date de début")
        Instant startedAt,

        @Schema(description = "Date de fin")
        Instant finishedAt,

        @Schema(description = "Date de création")
        Instant createdAt
) {

    @Schema(description = "Informations équipe")
    public record TeamResponse(
            UUID id,
            TeamSide side,
            String name,
            UUID captainId,
            List<PlayerResponse> players
    ) {}

    @Schema(description = "Informations joueur")
    public record PlayerResponse(
            UUID id,
            UUID userId,
            String handle,
            boolean suspended
    ) {}
}
