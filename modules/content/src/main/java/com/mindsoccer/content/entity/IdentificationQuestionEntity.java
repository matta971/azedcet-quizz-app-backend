package com.mindsoccer.content.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une question d'identification pour la rubrique IDENTIFICATION.
 * Contient 4 indices progressifs (du plus flou au plus net) avec points dégressifs.
 */
@Entity
@Table(name = "ms_identification_question")
public class IdentificationQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "answer", nullable = false, length = 500)
    private String answer;

    @Column(name = "category", length = 100)
    private String category;

    @ElementCollection
    @CollectionTable(name = "ms_identification_hints", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "hint", length = 1000)
    @OrderColumn(name = "hint_order")
    private List<String> hints = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ms_identification_hints_en", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "hint", length = 1000)
    @OrderColumn(name = "hint_order")
    private List<String> hintsEn = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private MediaEntity media;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public IdentificationQuestionEntity() {
    }

    public IdentificationQuestionEntity(String answer, List<String> hints) {
        this.answer = answer;
        this.hints = hints;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getHints() {
        return hints;
    }

    public void setHints(List<String> hints) {
        this.hints = hints;
    }

    public void addHint(String hint) {
        this.hints.add(hint);
    }

    public List<String> getHintsEn() {
        return hintsEn;
    }

    public void setHintsEn(List<String> hintsEn) {
        this.hintsEn = hintsEn;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Retourne l'indice à l'index donné (0 = premier indice, le plus flou).
     */
    public String getHint(int index, String locale) {
        if (index < 0 || index >= hints.size()) {
            return null;
        }
        if ("en".equals(locale) && hintsEn.size() > index) {
            return hintsEn.get(index);
        }
        return hints.get(index);
    }

    /**
     * Retourne les points pour une réponse à l'indice donné.
     * Indice 0 = 40 points, Indice 1 = 30 points, Indice 2 = 20 points, Indice 3 = 10 points.
     */
    public int getPointsForHint(int hintIndex) {
        return switch (hintIndex) {
            case 0 -> 40;
            case 1 -> 30;
            case 2 -> 20;
            default -> 10;
        };
    }
}
