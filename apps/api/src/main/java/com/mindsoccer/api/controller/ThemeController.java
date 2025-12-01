package com.mindsoccer.api.controller;

import com.mindsoccer.content.entity.ThemeEntity;
import com.mindsoccer.content.service.ThemeService;
import com.mindsoccer.protocol.dto.common.ApiResponse;
import com.mindsoccer.protocol.dto.common.PageResponse;
import com.mindsoccer.protocol.dto.request.CreateThemeRequest;
import com.mindsoccer.protocol.dto.response.ThemeDetailResponse;
import com.mindsoccer.protocol.dto.response.ThemeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/themes")
@Tag(name = "Themes", description = "Gestion des thèmes de questions")
@SecurityRequirement(name = "bearerAuth")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    @Operation(summary = "Liste des thèmes", description = "Récupérer la liste paginée des thèmes actifs")
    public ResponseEntity<ApiResponse<PageResponse<ThemeResponse>>> getThemes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        Page<ThemeEntity> themes;
        if (search != null && !search.isBlank()) {
            themes = themeService.search(search, PageRequest.of(page, size, Sort.by("nameFr")));
        } else {
            themes = themeService.getAllActive(PageRequest.of(page, size, Sort.by("nameFr")));
        }

        Page<ThemeResponse> responsePage = themes.map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @GetMapping("/all")
    @Operation(summary = "Tous les thèmes", description = "Récupérer tous les thèmes actifs sans pagination")
    public ResponseEntity<ApiResponse<List<ThemeResponse>>> getAllThemes() {
        List<ThemeResponse> themes = themeService.getAllActive().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(themes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Thème par ID", description = "Récupérer un thème par son identifiant")
    public ResponseEntity<ApiResponse<ThemeDetailResponse>> getThemeById(@PathVariable UUID id) {
        ThemeEntity theme = themeService.getById(id);
        long questionCount = themeService.countQuestions(id);
        return ResponseEntity.ok(ApiResponse.success(toDetailResponse(theme, questionCount)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Thème par code", description = "Récupérer un thème par son code")
    public ResponseEntity<ApiResponse<ThemeDetailResponse>> getThemeByCode(@PathVariable String code) {
        ThemeEntity theme = themeService.getByCode(code);
        long questionCount = themeService.countQuestions(theme.getId());
        return ResponseEntity.ok(ApiResponse.success(toDetailResponse(theme, questionCount)));
    }

    @GetMapping("/random")
    @Operation(summary = "Thèmes aléatoires", description = "Récupérer des thèmes aléatoires pour le jeu")
    public ResponseEntity<ApiResponse<List<ThemeResponse>>> getRandomThemes(
            @RequestParam(defaultValue = "4") int count
    ) {
        List<ThemeResponse> themes = themeService.getRandomThemes(count).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(themes));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un thème", description = "Créer un nouveau thème (admin)")
    public ResponseEntity<ApiResponse<ThemeDetailResponse>> createTheme(
            @Valid @RequestBody CreateThemeRequest request
    ) {
        ThemeEntity theme = new ThemeEntity(request.code(), request.nameFr());
        theme.setNameEn(request.nameEn());
        theme.setNameHt(request.nameHt());
        theme.setNameFon(request.nameFon());
        theme.setDescription(request.description());
        theme.setIconUrl(request.iconUrl());

        theme = themeService.create(theme);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toDetailResponse(theme, 0)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un thème", description = "Modifier un thème existant (admin)")
    public ResponseEntity<ApiResponse<ThemeDetailResponse>> updateTheme(
            @PathVariable UUID id,
            @Valid @RequestBody CreateThemeRequest request
    ) {
        ThemeEntity updates = new ThemeEntity(request.code(), request.nameFr());
        updates.setNameEn(request.nameEn());
        updates.setNameHt(request.nameHt());
        updates.setNameFon(request.nameFon());
        updates.setDescription(request.description());
        updates.setIconUrl(request.iconUrl());

        ThemeEntity theme = themeService.update(id, updates);
        long questionCount = themeService.countQuestions(id);
        return ResponseEntity.ok(ApiResponse.success(toDetailResponse(theme, questionCount)));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer un thème", description = "Activer un thème désactivé (admin)")
    public ResponseEntity<ApiResponse<Void>> activateTheme(@PathVariable UUID id) {
        themeService.activate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Désactiver un thème", description = "Désactiver un thème (admin)")
    public ResponseEntity<ApiResponse<Void>> deactivateTheme(@PathVariable UUID id) {
        themeService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un thème", description = "Supprimer un thème sans questions (admin)")
    public ResponseEntity<ApiResponse<Void>> deleteTheme(@PathVariable UUID id) {
        themeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ThemeResponse toResponse(ThemeEntity theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getCode(),
                theme.getNameFr(),
                theme.getIconUrl()
        );
    }

    private ThemeDetailResponse toDetailResponse(ThemeEntity theme, long questionCount) {
        return new ThemeDetailResponse(
                theme.getId(),
                theme.getCode(),
                theme.getNameFr(),
                theme.getNameEn(),
                theme.getNameHt(),
                theme.getNameFon(),
                theme.getDescription(),
                theme.getIconUrl(),
                theme.isActive(),
                questionCount,
                theme.getCreatedAt(),
                theme.getUpdatedAt()
        );
    }
}
