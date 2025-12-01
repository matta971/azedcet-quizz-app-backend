package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête d'enchère (JACKPOT).
 */
@Schema(description = "Requête d'enchère pour le JACKPOT")
public record BidRequest(

        @Schema(description = "Montant de l'enchère", example = "30", minimum = "1")
        @Min(value = 1, message = "validation.bid.min")
        int amount,

        @Schema(description = "Clé d'idempotence")
        @NotBlank(message = "error.idempotency.key_required")
        String idempotencyKey
) {}
