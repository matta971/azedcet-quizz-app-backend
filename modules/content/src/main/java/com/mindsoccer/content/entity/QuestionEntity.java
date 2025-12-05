package com.mindsoccer.content.entity;

import com.mindsoccer.protocol.enums.Country;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.QuestionType;
import com.mindsoccer.protocol.enums.RoundType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Entité représentant une question du jeu MINDSOCCER.
 * Supporte différents formats et peut être associée à des médias.
 */
@Entity
@Table(name = "ms_question", indexes = {
        @Index(name = "idx_question_theme", columnList = "theme_id"),
        @Index(name = "idx_question_difficulty", columnList = "difficulty"),
        @Index(name = "idx_question_active", columnList = "active"),
        @Index(name = "idx_question_country", columnList = "country"),
        @Index(name = "idx_question_type", columnList = "question_type"),
        @Index(name = "idx_question_imposed_letter", columnList = "imposed_letter")
})
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "text_fr", nullable = false, length = 2000)
    private String textFr;

    @Column(name = "text_en", length = 2000)
    private String textEn;

    @Column(name = "text_ht", length = 2000)
    private String textHt;

    @Column(name = "text_fon", length = 2000)
    private String textFon;

    @Column(name = "answer", nullable = false, length = 500)
    private String answer;

    @ElementCollection
    @CollectionTable(name = "ms_question_alt_answers", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "alt_answer", length = 500)
    private Set<String> alternativeAnswers = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "question_format", nullable = false, length = 30)
    private QuestionFormat questionFormat = QuestionFormat.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private ThemeEntity theme;

    /**
     * Modes de jeu pour lesquels cette question peut être utilisée.
     * Une question peut appartenir à plusieurs modes (DUEL, CASCADE, MARATHON, etc.)
     */
    @ElementCollection(targetClass = RoundType.class)
    @CollectionTable(name = "ms_question_round_types", joinColumns = @JoinColumn(name = "question_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", length = 30)
    private Set<RoundType> roundTypes = new HashSet<>();

    /**
     * Catégories de la question (ex: "peinture", "monuments", "presidents", etc.)
     * Permet une classification plus fine que le thème.
     */
    @ElementCollection
    @CollectionTable(name = "ms_question_categories", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "category", length = 100)
    private Set<String> categories = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", length = 30)
    private QuestionType questionType = QuestionType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "country", length = 10)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private MediaEntity media;

    @ElementCollection
    @CollectionTable(name = "ms_question_choices", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "choice", length = 500)
    @OrderColumn(name = "choice_order")
    private List<String> choices = new ArrayList<>();

    @Column(name = "correct_choice_index")
    private Integer correctChoiceIndex;

    @Column(name = "hint_fr", length = 1000)
    private String hintFr;

    @Column(name = "hint_en", length = 1000)
    private String hintEn;

    @Column(name = "explanation_fr", length = 2000)
    private String explanationFr;

    @Column(name = "explanation_en", length = 2000)
    private String explanationEn;

    @Column(name = "points")
    private Integer points;

    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    @Column(name = "imposed_letter", length = 1)
    private String imposedLetter;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "success_count", nullable = false)
    private int successCount = 0;

    @Column(name = "source", length = 200)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public QuestionEntity() {
    }

    public QuestionEntity(String textFr, String answer) {
        this.textFr = textFr;
        this.answer = answer;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTextFr() {
        return textFr;
    }

    public void setTextFr(String textFr) {
        this.textFr = textFr;
    }

    public String getTextEn() {
        return textEn;
    }

    public void setTextEn(String textEn) {
        this.textEn = textEn;
    }

    public String getTextHt() {
        return textHt;
    }

    public void setTextHt(String textHt) {
        this.textHt = textHt;
    }

    public String getTextFon() {
        return textFon;
    }

    public void setTextFon(String textFon) {
        this.textFon = textFon;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Set<String> getAlternativeAnswers() {
        return alternativeAnswers;
    }

    public void setAlternativeAnswers(Set<String> alternativeAnswers) {
        this.alternativeAnswers = alternativeAnswers;
    }

    public void addAlternativeAnswer(String answer) {
        this.alternativeAnswers.add(answer);
    }

    public QuestionFormat getQuestionFormat() {
        return questionFormat;
    }

    public void setQuestionFormat(QuestionFormat questionFormat) {
        this.questionFormat = questionFormat;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public ThemeEntity getTheme() {
        return theme;
    }

    public void setTheme(ThemeEntity theme) {
        this.theme = theme;
    }

    public Set<RoundType> getRoundTypes() {
        return roundTypes;
    }

    public void setRoundTypes(Set<RoundType> roundTypes) {
        this.roundTypes = roundTypes;
    }

    public void addRoundType(RoundType roundType) {
        this.roundTypes.add(roundType);
    }

    public boolean hasRoundType(RoundType roundType) {
        return this.roundTypes.contains(roundType);
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public void addCategory(String category) {
        this.categories.add(category.toLowerCase());
    }

    public boolean hasCategory(String category) {
        return this.categories.contains(category.toLowerCase());
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public MediaEntity getMedia() {
        return media;
    }

    public void setMedia(MediaEntity media) {
        this.media = media;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public Integer getCorrectChoiceIndex() {
        return correctChoiceIndex;
    }

    public void setCorrectChoiceIndex(Integer correctChoiceIndex) {
        this.correctChoiceIndex = correctChoiceIndex;
    }

    public String getHintFr() {
        return hintFr;
    }

    public void setHintFr(String hintFr) {
        this.hintFr = hintFr;
    }

    public String getHintEn() {
        return hintEn;
    }

    public void setHintEn(String hintEn) {
        this.hintEn = hintEn;
    }

    public String getExplanationFr() {
        return explanationFr;
    }

    public void setExplanationFr(String explanationFr) {
        this.explanationFr = explanationFr;
    }

    public String getExplanationEn() {
        return explanationEn;
    }

    public void setExplanationEn(String explanationEn) {
        this.explanationEn = explanationEn;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public String getImposedLetter() {
        return imposedLetter;
    }

    public void setImposedLetter(String imposedLetter) {
        this.imposedLetter = imposedLetter;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void incrementSuccessCount() {
        this.successCount++;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Retourne le texte localisé selon la locale.
     */
    public String getLocalizedText(String locale) {
        return switch (locale) {
            case "en" -> textEn != null ? textEn : textFr;
            case "ht" -> textHt != null ? textHt : textFr;
            case "fon" -> textFon != null ? textFon : textFr;
            default -> textFr;
        };
    }

    /**
     * Calcule le taux de succès.
     */
    public double getSuccessRate() {
        return usageCount > 0 ? (double) successCount / usageCount : 0.0;
    }
}
