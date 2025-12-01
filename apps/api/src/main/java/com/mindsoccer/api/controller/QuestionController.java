package com.mindsoccer.api.controller;

import com.mindsoccer.content.entity.MediaEntity;
import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.content.entity.ThemeEntity;
import com.mindsoccer.content.service.QuestionImportService;
import com.mindsoccer.content.service.QuestionService;
import com.mindsoccer.content.service.QuestionService.QuestionSearchCriteria;
import com.mindsoccer.protocol.dto.common.ApiResponse;
import com.mindsoccer.protocol.dto.common.PageResponse;
import com.mindsoccer.protocol.dto.request.CreateQuestionRequest;
import com.mindsoccer.protocol.dto.response.ImportResultResponse;
import com.mindsoccer.protocol.dto.response.QuestionDetailResponse;
import com.mindsoccer.protocol.dto.response.QuestionStatsResponse;
import com.mindsoccer.protocol.dto.response.ThemeResponse;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.RoundType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "Gestion des questions")
@SecurityRequirement(name = "bearerAuth")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionImportService importService;

    public QuestionController(QuestionService questionService, QuestionImportService importService) {
        this.questionService = questionService;
        this.importService = importService;
    }

    @GetMapping
    @Operation(summary = "Liste des questions", description = "Récupérer la liste paginée des questions")
    public ResponseEntity<ApiResponse<PageResponse<QuestionDetailResponse>>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID themeId,
            @RequestParam(required = false) RoundType roundType,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String search
    ) {
        QuestionSearchCriteria criteria = QuestionSearchCriteria.builder()
                .themeId(themeId)
                .roundType(roundType)
                .difficulty(difficulty)
                .searchText(search)
                .build();

        Page<QuestionEntity> questions = questionService.search(criteria,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        Page<QuestionDetailResponse> responsePage = questions.map(this::toDetailResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Question par ID", description = "Récupérer une question par son identifiant")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> getQuestionById(@PathVariable UUID id) {
        QuestionEntity question = questionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(toDetailResponse(question)));
    }

    @GetMapping("/theme/{themeId}")
    @Operation(summary = "Questions par thème", description = "Récupérer les questions d'un thème")
    public ResponseEntity<ApiResponse<PageResponse<QuestionDetailResponse>>> getByTheme(
            @PathVariable UUID themeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<QuestionEntity> questions = questionService.getByTheme(themeId, PageRequest.of(page, size));
        Page<QuestionDetailResponse> responsePage = questions.map(this::toDetailResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @GetMapping("/round-type/{roundType}")
    @Operation(summary = "Questions par rubrique", description = "Récupérer les questions d'une rubrique")
    public ResponseEntity<ApiResponse<PageResponse<QuestionDetailResponse>>> getByRoundType(
            @PathVariable RoundType roundType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<QuestionEntity> questions = questionService.getByRoundType(roundType, PageRequest.of(page, size));
        Page<QuestionDetailResponse> responsePage = questions.map(this::toDetailResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Statistiques", description = "Obtenir les statistiques des questions (admin)")
    public ResponseEntity<ApiResponse<QuestionStatsResponse>> getStats() {
        Map<Difficulty, Long> byDifficulty = questionService.getCountByDifficulty();
        Map<RoundType, Long> byRoundType = questionService.getCountByRoundType();

        long total = byDifficulty.values().stream().mapToLong(Long::longValue).sum();

        return ResponseEntity.ok(ApiResponse.success(new QuestionStatsResponse(
                total,
                byDifficulty,
                byRoundType,
                new HashMap<>()
        )));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une question", description = "Créer une nouvelle question (admin)")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        QuestionEntity question = new QuestionEntity(request.textFr(), request.answer());
        question.setTextEn(request.textEn());
        question.setTextHt(request.textHt());
        question.setTextFon(request.textFon());
        question.setAlternativeAnswers(request.alternativeAnswers() != null ? request.alternativeAnswers() : new java.util.HashSet<>());
        question.setQuestionFormat(request.format() != null ? request.format() : com.mindsoccer.protocol.enums.QuestionFormat.TEXT);
        question.setDifficulty(request.difficulty() != null ? request.difficulty() : Difficulty.MEDIUM);
        question.setRoundType(request.roundType());
        question.setChoices(request.choices() != null ? request.choices() : new java.util.ArrayList<>());
        question.setCorrectChoiceIndex(request.correctChoiceIndex());
        question.setHintFr(request.hintFr());
        question.setHintEn(request.hintEn());
        question.setExplanationFr(request.explanationFr());
        question.setExplanationEn(request.explanationEn());
        question.setPoints(request.points());
        question.setTimeLimitSeconds(request.timeLimitSeconds());
        question.setImposedLetter(request.imposedLetter());
        question.setSource(request.source());

        question = questionService.create(question, request.themeId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toDetailResponse(question)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier une question", description = "Modifier une question existante (admin)")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> updateQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody CreateQuestionRequest request
    ) {
        QuestionEntity updates = new QuestionEntity(request.textFr(), request.answer());
        updates.setTextEn(request.textEn());
        updates.setTextHt(request.textHt());
        updates.setTextFon(request.textFon());
        updates.setAlternativeAnswers(request.alternativeAnswers() != null ? request.alternativeAnswers() : new java.util.HashSet<>());
        updates.setQuestionFormat(request.format() != null ? request.format() : com.mindsoccer.protocol.enums.QuestionFormat.TEXT);
        updates.setDifficulty(request.difficulty() != null ? request.difficulty() : Difficulty.MEDIUM);
        updates.setRoundType(request.roundType());
        updates.setChoices(request.choices() != null ? request.choices() : new java.util.ArrayList<>());
        updates.setCorrectChoiceIndex(request.correctChoiceIndex());
        updates.setHintFr(request.hintFr());
        updates.setHintEn(request.hintEn());
        updates.setExplanationFr(request.explanationFr());
        updates.setExplanationEn(request.explanationEn());
        updates.setPoints(request.points());
        updates.setTimeLimitSeconds(request.timeLimitSeconds());
        updates.setImposedLetter(request.imposedLetter());
        updates.setSource(request.source());

        QuestionEntity question = questionService.update(id, updates);

        if (request.themeId() != null) {
            questionService.setTheme(id, request.themeId());
            question = questionService.getById(id);
        }

        return ResponseEntity.ok(ApiResponse.success(toDetailResponse(question)));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer une question", description = "Activer une question désactivée (admin)")
    public ResponseEntity<ApiResponse<Void>> activateQuestion(@PathVariable UUID id) {
        questionService.activate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver une question", description = "Désactiver une question (admin)")
    public ResponseEntity<ApiResponse<Void>> deactivateQuestion(@PathVariable UUID id) {
        questionService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une question", description = "Supprimer une question (admin)")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable UUID id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Importer CSV", description = "Importer des questions depuis un fichier CSV (admin)")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importCsv(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        QuestionImportService.ImportResult result = importService.importFromCsv(file.getInputStream());
        return ResponseEntity.ok(ApiResponse.success(toImportResponse(result)));
    }

    @PostMapping(value = "/import/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Importer Excel", description = "Importer des questions depuis un fichier Excel (admin)")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importExcel(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        QuestionImportService.ImportResult result = importService.importFromExcel(file.getInputStream());
        return ResponseEntity.ok(ApiResponse.success(toImportResponse(result)));
    }

    private QuestionDetailResponse toDetailResponse(QuestionEntity question) {
        ThemeEntity theme = question.getTheme();
        MediaEntity media = question.getMedia();

        return new QuestionDetailResponse(
                question.getId(),
                question.getTextFr(),
                question.getTextEn(),
                question.getTextHt(),
                question.getTextFon(),
                question.getAnswer(),
                question.getAlternativeAnswers(),
                question.getQuestionFormat(),
                question.getDifficulty(),
                theme != null ? new ThemeResponse(theme.getId(), theme.getNameFr(), theme.getDescription(), 0, theme.getIconUrl()) : null,
                question.getRoundType(),
                media != null ? new QuestionDetailResponse.MediaResponse(media.getId(), media.getMediaType().name(), media.getUrl(), media.getThumbnailUrl()) : null,
                question.getChoices(),
                question.getCorrectChoiceIndex(),
                question.getHintFr(),
                question.getHintEn(),
                question.getExplanationFr(),
                question.getExplanationEn(),
                question.getPoints(),
                question.getTimeLimitSeconds(),
                question.getImposedLetter(),
                question.isActive(),
                question.getUsageCount(),
                question.getSuccessCount(),
                question.getSuccessRate(),
                question.getSource(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }

    private ImportResultResponse toImportResponse(QuestionImportService.ImportResult result) {
        return ImportResultResponse.of(
                result.importedCount(),
                result.errors().stream()
                        .map(e -> new ImportResultResponse.ImportErrorResponse(e.lineNumber(), e.message()))
                        .toList()
        );
    }
}
