package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Requête de sélection de thème (PANIER, RELAIS, CIME).
 */
@Schema(description = "Requête de sélection de thème")
public record SelectThemeRequest(

        @Schema(description = "ID du thème choisi")
        @NotNull(message = "validation.required")
        UUID themeId,

        @Schema(description = "ID du tireur désigné (PANIER)")
        UUID shooterId,

        @Schema(description = "Clé d'idempotence")
        @NotBlank(message = "error.idempotency.key_required")
        String idempotencyKey
) {}
