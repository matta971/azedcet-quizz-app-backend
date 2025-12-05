package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.Language;
import com.mindsoccer.protocol.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Réponse d'authentification.
 */
@Schema(description = "Réponse d'authentification")
public record AuthResponse(

        @Schema(description = "Token d'accès JWT")
        String accessToken,

        @Schema(description = "Token de rafraîchissement")
        String refreshToken,

        @Schema(description = "Type de token", example = "Bearer")
        String tokenType,

        @Schema(description = "Durée de validité en secondes")
        long expiresIn,

        @Schema(description = "Informations utilisateur")
        UserInfo user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, UserInfo user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }

    @Schema(description = "Informations utilisateur")
    public record UserInfo(
            UUID id,
            String handle,
            String email,
            UserRole role,
            int rating,
            String firstName,
            String lastName,
            LocalDate birthDate,
            String country,
            Language preferredLanguage
    ) {}
}
