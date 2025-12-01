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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
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
    public MatchEntity createMatch(UUID creatorId, String creatorHandle, boolean ranked, boolean duo, TeamSide preferredSide) {
        String code = generateUniqueCode();

        MatchEntity match = new MatchEntity(code);
        match.setRanked(ranked);
        match.setDuo(duo);

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

        log.info("Match created: {} by {} ({})", code, creatorHandle, creatorId);
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

        int maxSize = match.isDuo() ? GameConstants.TEAM_DUO_SIZE : GameConstants.TEAM_MAX_SIZE;
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
        team.removePlayer(player);

        if (team.getCaptainId() != null && team.getCaptainId().equals(userId)) {
            team.setCaptainId(team.getPlayers().isEmpty() ? null : team.getPlayers().get(0).getUserId());
        }

        playerRepository.delete(player);

        if (match.getTeamA().getPlayerCount() == 0 && match.getTeamB().getPlayerCount() == 0) {
            matchRepository.delete(match);
            log.info("Match {} deleted (empty)", match.getCode());
        } else {
            log.info("Player {} left match {}", userId, match.getCode());
        }
    }

    @Transactional
    public MatchEntity startMatch(UUID matchId, UUID refereeId) {
        MatchEntity match = getById(matchId);

        if (!match.isWaiting()) {
            throw MatchException.alreadyStarted();
        }

        int minPlayers = match.isDuo() ? 2 : GameConstants.TEAM_MIN_SIZE;
        if (match.getTeamA().getPlayerCount() < minPlayers || match.getTeamB().getPlayerCount() < minPlayers) {
            throw MatchException.teamsIncomplete();
        }

        match.setRefereeId(refereeId);
        match.start();

        log.info("Match {} started by referee {}", match.getCode(), refereeId);
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
}
