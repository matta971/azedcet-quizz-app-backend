package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête de décision SAUT PATRIOTIQUE ("En voulez-vous?").
 */
@Schema(description = "Requête de décision SAUT PATRIOTIQUE")
public record SautPatriotiqueDecisionRequest(

        @Schema(description = "Accepter la question", example = "true")
        @NotNull(message = "validation.required")
        Boolean accept,

        @Schema(description = "Clé d'idempotence")
        @NotBlank(message = "error.idempotency.key_required")
        String idempotencyKey
) {}
