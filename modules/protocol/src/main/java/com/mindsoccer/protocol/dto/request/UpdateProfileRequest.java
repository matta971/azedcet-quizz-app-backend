package com.mindsoccer.protocol.dto.request;

import com.mindsoccer.protocol.enums.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Requête de mise à jour du profil.
 */
@Schema(description = "Requête de mise à jour du profil utilisateur")
public record UpdateProfileRequest(

        @Schema(description = "Prénom", example = "Jean")
        @Size(max = 100, message = "validation.firstName.size")
        String firstName,

        @Schema(description = "Nom de famille", example = "Dupont")
        @Size(max = 100, message = "validation.lastName.size")
        String lastName,

        @Schema(description = "Date de naissance", example = "1990-05-15")
        @Past(message = "validation.birthDate.past")
        LocalDate birthDate,

        @Schema(description = "Code pays ISO 3166-1 alpha-3", example = "BEN")
        @Size(min = 3, max = 3, message = "validation.country.size")
        String country,

        @Schema(description = "Langue préférée", example = "FR")
        Language preferredLanguage
) {}
