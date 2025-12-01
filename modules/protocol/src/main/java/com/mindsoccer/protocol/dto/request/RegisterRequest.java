package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Requête d'inscription.
 */
@Schema(description = "Requête d'inscription d'un nouvel utilisateur")
public record RegisterRequest(

        @Schema(description = "Pseudo unique", example = "champion2024")
        @NotBlank(message = "validation.required")
        @Size(min = 3, max = 50, message = "validation.handle.size")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "validation.handle.pattern")
        String handle,

        @Schema(description = "Adresse email", example = "user@example.com")
        @NotBlank(message = "validation.required")
        @Email(message = "validation.email.invalid")
        String email,

        @Schema(description = "Mot de passe", example = "SecurePass123!")
        @NotBlank(message = "validation.required")
        @Size(min = 8, max = 100, message = "validation.password.size")
        String password,

        @Schema(description = "Code pays ISO 3166-1 alpha-3", example = "BEN")
        @Size(min = 3, max = 3, message = "validation.country.size")
        String country
) {}
