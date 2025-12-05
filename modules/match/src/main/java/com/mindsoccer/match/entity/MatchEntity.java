package com.mindsoccer.match.entity;

import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.TeamSide;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant un match MINDSOCCER.
 */
@Entity
@Table(name = "ms_match", indexes = {
        @Index(name = "idx_match_status", columnList = "status"),
        @Index(name = "idx_match_created", columnList = "created_at")
})
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.WAITING;

    @Column(name = "is_ranked", nullable = false)
    private boolean ranked = true;

    @Column(name = "is_duo", nullable = false)
    private boolean duo = false;

    @Column(name = "max_players_per_team", nullable = false)
    private int maxPlayersPerTeam = 5;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("side ASC")
    private List<TeamEntity> teams = new ArrayList<>();

    @Column(name = "referee_id")
    private UUID refereeId;

    @Column(name = "current_round")
    private Integer currentRound;

    @Column(name = "current_round_type", length = 30)
    private String currentRoundType;

    @Column(name = "score_team_a", nullable = false)
    private int scoreTeamA = 0;

    @Column(name = "score_team_b", nullable = false)
    private int scoreTeamB = 0;

    @Column(name = "penalties_team_a", nullable = false)
    private int penaltiesTeamA = 0;

    @Column(name = "penalties_team_b", nullable = false)
    private int penaltiesTeamB = 0;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "winner_team_id")
    private UUID winnerTeamId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public MatchEntity() {
    }

    public MatchEntity(String code) {
        this.code = code;
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

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public boolean isRanked() {
        return ranked;
    }

    public void setRanked(boolean ranked) {
        this.ranked = ranked;
    }

    public boolean isDuo() {
        return duo;
    }

    public void setDuo(boolean duo) {
        this.duo = duo;
    }

    public int getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }

    public void setMaxPlayersPerTeam(int maxPlayersPerTeam) {
        this.maxPlayersPerTeam = maxPlayersPerTeam;
    }

    public List<TeamEntity> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamEntity> teams) {
        this.teams = teams;
    }

    public void addTeam(TeamEntity team) {
        teams.add(team);
        team.setMatch(this);
    }

    public UUID getRefereeId() {
        return refereeId;
    }

    public void setRefereeId(UUID refereeId) {
        this.refereeId = refereeId;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public String getCurrentRoundType() {
        return currentRoundType;
    }

    public void setCurrentRoundType(String currentRoundType) {
        this.currentRoundType = currentRoundType;
    }

    public int getScoreTeamA() {
        return scoreTeamA;
    }

    public void setScoreTeamA(int scoreTeamA) {
        this.scoreTeamA = scoreTeamA;
    }

    public void addScoreTeamA(int points) {
        this.scoreTeamA += points;
    }

    public int getScoreTeamB() {
        return scoreTeamB;
    }

    public void setScoreTeamB(int scoreTeamB) {
        this.scoreTeamB = scoreTeamB;
    }

    public void addScoreTeamB(int points) {
        this.scoreTeamB += points;
    }

    public int getPenaltiesTeamA() {
        return penaltiesTeamA;
    }

    public void setPenaltiesTeamA(int penaltiesTeamA) {
        this.penaltiesTeamA = penaltiesTeamA;
    }

    public void addPenaltyTeamA() {
        this.penaltiesTeamA++;
    }

    public int getPenaltiesTeamB() {
        return penaltiesTeamB;
    }

    public void setPenaltiesTeamB(int penaltiesTeamB) {
        this.penaltiesTeamB = penaltiesTeamB;
    }

    public void addPenaltyTeamB() {
        this.penaltiesTeamB++;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public UUID getWinnerTeamId() {
        return winnerTeamId;
    }

    public void setWinnerTeamId(UUID winnerTeamId) {
        this.winnerTeamId = winnerTeamId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Helper methods

    public TeamEntity getTeamA() {
        return teams.stream()
                .filter(t -> t.getSide() == TeamSide.A)
                .findFirst()
                .orElse(null);
    }

    public TeamEntity getTeamB() {
        return teams.stream()
                .filter(t -> t.getSide() == TeamSide.B)
                .findFirst()
                .orElse(null);
    }

    public boolean isWaiting() {
        return status == MatchStatus.WAITING;
    }

    public boolean isPlaying() {
        return status == MatchStatus.PLAYING;
    }

    public boolean isFinished() {
        return status == MatchStatus.FINISHED;
    }

    public void start() {
        this.status = MatchStatus.PLAYING;
        this.startedAt = Instant.now();
        this.currentRound = 1;
    }

    public void finish() {
        this.status = MatchStatus.FINISHED;
        this.finishedAt = Instant.now();
    }

    /**
     * Vérifie si une équipe est complète (a atteint maxPlayersPerTeam).
     */
    public boolean isTeamFull(TeamSide side) {
        TeamEntity team = side == TeamSide.A ? getTeamA() : getTeamB();
        return team != null && team.getPlayerCount() >= maxPlayersPerTeam;
    }

    /**
     * Vérifie si les deux équipes sont complètes et prêtes à démarrer.
     */
    public boolean areBothTeamsFull() {
        return isTeamFull(TeamSide.A) && isTeamFull(TeamSide.B);
    }

    /**
     * Vérifie si le match peut être démarré (les deux équipes sont complètes).
     */
    public boolean canStart() {
        return isWaiting() && areBothTeamsFull();
    }
}
