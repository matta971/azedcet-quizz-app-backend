package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Requête de création d'un match.
 */
@Schema(description = "Requête de création d'un match")
public record CreateMatchRequest(

        @Schema(description = "Mode de jeu", example = "CLASSIC", allowableValues = {"CLASSIC", "QUICK", "CUSTOM"})
        String mode,

        @Schema(description = "Taille des équipes", example = "4", minimum = "2", maximum = "5")
        @Min(2) @Max(5)
        int teamSize,

        @Schema(description = "Région du match", example = "EU")
        @Size(max = 50)
        String region,

        @Schema(description = "Match privé", example = "true")
        boolean isPrivate,

        @Schema(description = "Rubriques personnalisées (null = toutes)")
        String[] customRounds
) {
    public CreateMatchRequest {
        if (mode == null || mode.isBlank()) mode = "CLASSIC";
        if (teamSize < 2) teamSize = 2;
        if (teamSize > 5) teamSize = 5;
    }
}
