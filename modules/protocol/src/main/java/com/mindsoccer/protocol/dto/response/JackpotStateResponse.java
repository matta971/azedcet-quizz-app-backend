package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.TeamSide;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * État du JACKPOT.
 */
@Schema(description = "État du JACKPOT")
public record JackpotStateResponse(

        @Schema(description = "Cagnottes restantes par équipe")
        Map<TeamSide, Integer> pools,

        @Schema(description = "Index de la question (1-3)")
        int questionIndex,

        @Schema(description = "Niveau d'indice actuel (1-3)")
        int currentHintLevel,

        @Schema(description = "Indice actuel")
        String currentHint,

        @Schema(description = "Enchère la plus haute")
        BidInfo highestBid,

        @Schema(description = "Phase: BIDDING, ANSWERING, RESOLVED")
        String phase
) {
    @Schema(description = "Information sur une enchère")
    public record BidInfo(
            TeamSide team,
            int amount
    ) {}
}
