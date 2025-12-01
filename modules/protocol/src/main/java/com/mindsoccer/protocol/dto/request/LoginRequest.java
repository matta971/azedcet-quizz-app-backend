package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête de connexion.
 */
@Schema(description = "Requête de connexion")
public record LoginRequest(

        @Schema(description = "Email ou pseudo", example = "user@example.com")
        @NotBlank(message = "validation.required")
        String identifier,

        @Schema(description = "Mot de passe")
        @NotBlank(message = "validation.required")
        String password
) {}
