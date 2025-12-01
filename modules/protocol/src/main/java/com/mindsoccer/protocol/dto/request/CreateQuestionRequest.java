package com.mindsoccer.protocol.dto.request;

import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.RoundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CreateQuestionRequest(
        @NotBlank(message = "error.validation.text_required")
        @Size(max = 2000, message = "error.validation.text_length")
        String textFr,

        @Size(max = 2000)
        String textEn,

        @Size(max = 2000)
        String textHt,

        @Size(max = 2000)
        String textFon,

        @NotBlank(message = "error.validation.answer_required")
        @Size(max = 500, message = "error.validation.answer_length")
        String answer,

        Set<String> alternativeAnswers,

        QuestionFormat format,

        Difficulty difficulty,

        UUID themeId,

        RoundType roundType,

        UUID mediaId,

        List<String> choices,

        Integer correctChoiceIndex,

        @Size(max = 1000)
        String hintFr,

        @Size(max = 1000)
        String hintEn,

        @Size(max = 2000)
        String explanationFr,

        @Size(max = 2000)
        String explanationEn,

        Integer points,

        Integer timeLimitSeconds,

        @Size(max = 1)
        String imposedLetter,

        @Size(max = 200)
        String source
) {
}
