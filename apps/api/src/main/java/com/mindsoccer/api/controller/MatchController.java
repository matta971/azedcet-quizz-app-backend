package com.mindsoccer.api.controller;

import com.mindsoccer.api.security.CurrentUser;
import com.mindsoccer.api.security.UserPrincipal;
import com.mindsoccer.api.service.GameOrchestratorService;
import com.mindsoccer.match.entity.MatchEntity;
import com.mindsoccer.match.entity.PlayerEntity;
import com.mindsoccer.match.entity.TeamEntity;
import com.mindsoccer.match.service.MatchService;
import com.mindsoccer.protocol.dto.common.ApiResponse;
import com.mindsoccer.protocol.dto.common.PageResponse;
import com.mindsoccer.protocol.dto.request.CreateMatchRequest;
import com.mindsoccer.protocol.dto.request.JoinMatchRequest;
import com.mindsoccer.protocol.dto.response.MatchResponse;
import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.TeamSide;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "Gestion des matchs")
@SecurityRequirement(name = "bearerAuth")
@Transactional(readOnly = true)
public class MatchController {

    private final MatchService matchService;
    private final GameOrchestratorService gameOrchestratorService;

    public MatchController(MatchService matchService, GameOrchestratorService gameOrchestratorService) {
        this.matchService = matchService;
        this.gameOrchestratorService = gameOrchestratorService;
    }

    @GetMapping
    @Operation(summary = "Matchs en attente", description = "Récupérer la liste des matchs en attente de joueurs")
    public ResponseEntity<ApiResponse<PageResponse<MatchResponse>>> getWaitingMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MatchEntity> matches = matchService.getWaitingMatches(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<MatchResponse> responsePage = matches.map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Match par ID", description = "Récupérer un match par son identifiant")
    public ResponseEntity<ApiResponse<MatchResponse>> getMatchById(@PathVariable UUID id) {
        MatchEntity match = matchService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(toResponse(match)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Match par code", description = "Récupérer un match par son code")
    public ResponseEntity<ApiResponse<MatchResponse>> getMatchByCode(@PathVariable String code) {
        MatchEntity match = matchService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.success(toResponse(match)));
    }

    @GetMapping("/my")
    @Operation(summary = "Mes matchs", description = "Récupérer l'historique de mes matchs")
    public ResponseEntity<ApiResponse<PageResponse<MatchResponse>>> getMyMatches(
            @CurrentUser UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MatchEntity> matches = matchService.getPlayerHistory(principal.getId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<MatchResponse> responsePage = matches.map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responsePage)));
    }

    @PostMapping
    @Operation(summary = "Créer un match", description = "Créer un nouveau match avec le nombre de joueurs par équipe spécifié")
    @Transactional
    public ResponseEntity<ApiResponse<MatchResponse>> createMatch(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody CreateMatchRequest request
    ) {
        MatchEntity match = matchService.createMatch(
                principal.getId(),
                principal.getHandle(),
                request.ranked(),
                request.maxPlayersPerTeam(),
                null  // preferredSide not in request
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toResponse(match)));
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Rejoindre un match", description = "Rejoindre un match existant")
    @Transactional
    public ResponseEntity<ApiResponse<MatchResponse>> joinMatch(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody(required = false) JoinMatchRequest request
    ) {
        TeamSide preferredSide = request != null ? request.team() : null;
        MatchEntity match = matchService.joinMatch(id, principal.getId(), principal.getHandle(), preferredSide);

        // If match auto-started (became PLAYING), initialize the game engine
        if (match.getStatus() == MatchStatus.PLAYING) {
            gameOrchestratorService.initializeAndStartGame(match);
        }

        return ResponseEntity.ok(ApiResponse.success(toResponse(match)));
    }

    @PostMapping("/code/{code}/join")
    @Operation(summary = "Rejoindre par code", description = "Rejoindre un match via son code")
    @Transactional
    public ResponseEntity<ApiResponse<MatchResponse>> joinMatchByCode(
            @PathVariable String code,
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody(required = false) JoinMatchRequest request
    ) {
        TeamSide preferredSide = request != null ? request.team() : null;
        MatchEntity match = matchService.joinByCode(code, principal.getId(), principal.getHandle(), preferredSide);

        // If match auto-started (became PLAYING), initialize the game engine
        if (match.getStatus() == MatchStatus.PLAYING) {
            gameOrchestratorService.initializeAndStartGame(match);
        }

        return ResponseEntity.ok(ApiResponse.success(toResponse(match)));
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "Quitter un match", description = "Quitter un match en attente")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> leaveMatch(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal
    ) {
        matchService.leaveMatch(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Démarrer un match", description = "Démarrer un match (capitaine)")
    @Transactional
    public ResponseEntity<ApiResponse<MatchResponse>> startMatch(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal
    ) {
        MatchEntity match = matchService.startMatch(id, principal.getId());

        // Initialize the game engine after match starts
        gameOrchestratorService.initializeAndStartGame(match);

        return ResponseEntity.ok(ApiResponse.success(toResponse(match)));
    }

    private MatchResponse toResponse(MatchEntity match) {
        TeamEntity teamA = match.getTeamA();
        TeamEntity teamB = match.getTeamB();
        int maxPlayers = match.getMaxPlayersPerTeam();

        return new MatchResponse(
                match.getId(),
                match.getCode(),
                match.getStatus(),
                match.isRanked(),
                match.isDuo(),
                maxPlayers,
                toTeamResponse(teamA, maxPlayers),
                toTeamResponse(teamB, maxPlayers),
                match.getScoreTeamA(),
                match.getScoreTeamB(),
                match.getCurrentRound(),
                match.getCurrentRoundType(),
                match.canStart(),
                match.getStartedAt(),
                match.getFinishedAt(),
                match.getCreatedAt()
        );
    }

    private MatchResponse.TeamResponse toTeamResponse(TeamEntity team, int maxPlayersPerTeam) {
        if (team == null) return null;

        List<MatchResponse.PlayerResponse> players = team.getPlayers().stream()
                .map(this::toPlayerResponse)
                .toList();

        return new MatchResponse.TeamResponse(
                team.getId(),
                team.getSide(),
                team.getName(),
                team.getCaptainId(),
                players,
                team.getPlayerCount(),
                team.getPlayerCount() >= maxPlayersPerTeam
        );
    }

    private MatchResponse.PlayerResponse toPlayerResponse(PlayerEntity player) {
        return new MatchResponse.PlayerResponse(
                player.getId(),
                player.getUserId(),
                player.getHandle(),
                player.isSuspended()
        );
    }
}
