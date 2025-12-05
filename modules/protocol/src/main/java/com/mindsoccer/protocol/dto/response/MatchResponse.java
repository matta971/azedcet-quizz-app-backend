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

        @Schema(description = "Mode duo (1v1)")
        boolean duo,

        @Schema(description = "Nombre maximum de joueurs par équipe")
        int maxPlayersPerTeam,

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

        @Schema(description = "Le match peut être démarré (les deux équipes sont complètes)")
        boolean canStart,

        @Schema(description = "Date de début")
        Instant startedAt,

        @Schema(description = "Date de fin")
        Instant finishedAt,

        @Schema(description = "Date de création")
        Instant createdAt
) {

    @Schema(description = "Informations équipe")
    public record TeamResponse(
            @Schema(description = "ID de l'équipe")
            UUID id,

            @Schema(description = "Côté de l'équipe (A ou B)")
            TeamSide side,

            @Schema(description = "Nom de l'équipe")
            String name,

            @Schema(description = "ID du capitaine")
            UUID captainId,

            @Schema(description = "Liste des joueurs")
            List<PlayerResponse> players,

            @Schema(description = "Nombre de joueurs actuels")
            int playerCount,

            @Schema(description = "L'équipe est complète")
            boolean isFull
    ) {}

    @Schema(description = "Informations joueur")
    public record PlayerResponse(
            UUID id,
            UUID userId,
            String handle,
            boolean suspended
    ) {}
}
