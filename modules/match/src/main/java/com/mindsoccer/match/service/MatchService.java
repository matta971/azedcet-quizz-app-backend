package com.mindsoccer.match.service;

import com.mindsoccer.match.entity.MatchEntity;
import com.mindsoccer.match.entity.PlayerEntity;
import com.mindsoccer.match.entity.TeamEntity;
import com.mindsoccer.match.repository.MatchRepository;
import com.mindsoccer.match.repository.PlayerRepository;
import com.mindsoccer.match.repository.TeamRepository;
import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.shared.exception.MatchException;
import com.mindsoccer.shared.exception.NotFoundException;
import com.mindsoccer.shared.util.GameConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service principal de gestion des matchs.
 */
@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public MatchService(MatchRepository matchRepository, TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public MatchEntity getById(UUID id) {
        return matchRepository.findById(id)
                .orElseThrow(NotFoundException::match);
    }

    @Transactional(readOnly = true)
    public MatchEntity getByCode(String code) {
        return matchRepository.findByCode(code.toUpperCase())
                .orElseThrow(NotFoundException::match);
    }

    @Transactional(readOnly = true)
    public Page<MatchEntity> getWaitingMatches(Pageable pageable) {
        return matchRepository.findByStatus(MatchStatus.WAITING, pageable);
    }

    @Transactional(readOnly = true)
    public List<MatchEntity> getPlayerMatches(UUID userId, MatchStatus status) {
        return matchRepository.findByPlayerAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public Page<MatchEntity> getPlayerHistory(UUID userId, Pageable pageable) {
        return matchRepository.findByPlayer(userId, pageable);
    }

    @Transactional
    public MatchEntity createMatch(UUID creatorId, String creatorHandle, boolean ranked, int maxPlayersPerTeam, TeamSide preferredSide) {
        // Valider maxPlayersPerTeam
        if (maxPlayersPerTeam < 1) maxPlayersPerTeam = 1;
        if (maxPlayersPerTeam > GameConstants.TEAM_MAX_SIZE) maxPlayersPerTeam = GameConstants.TEAM_MAX_SIZE;

        String code = generateUniqueCode();

        MatchEntity match = new MatchEntity(code);
        match.setRanked(ranked);
        match.setMaxPlayersPerTeam(maxPlayersPerTeam);
        match.setDuo(maxPlayersPerTeam == 1); // duo si 1v1

        TeamEntity teamA = new TeamEntity(TeamSide.A);
        TeamEntity teamB = new TeamEntity(TeamSide.B);

        match.addTeam(teamA);
        match.addTeam(teamB);

        match = matchRepository.save(match);

        TeamEntity creatorTeam = preferredSide == TeamSide.B ? teamB : teamA;
        PlayerEntity creator = new PlayerEntity(creatorId, creatorHandle);
        creatorTeam.addPlayer(creator);
        creatorTeam.setCaptainId(creatorId);

        matchRepository.save(match);

        log.info("Match created: {} by {} ({}) - maxPlayersPerTeam: {}", code, creatorHandle, creatorId, maxPlayersPerTeam);
        return match;
    }

    @Transactional
    public MatchEntity joinMatch(UUID matchId, UUID userId, String userHandle, TeamSide preferredSide) {
        MatchEntity match = getById(matchId);

        if (!match.isWaiting()) {
            throw MatchException.alreadyStarted();
        }

        if (isPlayerInMatch(match, userId)) {
            throw MatchException.alreadyParticipant();
        }

        int maxSize = match.getMaxPlayersPerTeam();
        TeamEntity targetTeam = null;

        if (preferredSide != null) {
            TeamEntity preferred = preferredSide == TeamSide.A ? match.getTeamA() : match.getTeamB();
            if (!preferred.isFull(maxSize)) {
                targetTeam = preferred;
            }
        }

        if (targetTeam == null) {
            List<TeamEntity> available = teamRepository.findAvailableTeams(matchId, maxSize);
            if (available.isEmpty()) {
                throw MatchException.full();
            }
            targetTeam = available.stream()
                    .min((t1, t2) -> Integer.compare(t1.getPlayerCount(), t2.getPlayerCount()))
                    .orElse(available.get(0));
        }

        PlayerEntity player = new PlayerEntity(userId, userHandle);
        targetTeam.addPlayer(player);

        if (targetTeam.getCaptainId() == null) {
            targetTeam.setCaptainId(userId);
        }

        playerRepository.save(player);

        log.info("Player {} joined match {} in team {}", userHandle, match.getCode(), targetTeam.getSide());

        // Broadcast player joined event
        broadcastPlayerJoined(match.getId(), userId, userHandle, targetTeam.getSide());

        // Broadcast lobby updated with team status
        broadcastLobbyUpdated(match);

        return match;
    }

    @Transactional
    public MatchEntity joinByCode(String code, UUID userId, String userHandle, TeamSide preferredSide) {
        MatchEntity match = getByCode(code);
        return joinMatch(match.getId(), userId, userHandle, preferredSide);
    }

    @Transactional
    public void leaveMatch(UUID matchId, UUID userId) {
        MatchEntity match = getById(matchId);

        if (match.isPlaying()) {
            throw MatchException.alreadyStarted();
        }

        PlayerEntity player = playerRepository.findByMatchAndUser(matchId, userId)
                .orElseThrow(NotFoundException::player);

        TeamEntity team = player.getTeam();
        TeamSide teamSide = team.getSide();
        team.removePlayer(player);

        if (team.getCaptainId() != null && team.getCaptainId().equals(userId)) {
            team.setCaptainId(team.getPlayers().isEmpty() ? null : team.getPlayers().get(0).getUserId());
        }

        playerRepository.delete(player);

        if (match.getTeamA().getPlayerCount() == 0 && match.getTeamB().getPlayerCount() == 0) {
            matchRepository.delete(match);
            log.info("Match {} deleted (empty)", match.getCode());
        } else {
            // Broadcast player left event
            broadcastPlayerLeft(matchId, userId, teamSide);
            // Broadcast lobby updated with new team status
            broadcastLobbyUpdated(match);
            log.info("Player {} left match {}", userId, match.getCode());
        }
    }

    @Transactional
    public MatchEntity startMatch(UUID matchId, UUID userId) {
        MatchEntity match = getById(matchId);

        if (!match.isWaiting()) {
            throw MatchException.alreadyStarted();
        }

        // Les deux équipes doivent être complètes (atteindre maxPlayersPerTeam)
        if (!match.areBothTeamsFull()) {
            throw MatchException.teamsIncomplete();
        }

        // Vérifier que l'utilisateur est un capitaine (équipe A ou B)
        boolean isCaptainA = match.getTeamA() != null && userId.equals(match.getTeamA().getCaptainId());
        boolean isCaptainB = match.getTeamB() != null && userId.equals(match.getTeamB().getCaptainId());
        if (!isCaptainA && !isCaptainB) {
            throw MatchException.notAuthorized("Seul un capitaine peut lancer le match");
        }

        match.start();

        broadcastMatchStarted(match.getId());
        log.info("Match {} started by captain {} - Teams full ({} players each)",
                match.getCode(), userId, match.getMaxPlayersPerTeam());
        return matchRepository.save(match);
    }

    @Transactional
    public MatchEntity finishMatch(UUID matchId) {
        MatchEntity match = getById(matchId);

        if (!match.isPlaying()) {
            throw MatchException.notStarted();
        }

        match.finish();

        UUID winnerId = null;
        if (match.getScoreTeamA() > match.getScoreTeamB()) {
            winnerId = match.getTeamA().getId();
        } else if (match.getScoreTeamB() > match.getScoreTeamA()) {
            winnerId = match.getTeamB().getId();
        }
        match.setWinnerTeamId(winnerId);

        log.info("Match {} finished. Score: {} - {}", match.getCode(), match.getScoreTeamA(), match.getScoreTeamB());
        return matchRepository.save(match);
    }

    @Transactional
    public void updateScore(UUID matchId, TeamSide side, int points) {
        MatchEntity match = getById(matchId);

        if (side == TeamSide.A) {
            match.addScoreTeamA(points);
        } else {
            match.addScoreTeamB(points);
        }

        matchRepository.save(match);
    }

    @Transactional
    public void updateRound(UUID matchId, int roundNumber, String roundType) {
        MatchEntity match = getById(matchId);
        match.setCurrentRound(roundNumber);
        match.setCurrentRoundType(roundType);
        matchRepository.save(match);
    }

    @Transactional(readOnly = true)
    public boolean isPlayerInMatch(UUID matchId, UUID userId) {
        return playerRepository.findByMatchAndUser(matchId, userId).isPresent();
    }

    @Transactional(readOnly = true)
    public PlayerEntity getPlayer(UUID matchId, UUID userId) {
        return playerRepository.findByMatchAndUser(matchId, userId)
                .orElseThrow(NotFoundException::player);
    }

    @Transactional(readOnly = true)
    public TeamEntity getPlayerTeam(UUID matchId, UUID userId) {
        return teamRepository.findByMatchAndPlayer(matchId, userId)
                .orElseThrow(NotFoundException::team);
    }

    private boolean isPlayerInMatch(MatchEntity match, UUID userId) {
        return match.getTeams().stream()
                .anyMatch(t -> t.hasPlayer(userId));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (matchRepository.existsByCode(code));
        return code;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    // === WebSocket broadcast methods ===

    private void broadcastPlayerJoined(UUID matchId, UUID playerId, String handle, TeamSide team) {
        if (messagingTemplate == null) {
            log.debug("WebSocket not available, skipping broadcast");
            return;
        }
        String destination = "/topic/match/" + matchId;
        Map<String, Object> payload = Map.of(
                "type", "PLAYER_JOINED",
                "payload", Map.of(
                        "playerId", playerId.toString(),
                        "handle", handle,
                        "team", team.name()
                ),
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Broadcast PLAYER_JOINED to {}", destination);
    }

    private void broadcastMatchStarted(UUID matchId) {
        if (messagingTemplate == null) {
            log.debug("WebSocket not available, skipping broadcast");
            return;
        }
        String destination = "/topic/match/" + matchId;
        Map<String, Object> payload = Map.of(
                "type", "MATCH_STARTED",
                "payload", Map.of(
                        "matchId", matchId.toString()
                ),
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Broadcast MATCH_STARTED to {}", destination);
    }

    private void broadcastPlayerLeft(UUID matchId, UUID playerId, TeamSide team) {
        if (messagingTemplate == null) {
            log.debug("WebSocket not available, skipping broadcast");
            return;
        }
        String destination = "/topic/match/" + matchId;
        Map<String, Object> payload = Map.of(
                "type", "PLAYER_LEFT",
                "payload", Map.of(
                        "playerId", playerId.toString(),
                        "team", team.name()
                ),
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Broadcast PLAYER_LEFT to {}", destination);
    }

    private void broadcastLobbyUpdated(MatchEntity match) {
        if (messagingTemplate == null) {
            log.debug("WebSocket not available, skipping broadcast");
            return;
        }
        String destination = "/topic/match/" + match.getId();

        TeamEntity teamA = match.getTeamA();
        TeamEntity teamB = match.getTeamB();
        int maxPlayers = match.getMaxPlayersPerTeam();

        // Build team info maps with null-safe handling
        Map<String, Object> teamAInfo = new java.util.HashMap<>();
        teamAInfo.put("playerCount", teamA.getPlayerCount());
        teamAInfo.put("isFull", teamA.getPlayerCount() >= maxPlayers);
        teamAInfo.put("captainId", teamA.getCaptainId() != null ? teamA.getCaptainId().toString() : null);

        Map<String, Object> teamBInfo = new java.util.HashMap<>();
        teamBInfo.put("playerCount", teamB.getPlayerCount());
        teamBInfo.put("isFull", teamB.getPlayerCount() >= maxPlayers);
        teamBInfo.put("captainId", teamB.getCaptainId() != null ? teamB.getCaptainId().toString() : null);

        Map<String, Object> payloadContent = new java.util.HashMap<>();
        payloadContent.put("matchId", match.getId().toString());
        payloadContent.put("maxPlayersPerTeam", maxPlayers);
        payloadContent.put("teamA", teamAInfo);
        payloadContent.put("teamB", teamBInfo);
        payloadContent.put("canStart", match.canStart());

        Map<String, Object> payload = Map.of(
                "type", "LOBBY_UPDATED",
                "payload", payloadContent,
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("Broadcast LOBBY_UPDATED to {} - canStart: {}", destination, match.canStart());
    }
}
