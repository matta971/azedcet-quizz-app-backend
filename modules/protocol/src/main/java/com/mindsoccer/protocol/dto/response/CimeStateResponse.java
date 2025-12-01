package com.mindsoccer.protocol.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * État de la CIME.
 */
@Schema(description = "État de la CIME")
public record CimeStateResponse(

        @Schema(description = "Joueur actif")
        UUID activePlayerId,

        @Schema(description = "Position sur l'échelle (0-10)")
        int currentStep,

        @Schema(description = "Points accumulés")
        int accumulatedPoints,

        @Schema(description = "Jokers utilisés")
        List<UUID> usedJokers,

        @Schema(description = "Jokers restants")
        int remainingJokers,

        @Schema(description = "Phase: NEGOTIATION, CLIMBING, DECISION, COMPLETED")
        String phase,

        @Schema(description = "Thème choisi")
        String theme,

        @Schema(description = "Bonus potentiel si arrêt maintenant")
        int potentialBonus
) {}
