package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.RoundType;

import java.util.Map;

public record QuestionStatsResponse(
        long totalQuestions,
        Map<Difficulty, Long> byDifficulty,
        Map<RoundType, Long> byRoundType,
        Map<String, Long> byTheme
) {
}
