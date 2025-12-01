package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Requête pour poser une question SMASH.
 */
@Schema(description = "Requête pour poser une question SMASH")
public record SmashQuestionRequest(

        @Schema(description = "Question posée", example = "Quel sommet de l'Himalaya est le plus haut au Bhoutan?")
        @NotBlank(message = "validation.required")
        @Size(max = 500, message = "validation.question.size")
        String question,

        @Schema(description = "Réponse attendue", example = "CHOMO LARI")
        @NotBlank(message = "validation.required")
        @Size(max = 200, message = "validation.answer.size")
        String expectedAnswer,

        @Schema(description = "Source de vérification", example = "Dictionnaire géographique")
        String source,

        @Schema(description = "Clé d'idempotence")
        @NotBlank(message = "error.idempotency.key_required")
        String idempotencyKey
) {}
