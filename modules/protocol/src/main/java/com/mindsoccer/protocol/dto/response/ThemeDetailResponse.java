package com.mindsoccer.protocol.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ThemeDetailResponse(
        UUID id,
        String code,
        String nameFr,
        String nameEn,
        String nameHt,
        String nameFon,
        String description,
        String iconUrl,
        boolean active,
        long questionCount,
        Instant createdAt,
        Instant updatedAt
) {
}
