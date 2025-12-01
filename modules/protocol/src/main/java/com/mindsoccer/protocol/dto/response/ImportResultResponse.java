package com.mindsoccer.protocol.dto.response;

import java.util.List;

public record ImportResultResponse(
        int importedCount,
        int errorCount,
        List<ImportErrorResponse> errors
) {
    public record ImportErrorResponse(
            int lineNumber,
            String message
    ) {
    }

    public static ImportResultResponse of(int importedCount, List<ImportErrorResponse> errors) {
        return new ImportResultResponse(importedCount, errors.size(), errors);
    }
}
