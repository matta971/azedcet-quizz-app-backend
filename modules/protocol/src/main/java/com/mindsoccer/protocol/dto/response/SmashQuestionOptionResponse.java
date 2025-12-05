package com.mindsoccer.protocol.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Option de question pour le mode SMASH.
 * Contient la question et la réponse attendue (visible uniquement par l'attaquant).
 */
@Schema(description = "Option de question pour SMASH")
public record SmashQuestionOptionResponse(

        @Schema(description = "ID de la question")
        UUID id,

        @Schema(description = "Texte de la question")
        String text,

        @Schema(description = "Réponse attendue")
        String answer,

        @Schema(description = "Difficulté")
        String difficulty
) {}
