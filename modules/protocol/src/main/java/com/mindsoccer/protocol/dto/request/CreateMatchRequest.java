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

        @Schema(description = "Nombre maximum de joueurs par équipe. Les deux équipes doivent atteindre ce nombre pour démarrer le match.",
                example = "3", minimum = "1", maximum = "5")
        @Min(1) @Max(5)
        int maxPlayersPerTeam,

        @Schema(description = "Région du match", example = "EU")
        @Size(max = 50)
        String region,

        @Schema(description = "Match classé (ranked)", example = "true")
        boolean ranked,

        @Schema(description = "Rubriques personnalisées (null = toutes)")
        String[] customRounds
) {
    public CreateMatchRequest {
        if (mode == null || mode.isBlank()) mode = "CLASSIC";
        if (maxPlayersPerTeam < 1) maxPlayersPerTeam = 1;
        if (maxPlayersPerTeam > 5) maxPlayersPerTeam = 5;
    }
}
