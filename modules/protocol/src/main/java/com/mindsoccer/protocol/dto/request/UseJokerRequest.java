package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Requête d'utilisation d'un joker (CIME).
 */
@Schema(description = "Requête d'utilisation d'un joker")
public record UseJokerRequest(

        @Schema(description = "ID du coéquipier joker")
        @NotNull(message = "validation.required")
        UUID helperId,

        @Schema(description = "Clé d'idempotence")
        @NotBlank(message = "error.idempotency.key_required")
        String idempotencyKey
) {}
