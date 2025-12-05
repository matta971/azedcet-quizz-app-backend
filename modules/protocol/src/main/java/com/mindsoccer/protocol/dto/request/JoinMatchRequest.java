package com.mindsoccer.protocol.dto.request;

import com.mindsoccer.protocol.enums.TeamSide;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Requête pour rejoindre un match.
 * Tous les champs sont optionnels car le matchId est dans l'URL
 * et le team peut être auto-assigné.
 */
@Schema(description = "Requête pour rejoindre un match")
public record JoinMatchRequest(

        @Schema(description = "Équipe souhaitée (A ou B). Si non spécifié, auto-assigné.", example = "B")
        TeamSide team,

        @Schema(description = "Code d'accès (pour les matchs privés)")
        String accessCode
) {}
