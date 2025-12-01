package com.mindsoccer.match.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entité représentant un joueur participant à un match.
 */
@Entity
@Table(name = "ms_player", indexes = {
        @Index(name = "idx_player_team", columnList = "team_id"),
        @Index(name = "idx_player_user", columnList = "user_id")
})
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String handle;

    @Column(name = "rating_before")
    private Integer ratingBefore;

    @Column(name = "rating_after")
    private Integer ratingAfter;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers = 0;

    @Column(name = "wrong_answers", nullable = false)
    private int wrongAnswers = 0;

    @Column(name = "points_earned", nullable = false)
    private int pointsEarned = 0;

    @Column(name = "penalty_count", nullable = false)
    private int penaltyCount = 0;

    @Column(nullable = false)
    private boolean suspended = false;

    @Column(name = "suspension_points_remaining")
    private Integer suspensionPointsRemaining;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    public PlayerEntity() {
    }

    public PlayerEntity(UUID userId, String handle) {
        this.userId = userId;
        this.handle = handle;
    }

    @PrePersist
    protected void onCreate() {
        joinedAt = Instant.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TeamEntity getTeam() {
        return team;
    }

    public void setTeam(TeamEntity team) {
        this.team = team;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public Integer getRatingBefore() {
        return ratingBefore;
    }

    public void setRatingBefore(Integer ratingBefore) {
        this.ratingBefore = ratingBefore;
    }

    public Integer getRatingAfter() {
        return ratingAfter;
    }

    public void setRatingAfter(Integer ratingAfter) {
        this.ratingAfter = ratingAfter;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void incrementCorrectAnswers() {
        this.correctAnswers++;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public void incrementWrongAnswers() {
        this.wrongAnswers++;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public void addPoints(int points) {
        this.pointsEarned += points;
    }

    public int getPenaltyCount() {
        return penaltyCount;
    }

    public void setPenaltyCount(int penaltyCount) {
        this.penaltyCount = penaltyCount;
    }

    public void addPenalty() {
        this.penaltyCount++;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Integer getSuspensionPointsRemaining() {
        return suspensionPointsRemaining;
    }

    public void setSuspensionPointsRemaining(Integer suspensionPointsRemaining) {
        this.suspensionPointsRemaining = suspensionPointsRemaining;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    // Helper methods

    public void suspend(int pointsToRecover) {
        this.suspended = true;
        this.suspensionPointsRemaining = pointsToRecover;
    }

    public void reduceSuspension(int points) {
        if (suspended && suspensionPointsRemaining != null) {
            this.suspensionPointsRemaining -= points;
            if (this.suspensionPointsRemaining <= 0) {
                this.suspended = false;
                this.suspensionPointsRemaining = null;
            }
        }
    }

    public int getTotalAnswers() {
        return correctAnswers + wrongAnswers;
    }

    public double getAccuracy() {
        int total = getTotalAnswers();
        return total > 0 ? (double) correctAnswers / total : 0.0;
    }
}
