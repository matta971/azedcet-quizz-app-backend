package com.mindsoccer.content.service;

import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.content.entity.ThemeEntity;
import com.mindsoccer.content.repository.QuestionRepository;
import com.mindsoccer.content.repository.ThemeRepository;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.shared.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service de gestion des questions.
 */
@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;
    private final ThemeRepository themeRepository;

    public QuestionService(QuestionRepository questionRepository, ThemeRepository themeRepository) {
        this.questionRepository = questionRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public QuestionEntity getById(UUID id) {
        return questionRepository.findById(id)
                .orElseThrow(NotFoundException::question);
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> getAll(Pageable pageable) {
        return questionRepository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> getByTheme(UUID themeId, Pageable pageable) {
        return questionRepository.findByThemeIdAndActiveTrue(themeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> getByRoundType(RoundType roundType, Pageable pageable) {
        return questionRepository.findByRoundTypeAndActiveTrue(roundType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> getByDifficulty(Difficulty difficulty, Pageable pageable) {
        return questionRepository.findByDifficultyAndActiveTrue(difficulty, pageable);
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> search(QuestionSearchCriteria criteria, Pageable pageable) {
        Specification<QuestionEntity> spec = buildSpecification(criteria);
        return questionRepository.findAll(spec, pageable);
    }

    private Specification<QuestionEntity> buildSpecification(QuestionSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("active")));

            if (criteria.themeId() != null) {
                predicates.add(cb.equal(root.get("theme").get("id"), criteria.themeId()));
            }
            if (criteria.roundType() != null) {
                predicates.add(cb.equal(root.get("roundType"), criteria.roundType()));
            }
            if (criteria.difficulty() != null) {
                predicates.add(cb.equal(root.get("difficulty"), criteria.difficulty()));
            }
            if (criteria.format() != null) {
                predicates.add(cb.equal(root.get("questionFormat"), criteria.format()));
            }
            if (criteria.searchText() != null && !criteria.searchText().isBlank()) {
                String search = "%" + criteria.searchText().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("textFr")), search),
                        cb.like(cb.lower(root.get("textEn")), search),
                        cb.like(cb.lower(root.get("answer")), search)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    // === Random Selection for Game ===

    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByTheme(UUID themeId, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByTheme(themeId, exclude, PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByRoundType(RoundType roundType, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByRoundType(roundType, exclude, PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByDifficulty(Difficulty difficulty, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByDifficulty(difficulty, exclude, PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByRoundTypeAndDifficulty(RoundType roundType, Difficulty difficulty,
                                                                   int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByRoundTypeAndDifficulty(roundType, difficulty, exclude,
                PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByThemeAndDifficulty(UUID themeId, Difficulty difficulty,
                                                               int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByThemeAndDifficulty(themeId, difficulty, exclude,
                PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByImposedLetter(String letter, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByImposedLetter(letter.toUpperCase(), exclude, PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByFormat(QuestionFormat format, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByFormat(format, exclude, PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableImposedLetters() {
        return questionRepository.findDistinctImposedLetters();
    }

    // === CRUD Operations ===

    @Transactional
    public QuestionEntity create(QuestionEntity question) {
        return questionRepository.save(question);
    }

    @Transactional
    public QuestionEntity create(QuestionEntity question, UUID themeId) {
        if (themeId != null) {
            ThemeEntity theme = themeRepository.findById(themeId)
                    .orElseThrow(NotFoundException::theme);
            question.setTheme(theme);
        }
        return questionRepository.save(question);
    }

    @Transactional
    public QuestionEntity update(UUID id, QuestionEntity updates) {
        QuestionEntity question = getById(id);

        question.setTextFr(updates.getTextFr());
        question.setTextEn(updates.getTextEn());
        question.setTextHt(updates.getTextHt());
        question.setTextFon(updates.getTextFon());
        question.setAnswer(updates.getAnswer());
        question.setAlternativeAnswers(updates.getAlternativeAnswers());
        question.setQuestionFormat(updates.getQuestionFormat());
        question.setDifficulty(updates.getDifficulty());
        question.setRoundType(updates.getRoundType());
        question.setChoices(updates.getChoices());
        question.setCorrectChoiceIndex(updates.getCorrectChoiceIndex());
        question.setHintFr(updates.getHintFr());
        question.setHintEn(updates.getHintEn());
        question.setExplanationFr(updates.getExplanationFr());
        question.setExplanationEn(updates.getExplanationEn());
        question.setPoints(updates.getPoints());
        question.setTimeLimitSeconds(updates.getTimeLimitSeconds());
        question.setImposedLetter(updates.getImposedLetter());
        question.setSource(updates.getSource());

        return questionRepository.save(question);
    }

    @Transactional
    public void setTheme(UUID questionId, UUID themeId) {
        QuestionEntity question = getById(questionId);
        if (themeId != null) {
            ThemeEntity theme = themeRepository.findById(themeId)
                    .orElseThrow(NotFoundException::theme);
            question.setTheme(theme);
        } else {
            question.setTheme(null);
        }
        questionRepository.save(question);
    }

    @Transactional
    public void activate(UUID id) {
        QuestionEntity question = getById(id);
        question.setActive(true);
        questionRepository.save(question);
    }

    @Transactional
    public void deactivate(UUID id) {
        QuestionEntity question = getById(id);
        question.setActive(false);
        questionRepository.save(question);
    }

    @Transactional
    public void delete(UUID id) {
        QuestionEntity question = getById(id);
        questionRepository.delete(question);
    }

    // === Statistics ===

    @Transactional
    public void recordUsage(UUID questionId, boolean success) {
        questionRepository.incrementUsageCount(questionId);
        if (success) {
            questionRepository.incrementSuccessCount(questionId);
        }
    }

    @Transactional(readOnly = true)
    public Map<Difficulty, Long> getCountByDifficulty() {
        Map<Difficulty, Long> result = new EnumMap<>(Difficulty.class);
        for (Object[] row : questionRepository.countByDifficultyGrouped()) {
            result.put((Difficulty) row[0], (Long) row[1]);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<RoundType, Long> getCountByRoundType() {
        Map<RoundType, Long> result = new EnumMap<>(RoundType.class);
        for (Object[] row : questionRepository.countByRoundTypeGrouped()) {
            result.put((RoundType) row[0], (Long) row[1]);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public long countByTheme(UUID themeId) {
        return questionRepository.countByThemeIdAndActiveTrue(themeId);
    }

    @Transactional(readOnly = true)
    public long countByRoundType(RoundType roundType) {
        return questionRepository.countByRoundTypeAndActiveTrue(roundType);
    }

    // === Search Criteria Record ===

    public record QuestionSearchCriteria(
            UUID themeId,
            RoundType roundType,
            Difficulty difficulty,
            QuestionFormat format,
            String searchText
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private UUID themeId;
            private RoundType roundType;
            private Difficulty difficulty;
            private QuestionFormat format;
            private String searchText;

            public Builder themeId(UUID themeId) {
                this.themeId = themeId;
                return this;
            }

            public Builder roundType(RoundType roundType) {
                this.roundType = roundType;
                return this;
            }

            public Builder difficulty(Difficulty difficulty) {
                this.difficulty = difficulty;
                return this;
            }

            public Builder format(QuestionFormat format) {
                this.format = format;
                return this;
            }

            public Builder searchText(String searchText) {
                this.searchText = searchText;
                return this;
            }

            public QuestionSearchCriteria build() {
                return new QuestionSearchCriteria(themeId, roundType, difficulty, format, searchText);
            }
        }
    }
}
