package com.mindsoccer.protocol.dto.common;

import java.util.List;

/**
 * Réponse paginée.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                page >= totalPages - 1
        );
    }

    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 0, 20, 0, 0, true, true);
    }
}
