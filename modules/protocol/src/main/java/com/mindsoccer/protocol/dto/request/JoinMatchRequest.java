package com.mindsoccer.protocol.dto.request;

import com.mindsoccer.protocol.enums.TeamSide;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Requête pour rejoindre un match.
 */
@Schema(description = "Requête pour rejoindre un match")
public record JoinMatchRequest(

        @Schema(description = "ID du match")
        @NotNull(message = "validation.required")
        UUID matchId,

        @Schema(description = "Équipe souhaitée", example = "A")
        @NotNull(message = "validation.required")
        TeamSide team,

        @Schema(description = "Code d'accès (match privé)")
        String accessCode
) {}
