package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.TeamSide;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Résultat d'une réponse.
 */
@Schema(description = "Résultat d'une réponse")
public record AnswerResultResponse(

        @Schema(description = "ID de la question")
        UUID questionId,

        @Schema(description = "ID du joueur")
        UUID playerId,

        @Schema(description = "Équipe")
        TeamSide team,

        @Schema(description = "Réponse correcte")
        boolean correct,

        @Schema(description = "Points attribués")
        int pointsAwarded,

        @Schema(description = "Réponse attendue")
        String expectedAnswer,

        @Schema(description = "Réponse donnée")
        String givenAnswer,

        @Schema(description = "Temps de réponse (ms)")
        long responseTimeMs,

        @Schema(description = "Nouveau score de l'équipe")
        int newTeamScore
) {}
