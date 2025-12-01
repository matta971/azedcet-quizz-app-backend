package com.mindsoccer.content.service;

import com.mindsoccer.content.entity.ThemeEntity;
import com.mindsoccer.content.repository.ThemeRepository;
import com.mindsoccer.shared.exception.NotFoundException;
import com.mindsoccer.shared.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service de gestion des thèmes.
 */
@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public ThemeEntity getById(UUID id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundException::theme);
    }

    @Transactional(readOnly = true)
    public ThemeEntity getByCode(String code) {
        return themeRepository.findByCode(code)
                .orElseThrow(NotFoundException::theme);
    }

    @Transactional(readOnly = true)
    public Page<ThemeEntity> getAllActive(Pageable pageable) {
        return themeRepository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public List<ThemeEntity> getAllActive() {
        return themeRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Page<ThemeEntity> search(String query, Pageable pageable) {
        return themeRepository.searchThemes(query, pageable);
    }

    @Transactional(readOnly = true)
    public List<ThemeEntity> getRandomThemes(int count) {
        return themeRepository.findRandomActiveThemes(PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<ThemeEntity> getRandomThemesExcluding(int count, List<UUID> excludeIds) {
        if (excludeIds == null || excludeIds.isEmpty()) {
            return getRandomThemes(count);
        }
        return themeRepository.findRandomActiveThemesExcluding(excludeIds, PageRequest.of(0, count));
    }

    @Transactional
    public ThemeEntity create(ThemeEntity theme) {
        if (themeRepository.existsByCode(theme.getCode())) {
            throw ValidationException.themeCodeExists();
        }
        return themeRepository.save(theme);
    }

    @Transactional
    public ThemeEntity update(UUID id, ThemeEntity updates) {
        ThemeEntity theme = getById(id);

        if (!theme.getCode().equals(updates.getCode()) && themeRepository.existsByCode(updates.getCode())) {
            throw ValidationException.themeCodeExists();
        }

        theme.setCode(updates.getCode());
        theme.setNameFr(updates.getNameFr());
        theme.setNameEn(updates.getNameEn());
        theme.setNameHt(updates.getNameHt());
        theme.setNameFon(updates.getNameFon());
        theme.setDescription(updates.getDescription());
        theme.setIconUrl(updates.getIconUrl());

        return themeRepository.save(theme);
    }

    @Transactional
    public void activate(UUID id) {
        ThemeEntity theme = getById(id);
        theme.setActive(true);
        themeRepository.save(theme);
    }

    @Transactional
    public void deactivate(UUID id) {
        ThemeEntity theme = getById(id);
        theme.setActive(false);
        themeRepository.save(theme);
    }

    @Transactional
    public void delete(UUID id) {
        ThemeEntity theme = getById(id);
        long questionCount = themeRepository.countActiveQuestionsByThemeId(id);
        if (questionCount > 0) {
            throw ValidationException.themeHasQuestions();
        }
        themeRepository.delete(theme);
    }

    @Transactional(readOnly = true)
    public long countQuestions(UUID themeId) {
        return themeRepository.countActiveQuestionsByThemeId(themeId);
    }
}
