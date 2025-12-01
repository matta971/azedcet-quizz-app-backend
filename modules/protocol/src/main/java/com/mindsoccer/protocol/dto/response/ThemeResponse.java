package com.mindsoccer.protocol.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Réponse thème.
 */
@Schema(description = "Informations d'un thème")
public record ThemeResponse(

        @Schema(description = "ID du thème")
        UUID id,

        @Schema(description = "Nom du thème")
        String name,

        @Schema(description = "Description")
        String description,

        @Schema(description = "Nombre de questions disponibles")
        int questionCount,

        @Schema(description = "Icône ou image")
        String iconUrl
) {}
