package com.mindsoccer.content.entity;

import com.mindsoccer.protocol.enums.Difficulty;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Entité représentant une question à indices pour les modes JACKPOT et ESTOCADE.
 * Contient 3 indices progressifs avec des points dégressifs.
 *
 * <p>Modes utilisant cette entité :</p>
 * <ul>
 *   <li><b>JACKPOT</b> : Enchères + 3 indices progressifs</li>
 *   <li><b>ESTOCADE</b> : 3 indices, points dégressifs (40 → 30 → 20 → 10)</li>
 * </ul>
 */
@Entity
@Table(name = "ms_jackpot_question", indexes = {
        @Index(name = "idx_jackpot_difficulty", columnList = "difficulty"),
        @Index(name = "idx_jackpot_active", columnList = "active"),
        @Index(name = "idx_jackpot_theme", columnList = "theme_id")
})
public class JackpotQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * La réponse attendue (ex: "LA MITE", "NAPOLÉON", etc.)
     */
    @Column(name = "answer", nullable = false, length = 500)
    private String answer;

    /**
     * Réponses alternatives acceptées.
     */
    @ElementCollection
    @CollectionTable(name = "ms_jackpot_alt_answers", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "alt_answer", length = 500)
    private Set<String> alternativeAnswers = new HashSet<>();

    /**
     * Catégorie de la question (ex: "animaux", "personnages historiques", "lieux")
     */
    @Column(name = "category", length = 100)
    private String category;

    /**
     * Difficulté de la question.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private Difficulty difficulty = Difficulty.MEDIUM;

    /**
     * Thème associé (optionnel).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private ThemeEntity theme;

    /**
     * Premier indice (le plus vague, 40 points si répondu ici).
     * Exemple pour "LA MITE" : "Insecte de la famille des tineides..."
     */
    @Column(name = "hint1_fr", nullable = false, length = 2000)
    private String hint1Fr;

    @Column(name = "hint1_en", length = 2000)
    private String hint1En;

    /**
     * Deuxième indice (plus précis, 30 points si répondu ici).
     * Exemple pour "LA MITE" : "j'existe sous d'autres varietes..."
     */
    @Column(name = "hint2_fr", nullable = false, length = 2000)
    private String hint2Fr;

    @Column(name = "hint2_en", length = 2000)
    private String hint2En;

    /**
     * Troisième indice (très précis, 20 points si répondu ici).
     * Exemple pour "LA MITE" : "je m'attaque aux tissus et aux vetements..."
     */
    @Column(name = "hint3_fr", nullable = false, length = 2000)
    private String hint3Fr;

    @Column(name = "hint3_en", length = 2000)
    private String hint3En;

    /**
     * Média associé (image, audio optionnel).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private MediaEntity media;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "success_count", nullable = false)
    private int successCount = 0;

    /**
     * Compteur de réussites par indice (index 0 = indice 1, etc.)
     */
    @Column(name = "success_at_hint1", nullable = false)
    private int successAtHint1 = 0;

    @Column(name = "success_at_hint2", nullable = false)
    private int successAtHint2 = 0;

    @Column(name = "success_at_hint3", nullable = false)
    private int successAtHint3 = 0;

    @Column(name = "source", length = 200)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public JackpotQuestionEntity() {
    }

    public JackpotQuestionEntity(String answer, String hint1Fr, String hint2Fr, String hint3Fr) {
        this.answer = answer;
        this.hint1Fr = hint1Fr;
        this.hint2Fr = hint2Fr;
        this.hint3Fr = hint3Fr;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getHint1Fr() {
        return hint1Fr;
    }

    public void setHint1Fr(String hint1Fr) {
        this.hint1Fr = hint1Fr;
    }

    public String getHint1En() {
        return hint1En;
    }

    public void setHint1En(String hint1En) {
        this.hint1En = hint1En;
    }

    public String getHint2Fr() {
        return hint2Fr;
    }

    public void setHint2Fr(String hint2Fr) {
        this.hint2Fr = hint2Fr;
    }

    public String getHint2En() {
        return hint2En;
    }

    public void setHint2En(String hint2En) {
        this.hint2En = hint2En;
    }

    public String getHint3Fr() {
        return hint3Fr;
    }

    public void setHint3Fr(String hint3Fr) {
        this.hint3Fr = hint3Fr;
    }

    public String getHint3En() {
        return hint3En;
    }

    public void setHint3En(String hint3En) {
        this.hint3En = hint3En;
    }

    public MediaEntity getMedia() {
        return media;
    }

    public void setMedia(MediaEntity media) {
        this.media = media;
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

    public int getSuccessAtHint1() {
        return successAtHint1;
    }

    public void incrementSuccessAtHint1() {
        this.successAtHint1++;
        this.successCount++;
    }

    public int getSuccessAtHint2() {
        return successAtHint2;
    }

    public void incrementSuccessAtHint2() {
        this.successAtHint2++;
        this.successCount++;
    }

    public int getSuccessAtHint3() {
        return successAtHint3;
    }

    public void incrementSuccessAtHint3() {
        this.successAtHint3++;
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
     * Retourne l'indice localisé selon l'index (1, 2 ou 3) et la locale.
     */
    public String getLocalizedHint(int hintNumber, String locale) {
        return switch (hintNumber) {
            case 1 -> "en".equals(locale) && hint1En != null ? hint1En : hint1Fr;
            case 2 -> "en".equals(locale) && hint2En != null ? hint2En : hint2Fr;
            case 3 -> "en".equals(locale) && hint3En != null ? hint3En : hint3Fr;
            default -> null;
        };
    }

    /**
     * Retourne les points pour une réponse à l'indice donné.
     * Jackpot/Estocade : 40 → 30 → 20 → 10 (question éclair après 3 indices)
     */
    public int getPointsForHint(int hintNumber) {
        return switch (hintNumber) {
            case 0 -> 40;  // Avant tout indice (si applicable)
            case 1 -> 40;  // Premier indice
            case 2 -> 30;  // Deuxième indice
            case 3 -> 20;  // Troisième indice
            default -> 10; // Question éclair (après tous les indices)
        };
    }

    /**
     * Retourne tous les indices dans l'ordre.
     */
    public List<String> getAllHints(String locale) {
        List<String> hints = new ArrayList<>();
        hints.add(getLocalizedHint(1, locale));
        hints.add(getLocalizedHint(2, locale));
        hints.add(getLocalizedHint(3, locale));
        return hints;
    }

    /**
     * Calcule le taux de succès global.
     */
    public double getSuccessRate() {
        return usageCount > 0 ? (double) successCount / usageCount : 0.0;
    }

    /**
     * Vérifie si une réponse est correcte.
     */
    public boolean isCorrectAnswer(String userAnswer) {
        if (userAnswer == null) return false;
        String normalized = userAnswer.trim().toUpperCase();
        if (answer.toUpperCase().equals(normalized)) return true;
        return alternativeAnswers.stream()
                .anyMatch(alt -> alt.toUpperCase().equals(normalized));
    }
}
