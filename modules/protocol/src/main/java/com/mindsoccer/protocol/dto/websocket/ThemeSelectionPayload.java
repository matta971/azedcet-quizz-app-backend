package com.mindsoccer.protocol.dto.websocket;

import com.mindsoccer.protocol.enums.TeamSide;

import java.util.List;
import java.util.UUID;

/**
 * Payload de sélection de thème.
 */
public record ThemeSelectionPayload(
        TeamSide selectingTeam,
        List<ThemeOption> availableThemes,
        long timeoutMs,
        boolean requiresShooter
) {
    public record ThemeOption(
            UUID id,
            String name,
            String description,
            int questionCount
    ) {}
}
