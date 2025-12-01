package com.mindsoccer.content.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entité représentant un thème de questions.
 * Les thèmes sont utilisés pour catégoriser les questions
 * et pour les mécaniques comme PANIER ou CIME.
 */
@Entity
@Table(name = "ms_theme")
public class ThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "name_fr", nullable = false, length = 200)
    private String nameFr;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "name_ht", length = 200)
    private String nameHt;

    @Column(name = "name_fon", length = 200)
    private String nameFon;

    @Column(length = 500)
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public ThemeEntity() {
    }

    public ThemeEntity(String code, String nameFr) {
        this.code = code;
        this.nameFr = nameFr;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameFr() {
        return nameFr;
    }

    public void setNameFr(String nameFr) {
        this.nameFr = nameFr;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameHt() {
        return nameHt;
    }

    public void setNameHt(String nameHt) {
        this.nameHt = nameHt;
    }

    public String getNameFon() {
        return nameFon;
    }

    public void setNameFon(String nameFon) {
        this.nameFon = nameFon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Retourne le nom localisé selon la locale.
     */
    public String getLocalizedName(String locale) {
        return switch (locale) {
            case "en" -> nameEn != null ? nameEn : nameFr;
            case "ht" -> nameHt != null ? nameHt : nameFr;
            case "fon" -> nameFon != null ? nameFon : nameFr;
            default -> nameFr;
        };
    }
}
