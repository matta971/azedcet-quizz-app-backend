package com.mindsoccer.content.service;

import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.content.entity.ThemeEntity;
import com.mindsoccer.content.repository.QuestionRepository;
import com.mindsoccer.content.repository.ThemeRepository;
import com.mindsoccer.protocol.enums.Country;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.QuestionType;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.shared.exception.NotFoundException;
import org.hibernate.Hibernate;
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
        QuestionEntity question = questionRepository.findById(id)
                .orElseThrow(NotFoundException::question);
        initializeLazyCollections(question);
        return question;
    }

    /**
     * Initialise les collections lazy d'une question pour éviter LazyInitializationException.
     * Doit être appelé dans un contexte transactionnel.
     */
    private void initializeLazyCollections(QuestionEntity question) {
        Hibernate.initialize(question.getAlternativeAnswers());
        Hibernate.initialize(question.getRoundTypes());
        Hibernate.initialize(question.getCategories());
        Hibernate.initialize(question.getChoices());
        if (question.getTheme() != null) {
            Hibernate.initialize(question.getTheme());
        }
        if (question.getMedia() != null) {
            Hibernate.initialize(question.getMedia());
        }
    }

    /**
     * Initialise les collections lazy pour une liste de questions.
     */
    private void initializeLazyCollections(Iterable<QuestionEntity> questions) {
        for (QuestionEntity question : questions) {
            initializeLazyCollections(question);
        }
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> getAll(Pageable pageable) {
        Page<QuestionEntity> page = questionRepository.findByActiveTrue(pageable);
        initializeLazyCollections(page.getContent());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> getByTheme(UUID themeId, Pageable pageable) {
        Page<QuestionEntity> page = questionRepository.findByThemeIdAndActiveTrue(themeId, pageable);
        initializeLazyCollections(page.getContent());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> getByRoundType(RoundType roundType, Pageable pageable) {
        Page<QuestionEntity> page = questionRepository.findByRoundTypeAndActiveTrue(roundType, pageable);
        initializeLazyCollections(page.getContent());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> getByDifficulty(Difficulty difficulty, Pageable pageable) {
        Page<QuestionEntity> page = questionRepository.findByDifficultyAndActiveTrue(difficulty, pageable);
        initializeLazyCollections(page.getContent());
        return page;
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> search(QuestionSearchCriteria criteria, Pageable pageable) {
        Specification<QuestionEntity> spec = buildSpecification(criteria);
        Page<QuestionEntity> page = questionRepository.findAll(spec, pageable);
        initializeLazyCollections(page.getContent());
        return page;
    }

    private Specification<QuestionEntity> buildSpecification(QuestionSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("active")));

            if (criteria.themeId() != null) {
                predicates.add(cb.equal(root.get("theme").get("id"), criteria.themeId()));
            }
            if (criteria.roundType() != null) {
                // Recherche dans la collection roundTypes
                predicates.add(cb.isMember(criteria.roundType(), root.get("roundTypes")));
            }
            if (criteria.category() != null && !criteria.category().isBlank()) {
                // Recherche dans la collection categories
                predicates.add(cb.isMember(criteria.category().toLowerCase(), root.get("categories")));
            }
            if (criteria.difficulty() != null) {
                predicates.add(cb.equal(root.get("difficulty"), criteria.difficulty()));
            }
            if (criteria.format() != null) {
                predicates.add(cb.equal(root.get("questionFormat"), criteria.format()));
            }
            if (criteria.questionType() != null) {
                predicates.add(cb.equal(root.get("questionType"), criteria.questionType()));
            }
            if (criteria.country() != null) {
                predicates.add(cb.equal(root.get("country"), criteria.country()));
            }
            if (criteria.imposedLetter() != null && !criteria.imposedLetter().isBlank()) {
                predicates.add(cb.equal(root.get("imposedLetter"), criteria.imposedLetter().toUpperCase()));
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

    // === Game Mode Specific Methods ===

    /**
     * Pour SAUT_PATRIOTIQUE : Questions par pays.
     */
    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByCountry(Country country, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByCountry(country, exclude, PageRequest.of(0, count));
    }

    /**
     * Pour différents modes : Questions par type.
     */
    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByQuestionType(QuestionType type, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByQuestionType(type, exclude, PageRequest.of(0, count));
    }

    /**
     * Pour RANDONNEE_LEXICALE : Questions alphabétiques par lettre.
     */
    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomAlphabeticByLetter(String letter, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomAlphabeticByLetter(letter.toUpperCase(), exclude, PageRequest.of(0, count));
    }

    /**
     * Pour SPRINT_FINAL : Questions rapides (éclairs).
     */
    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomEclairQuestions(int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomEclairQuestions(exclude, PageRequest.of(0, count));
    }

    /**
     * Pour CAPOEIRA : Questions sur le thème musique.
     */
    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomMusicQuestions(int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomMusicQuestions(exclude, PageRequest.of(0, count));
    }

    /**
     * Pour ECHAPPEE : Questions géographiques.
     */
    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomGeographicQuestions(int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomGeographicQuestions(exclude, PageRequest.of(0, count));
    }

    /**
     * Pour CIME : Retourne 10 questions de difficulté croissante.
     * 3 EASY, 3 MEDIUM, 4 HARD (dont le sommet).
     */
    @Transactional(readOnly = true)
    public List<QuestionEntity> getQuestionsForCime(Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        List<QuestionEntity> questions = new ArrayList<>();

        // 3 questions faciles (paliers 1-3)
        questions.addAll(questionRepository.findRandomForCime(Difficulty.EASY, exclude, PageRequest.of(0, 3)));

        // 3 questions moyennes (paliers 4-6)
        List<UUID> usedIds = new ArrayList<>(exclude);
        questions.forEach(q -> usedIds.add(q.getId()));
        questions.addAll(questionRepository.findRandomForCime(Difficulty.MEDIUM, usedIds, PageRequest.of(0, 3)));

        // 4 questions difficiles (paliers 7-10, dont le sommet)
        questions.forEach(q -> usedIds.add(q.getId()));
        questions.addAll(questionRepository.findRandomForCime(Difficulty.HARD, usedIds, PageRequest.of(0, 4)));

        return questions;
    }

    /**
     * Pour RANDONNEE_LEXICALE : Retourne 26 questions (une par lettre A-Z).
     */
    @Transactional(readOnly = true)
    public Map<String, QuestionEntity> getQuestionsForRandonneeLexicale(Set<UUID> excludeIds) {
        Map<String, QuestionEntity> questions = new LinkedHashMap<>();
        List<UUID> usedIds = excludeIds != null ? new ArrayList<>(excludeIds) : new ArrayList<>();

        for (char letter = 'A'; letter <= 'Z'; letter++) {
            String letterStr = String.valueOf(letter);
            List<QuestionEntity> found = getRandomAlphabeticByLetter(letterStr, 1, new HashSet<>(usedIds));
            if (!found.isEmpty()) {
                QuestionEntity q = found.get(0);
                questions.put(letterStr, q);
                usedIds.add(q.getId());
            }
        }

        return questions;
    }

    /**
     * Retourne le nombre de questions disponibles par pays.
     */
    @Transactional(readOnly = true)
    public Map<Country, Long> getCountByCountry() {
        Map<Country, Long> result = new EnumMap<>(Country.class);
        for (Object[] row : questionRepository.countByCountryGrouped()) {
            result.put((Country) row[0], (Long) row[1]);
        }
        return result;
    }

    /**
     * Retourne le nombre de questions disponibles par type.
     */
    @Transactional(readOnly = true)
    public long countByCountry(Country country) {
        return questionRepository.countByCountryAndActiveTrue(country);
    }

    /**
     * Retourne le nombre de questions disponibles par type.
     */
    @Transactional(readOnly = true)
    public long countByQuestionType(QuestionType type) {
        return questionRepository.countByQuestionTypeAndActiveTrue(type);
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
        question.setRoundTypes(updates.getRoundTypes());
        question.setCategories(updates.getCategories());
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

    // === Category Methods ===

    @Transactional(readOnly = true)
    public List<QuestionEntity> getRandomByCategory(String category, int count, Set<UUID> excludeIds) {
        List<UUID> exclude = excludeIds != null ? new ArrayList<>(excludeIds) : List.of();
        return questionRepository.findRandomByCategory(category.toLowerCase(), exclude, PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> getByCategory(String category, Pageable pageable) {
        return questionRepository.findByCategoryAndActiveTrue(category.toLowerCase(), pageable);
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableCategories() {
        return questionRepository.findDistinctCategories();
    }

    @Transactional(readOnly = true)
    public long countByCategory(String category) {
        return questionRepository.countByCategoryAndActiveTrue(category.toLowerCase());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCountByCategory() {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : questionRepository.countByCategoryGrouped()) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    // === Search Criteria Record ===

    public record QuestionSearchCriteria(
            UUID themeId,
            RoundType roundType,
            String category,
            Difficulty difficulty,
            QuestionFormat format,
            QuestionType questionType,
            Country country,
            String imposedLetter,
            String searchText
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private UUID themeId;
            private RoundType roundType;
            private String category;
            private Difficulty difficulty;
            private QuestionFormat format;
            private QuestionType questionType;
            private Country country;
            private String imposedLetter;
            private String searchText;

            public Builder themeId(UUID themeId) {
                this.themeId = themeId;
                return this;
            }

            public Builder roundType(RoundType roundType) {
                this.roundType = roundType;
                return this;
            }

            public Builder category(String category) {
                this.category = category;
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

            public Builder questionType(QuestionType questionType) {
                this.questionType = questionType;
                return this;
            }

            public Builder country(Country country) {
                this.country = country;
                return this;
            }

            public Builder imposedLetter(String imposedLetter) {
                this.imposedLetter = imposedLetter;
                return this;
            }

            public Builder searchText(String searchText) {
                this.searchText = searchText;
                return this;
            }

            public QuestionSearchCriteria build() {
                return new QuestionSearchCriteria(themeId, roundType, category, difficulty, format,
                        questionType, country, imposedLetter, searchText);
            }
        }
    }
}
