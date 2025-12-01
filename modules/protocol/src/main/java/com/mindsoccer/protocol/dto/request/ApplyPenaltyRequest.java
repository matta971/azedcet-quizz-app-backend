package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Requête d'application d'une pénalité (arbitre).
 */
@Schema(description = "Requête d'application d'une pénalité")
public record ApplyPenaltyRequest(

        @Schema(description = "ID du joueur pénalisé")
        @NotNull(message = "validation.required")
        UUID playerId,

        @Schema(description = "Raison de la pénalité")
        @NotBlank(message = "validation.required")
        String reason,

        @Schema(description = "Clé d'idempotence")
        @NotBlank(message = "error.idempotency.key_required")
        String idempotencyKey
) {}
