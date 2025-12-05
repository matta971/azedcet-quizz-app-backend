package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.Language;
import com.mindsoccer.protocol.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Réponse utilisateur.
 */
@Schema(description = "Informations utilisateur")
public record UserResponse(

        @Schema(description = "ID unique")
        UUID id,

        @Schema(description = "Pseudo")
        String handle,

        @Schema(description = "Email")
        String email,

        @Schema(description = "Rôle")
        UserRole role,

        @Schema(description = "Rating ELO")
        int rating,

        @Schema(description = "Prénom")
        String firstName,

        @Schema(description = "Nom de famille")
        String lastName,

        @Schema(description = "Date de naissance")
        LocalDate birthDate,

        @Schema(description = "Pays (ISO 3166-1 alpha-3)")
        String country,

        @Schema(description = "Langue préférée")
        Language preferredLanguage,

        @Schema(description = "Date de création")
        Instant createdAt
) {}
