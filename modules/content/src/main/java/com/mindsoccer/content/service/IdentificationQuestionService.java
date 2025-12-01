package com.mindsoccer.content.service;

import com.mindsoccer.content.entity.IdentificationQuestionEntity;
import com.mindsoccer.content.repository.IdentificationQuestionRepository;
import com.mindsoccer.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service de gestion des questions d'identification.
 */
@Service
public class IdentificationQuestionService {

    private final IdentificationQuestionRepository repository;

    public IdentificationQuestionService(IdentificationQuestionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public IdentificationQuestionEntity getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(NotFoundException::question);
    }

    @Transactional(readOnly = true)
    public Page<IdentificationQuestionEntity> getAll(Pageable pageable) {
        return repository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<IdentificationQuestionEntity> getByCategory(String category, Pageable pageable) {
        return repository.findByCategoryAndActiveTrue(category, pageable);
    }

    @Transactional(readOnly = true)
    public List<IdentificationQuestionEntity> getRandomQuestions(int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return repository.findRandomActive(exclude, PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<IdentificationQuestionEntity> getRandomByCategory(String category, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return repository.findRandomByCategory(category, exclude, PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return repository.findDistinctCategories();
    }

    @Transactional
    public IdentificationQuestionEntity create(IdentificationQuestionEntity question) {
        return repository.save(question);
    }

    @Transactional
    public IdentificationQuestionEntity update(UUID id, IdentificationQuestionEntity updates) {
        IdentificationQuestionEntity question = getById(id);

        question.setAnswer(updates.getAnswer());
        question.setCategory(updates.getCategory());
        question.setHints(updates.getHints());
        question.setHintsEn(updates.getHintsEn());

        return repository.save(question);
    }

    @Transactional
    public void activate(UUID id) {
        IdentificationQuestionEntity question = getById(id);
        question.setActive(true);
        repository.save(question);
    }

    @Transactional
    public void deactivate(UUID id) {
        IdentificationQuestionEntity question = getById(id);
        question.setActive(false);
        repository.save(question);
    }

    @Transactional
    public void delete(UUID id) {
        IdentificationQuestionEntity question = getById(id);
        repository.delete(question);
    }

    @Transactional
    public void recordUsage(UUID questionId) {
        repository.incrementUsageCount(questionId);
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public long countByCategory(String category) {
        return repository.countByCategoryAndActiveTrue(category);
    }
}
