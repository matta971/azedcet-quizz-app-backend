package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.RoundType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Réponse question.
 */
@Schema(description = "Informations d'une question")
public record QuestionResponse(

        @Schema(description = "ID de la question")
        UUID id,

        @Schema(description = "Type de rubrique")
        RoundType rubric,

        @Schema(description = "Thème")
        String theme,

        @Schema(description = "Langue")
        String locale,

        @Schema(description = "Difficulté (1-5)")
        int difficulty,

        @Schema(description = "Format (TEXT, AUDIO, IMAGE)")
        String format,

        @Schema(description = "Énoncé")
        String statement,

        @Schema(description = "Choix (QCM)")
        List<String> choices,

        @Schema(description = "Indices")
        List<String> hints,

        @Schema(description = "Points par défaut")
        int pointsDefault,

        @Schema(description = "URL du média")
        String mediaUrl
) {}
