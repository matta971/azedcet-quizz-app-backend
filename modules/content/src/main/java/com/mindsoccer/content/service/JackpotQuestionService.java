package com.mindsoccer.content.service;

import com.mindsoccer.content.entity.JackpotQuestionEntity;
import com.mindsoccer.content.repository.JackpotQuestionRepository;
import com.mindsoccer.protocol.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service pour gérer les questions à indices (Jackpot, Estocade).
 */
@Service
@Transactional(readOnly = true)
public class JackpotQuestionService {

    private final JackpotQuestionRepository repository;

    public JackpotQuestionService(JackpotQuestionRepository repository) {
        this.repository = repository;
    }

    /**
     * Récupère une question par son ID.
     */
    public Optional<JackpotQuestionEntity> findById(UUID id) {
        return repository.findById(id);
    }

    /**
     * Liste toutes les questions actives avec pagination.
     */
    public Page<JackpotQuestionEntity> findAllActive(Pageable pageable) {
        return repository.findByActiveTrue(pageable);
    }

    /**
     * Liste les questions par difficulté.
     */
    public Page<JackpotQuestionEntity> findByDifficulty(Difficulty difficulty, Pageable pageable) {
        return repository.findByDifficultyAndActiveTrue(difficulty, pageable);
    }

    /**
     * Liste les questions par thème.
     */
    public Page<JackpotQuestionEntity> findByTheme(UUID themeId, Pageable pageable) {
        return repository.findByThemeIdAndActiveTrue(themeId, pageable);
    }

    /**
     * Liste les questions par catégorie.
     */
    public Page<JackpotQuestionEntity> findByCategory(String category, Pageable pageable) {
        return repository.findByCategoryAndActiveTrue(category, pageable);
    }

    /**
     * Récupère N questions aléatoires pour le mode JACKPOT.
     *
     * @param count Le nombre de questions à récupérer
     * @param excludeIds Les IDs de questions à exclure (déjà utilisées)
     * @return Liste de questions aléatoires
     */
    public List<JackpotQuestionEntity> getRandomQuestions(int count, List<UUID> excludeIds) {
        List<UUID> safeExcludeIds = excludeIds != null ? excludeIds : Collections.emptyList();
        return repository.findRandomQuestions(safeExcludeIds, PageRequest.of(0, count));
    }

    /**
     * Récupère N questions aléatoires par difficulté.
     */
    public List<JackpotQuestionEntity> getRandomByDifficulty(Difficulty difficulty, int count, List<UUID> excludeIds) {
        List<UUID> safeExcludeIds = excludeIds != null ? excludeIds : Collections.emptyList();
        return repository.findRandomByDifficulty(difficulty, safeExcludeIds, PageRequest.of(0, count));
    }

    /**
     * Récupère N questions aléatoires par thème.
     */
    public List<JackpotQuestionEntity> getRandomByTheme(UUID themeId, int count, List<UUID> excludeIds) {
        List<UUID> safeExcludeIds = excludeIds != null ? excludeIds : Collections.emptyList();
        return repository.findRandomByTheme(themeId, safeExcludeIds, PageRequest.of(0, count));
    }

    /**
     * Récupère N questions aléatoires par catégorie.
     */
    public List<JackpotQuestionEntity> getRandomByCategory(String category, int count, List<UUID> excludeIds) {
        List<UUID> safeExcludeIds = excludeIds != null ? excludeIds : Collections.emptyList();
        return repository.findRandomByCategory(category, safeExcludeIds, PageRequest.of(0, count));
    }

    /**
     * Crée une nouvelle question.
     */
    @Transactional
    public JackpotQuestionEntity create(JackpotQuestionEntity question) {
        return repository.save(question);
    }

    /**
     * Met à jour une question existante.
     */
    @Transactional
    public JackpotQuestionEntity update(JackpotQuestionEntity question) {
        return repository.save(question);
    }

    /**
     * Désactive une question (soft delete).
     */
    @Transactional
    public void deactivate(UUID id) {
        repository.findById(id).ifPresent(question -> {
            question.setActive(false);
            repository.save(question);
        });
    }

    /**
     * Enregistre l'utilisation d'une question.
     */
    @Transactional
    public void recordUsage(UUID questionId) {
        repository.incrementUsageCount(questionId);
    }

    /**
     * Enregistre une réponse correcte à l'indice 1.
     */
    @Transactional
    public void recordSuccessAtHint1(UUID questionId) {
        repository.incrementSuccessAtHint1(questionId);
    }

    /**
     * Enregistre une réponse correcte à l'indice 2.
     */
    @Transactional
    public void recordSuccessAtHint2(UUID questionId) {
        repository.incrementSuccessAtHint2(questionId);
    }

    /**
     * Enregistre une réponse correcte à l'indice 3.
     */
    @Transactional
    public void recordSuccessAtHint3(UUID questionId) {
        repository.incrementSuccessAtHint3(questionId);
    }

    /**
     * Enregistre une réponse correcte selon le numéro d'indice.
     */
    @Transactional
    public void recordSuccessAtHint(UUID questionId, int hintNumber) {
        switch (hintNumber) {
            case 1 -> recordSuccessAtHint1(questionId);
            case 2 -> recordSuccessAtHint2(questionId);
            case 3 -> recordSuccessAtHint3(questionId);
        }
    }

    /**
     * Retourne les catégories disponibles.
     */
    public List<String> getAvailableCategories() {
        return repository.findDistinctCategories();
    }

    /**
     * Statistiques globales.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", repository.countByActiveTrue());
        stats.put("byDifficulty", repository.countByDifficultyGrouped());
        stats.put("byCategory", repository.countByCategoryGrouped());
        return stats;
    }

    /**
     * Import en lot de questions.
     */
    @Transactional
    public List<JackpotQuestionEntity> importBatch(List<JackpotQuestionEntity> questions) {
        return repository.saveAll(questions);
    }
}
