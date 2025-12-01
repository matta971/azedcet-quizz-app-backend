package com.mindsoccer.match.entity;

import com.mindsoccer.protocol.enums.TeamSide;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une équipe dans un match.
 */
@Entity
@Table(name = "ms_team", indexes = {
        @Index(name = "idx_team_match", columnList = "match_id")
})
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private TeamSide side;

    @Column(length = 100)
    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("joinedAt ASC")
    private List<PlayerEntity> players = new ArrayList<>();

    @Column(name = "captain_id")
    private UUID captainId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TeamEntity() {
    }

    public TeamEntity(TeamSide side) {
        this.side = side;
    }

    public TeamEntity(TeamSide side, String name) {
        this.side = side;
        this.name = name;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MatchEntity getMatch() {
        return match;
    }

    public void setMatch(MatchEntity match) {
        this.match = match;
    }

    public TeamSide getSide() {
        return side;
    }

    public void setSide(TeamSide side) {
        this.side = side;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PlayerEntity> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerEntity> players) {
        this.players = players;
    }

    public void addPlayer(PlayerEntity player) {
        players.add(player);
        player.setTeam(this);
    }

    public void removePlayer(PlayerEntity player) {
        players.remove(player);
        player.setTeam(null);
    }

    public UUID getCaptainId() {
        return captainId;
    }

    public void setCaptainId(UUID captainId) {
        this.captainId = captainId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Helper methods

    public int getPlayerCount() {
        return players.size();
    }

    public boolean isFull(int maxSize) {
        return players.size() >= maxSize;
    }

    public boolean hasPlayer(UUID userId) {
        return players.stream().anyMatch(p -> p.getUserId().equals(userId));
    }

    public PlayerEntity getPlayer(UUID userId) {
        return players.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public PlayerEntity getCaptain() {
        if (captainId == null) return null;
        return getPlayer(captainId);
    }

    public List<PlayerEntity> getActivePlayers() {
        return players.stream()
                .filter(p -> !p.isSuspended())
                .toList();
    }
}
