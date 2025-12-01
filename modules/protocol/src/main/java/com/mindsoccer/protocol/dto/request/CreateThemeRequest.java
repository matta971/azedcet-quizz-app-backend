package com.mindsoccer.protocol.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateThemeRequest(
        @NotBlank(message = "error.validation.code_required")
        @Size(min = 2, max = 50, message = "error.validation.code_length")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "error.validation.code_format")
        String code,

        @NotBlank(message = "error.validation.name_required")
        @Size(max = 200, message = "error.validation.name_length")
        String nameFr,

        @Size(max = 200)
        String nameEn,

        @Size(max = 200)
        String nameHt,

        @Size(max = 200)
        String nameFon,

        @Size(max = 500)
        String description,

        @Size(max = 500)
        String iconUrl
) {
}
