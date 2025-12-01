package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Requête de décision CIME (quitter ou doubler).
 */
@Schema(description = "Requête de décision CIME")
public record CimeDecisionRequest(

        @Schema(description = "Décision: QUIT ou DOUBLE")
        @NotNull(message = "validation.required")
        Decision decision,

        @Schema(description = "Clé d'idempotence")
        @NotBlank(message = "error.idempotency.key_required")
        String idempotencyKey
) {
    public enum Decision {
        QUIT,
        DOUBLE
    }
}
