package com.mindsoccer.protocol.dto.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Requête de pagination.
 */
public record PageRequest(
        @Min(0)
        int page,

        @Min(1) @Max(100)
        int size,

        String sortBy,

        SortDirection sortDirection
) {
    public PageRequest {
        if (page < 0) page = 0;
        if (size < 1) size = 20;
        if (size > 100) size = 100;
        if (sortDirection == null) sortDirection = SortDirection.ASC;
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, null, SortDirection.ASC);
    }

    public static PageRequest firstPage() {
        return new PageRequest(0, 20, null, SortDirection.ASC);
    }

    public int getOffset() {
        return page * size;
    }

    public enum SortDirection {
        ASC, DESC
    }
}
