package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête de rafraîchissement de token.
 */
@Schema(description = "Requête de rafraîchissement de token")
public record RefreshTokenRequest(

        @Schema(description = "Token de rafraîchissement")
        @NotBlank(message = "validation.required")
        String refreshToken
) {}
