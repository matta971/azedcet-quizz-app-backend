package com.mindsoccer.protocol.dto.response;

import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.RoundType;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record QuestionDetailResponse(
        UUID id,
        String textFr,
        String textEn,
        String textHt,
        String textFon,
        String answer,
        Set<String> alternativeAnswers,
        QuestionFormat format,
        Difficulty difficulty,
        ThemeResponse theme,
        RoundType roundType,
        MediaResponse media,
        List<String> choices,
        Integer correctChoiceIndex,
        String hintFr,
        String hintEn,
        String explanationFr,
        String explanationEn,
        Integer points,
        Integer timeLimitSeconds,
        String imposedLetter,
        boolean active,
        int usageCount,
        int successCount,
        double successRate,
        String source,
        Instant createdAt,
        Instant updatedAt
) {
    public record MediaResponse(
            UUID id,
            String mediaType,
            String url,
            String thumbnailUrl
    ) {
    }
}
