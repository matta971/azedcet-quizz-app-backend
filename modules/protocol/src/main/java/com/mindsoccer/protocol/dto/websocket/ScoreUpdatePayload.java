package com.mindsoccer.protocol.dto.websocket;

public record ScoreUpdatePayload(
        int scoreTeamA,
        int scoreTeamB
) {}
