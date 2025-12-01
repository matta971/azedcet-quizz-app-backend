package com.mindsoccer.match.entity;

import com.mindsoccer.protocol.enums.RoundType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entité pour l'historique des réponses données pendant un match.
 * Permet le replay et l'analyse post-match.
 */
@Entity
@Table(name = "ms_answer_history", indexes = {
        @Index(name = "idx_answer_match", columnList = "match_id"),
        @Index(name = "idx_answer_player", columnList = "player_id"),
        @Index(name = "idx_answer_question", columnList = "question_id")
})
public class AnswerHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false, length = 30)
    private RoundType roundType;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "question_index")
    private Integer questionIndex;

    @Column(name = "given_answer", length = 500)
    private String givenAnswer;

    @Column(name = "expected_answer", length = 500)
    private String expectedAnswer;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "points_awarded", nullable = false)
    private int pointsAwarded = 0;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "was_buzzer")
    private boolean wasBuzzer = false;

    @Column(name = "was_timeout")
    private boolean wasTimeout = false;

    @Column(name = "was_smashed")
    private boolean wasSmashed = false;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    public AnswerHistoryEntity() {
    }

    @PrePersist
    protected void onCreate() {
        answeredAt = Instant.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public void setMatchId(UUID matchId) {
        this.matchId = matchId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public RoundType getRoundType() {
        return roundType;
    }

    public void setRoundType(RoundType roundType) {
        this.roundType = roundType;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Integer getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(Integer questionIndex) {
        this.questionIndex = questionIndex;
    }

    public String getGivenAnswer() {
        return givenAnswer;
    }

    public void setGivenAnswer(String givenAnswer) {
        this.givenAnswer = givenAnswer;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public boolean isWasBuzzer() {
        return wasBuzzer;
    }

    public void setWasBuzzer(boolean wasBuzzer) {
        this.wasBuzzer = wasBuzzer;
    }

    public boolean isWasTimeout() {
        return wasTimeout;
    }

    public void setWasTimeout(boolean wasTimeout) {
        this.wasTimeout = wasTimeout;
    }

    public boolean isWasSmashed() {
        return wasSmashed;
    }

    public void setWasSmashed(boolean wasSmashed) {
        this.wasSmashed = wasSmashed;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(Instant answeredAt) {
        this.answeredAt = answeredAt;
    }

    // Builder pattern for convenience

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AnswerHistoryEntity entity = new AnswerHistoryEntity();

        public Builder matchId(UUID matchId) {
            entity.setMatchId(matchId);
            return this;
        }

        public Builder playerId(UUID playerId) {
            entity.setPlayerId(playerId);
            return this;
        }

        public Builder teamId(UUID teamId) {
            entity.setTeamId(teamId);
            return this;
        }

        public Builder questionId(UUID questionId) {
            entity.setQuestionId(questionId);
            return this;
        }

        public Builder roundType(RoundType roundType) {
            entity.setRoundType(roundType);
            return this;
        }

        public Builder roundNumber(int roundNumber) {
            entity.setRoundNumber(roundNumber);
            return this;
        }

        public Builder questionIndex(Integer questionIndex) {
            entity.setQuestionIndex(questionIndex);
            return this;
        }

        public Builder givenAnswer(String givenAnswer) {
            entity.setGivenAnswer(givenAnswer);
            return this;
        }

        public Builder expectedAnswer(String expectedAnswer) {
            entity.setExpectedAnswer(expectedAnswer);
            return this;
        }

        public Builder correct(boolean correct) {
            entity.setCorrect(correct);
            return this;
        }

        public Builder pointsAwarded(int pointsAwarded) {
            entity.setPointsAwarded(pointsAwarded);
            return this;
        }

        public Builder responseTimeMs(Long responseTimeMs) {
            entity.setResponseTimeMs(responseTimeMs);
            return this;
        }

        public Builder wasBuzzer(boolean wasBuzzer) {
            entity.setWasBuzzer(wasBuzzer);
            return this;
        }

        public Builder wasTimeout(boolean wasTimeout) {
            entity.setWasTimeout(wasTimeout);
            return this;
        }

        public Builder wasSmashed(boolean wasSmashed) {
            entity.setWasSmashed(wasSmashed);
            return this;
        }

        public AnswerHistoryEntity build() {
            return entity;
        }
    }
}
