package com.mindsoccer.protocol.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Requête de soumission d'une réponse.
 */
@Schema(description = "Requête de soumission d'une réponse")
public record SubmitAnswerRequest(

        @Schema(description = "ID de la question")
        @NotNull(message = "validation.required")
        UUID questionId,

        @Schema(description = "Réponse donnée", example = "Paris")
        @NotBlank(message = "validation.required")
        String answer,

        @Schema(description = "Timestamp client (ms)")
        long clientTimestampMs,

        @Schema(description = "Clé d'idempotence")
        @NotBlank(message = "error.idempotency.key_required")
        String idempotencyKey
) {}
