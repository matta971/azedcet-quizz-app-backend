package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.TeamSide;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Réponse pénalité.
 */
@Schema(description = "Informations d'une pénalité")
public record PenaltyResponse(

        @Schema(description = "ID de la pénalité")
        UUID id,

        @Schema(description = "ID du joueur")
        UUID playerId,

        @Schema(description = "Pseudo du joueur")
        String playerHandle,

        @Schema(description = "Équipe")
        TeamSide team,

        @Schema(description = "Raison")
        String reason,

        @Schema(description = "Compteur de pénalités du joueur")
        int playerPenaltyCount,

        @Schema(description = "Joueur suspendu suite à cette pénalité")
        boolean resultedInSuspension,

        @Schema(description = "Date")
        Instant createdAt
) {}
