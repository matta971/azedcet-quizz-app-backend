package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.MatchStatus;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.model.ScoreResult;
import com.mindsoccer.scoring.service.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Moteur de jeu principal.
 * Orchestre les rubriques et les plugins pour un match.
 */
@Service
public class GameEngine {

    private static final Logger log = LoggerFactory.getLogger(GameEngine.class);

    private final PluginRegistry pluginRegistry;
    private final ScoringService scoringService;

    // État des matchs en cours (en mémoire pour MVP, Redis pour production)
    private final Map<UUID, GameSession> activeSessions = new ConcurrentHashMap<>();

    public GameEngine(PluginRegistry pluginRegistry, ScoringService scoringService) {
        this.pluginRegistry = pluginRegistry;
        this.scoringService = scoringService;
    }

    /**
     * Crée une nouvelle session de jeu pour un match.
     */
    public GameSession createSession(UUID matchId, List<UUID> teamAPlayers, List<UUID> teamBPlayers) {
        GameSession session = new GameSession(matchId, teamAPlayers, teamBPlayers);
        activeSessions.put(matchId, session);
        log.info("Game session created for match: {}", matchId);
        return session;
    }

    /**
     * Récupère une session existante.
     */
    public Optional<GameSession> getSession(UUID matchId) {
        return Optional.ofNullable(activeSessions.get(matchId));
    }

    /**
     * Démarre une rubrique.
     */
    public RoundState startRound(UUID matchId, RoundType roundType) {
        GameSession session = activeSessions.get(matchId);
        if (session == null) {
            throw new IllegalStateException("No session for match: " + matchId);
        }

        RulePlugin plugin = pluginRegistry.getPluginOrThrow(roundType);
        MatchContext ctx = session.buildContext(roundType);

        RoundState state = plugin.init(ctx);
        session.setCurrentRound(roundType, state);

        log.info("Round {} started for match {}", roundType, matchId);
        return state;
    }

    /**
     * Traite un tick du moteur de jeu.
     * Appelé périodiquement (100ms par défaut).
     */
    public RoundState tick(UUID matchId, Duration dt) {
        GameSession session = activeSessions.get(matchId);
        if (session == null || session.getCurrentRoundType() == null) {
            return null;
        }

        RulePlugin plugin = pluginRegistry.getPluginOrThrow(session.getCurrentRoundType());
        MatchContext ctx = session.buildContext(session.getCurrentRoundType());

        RoundState newState = plugin.onTick(ctx, dt);
        session.updateRoundState(newState);

        return newState;
    }

    /**
     * Traite une réponse d'un joueur.
     */
    public AnswerResult processAnswer(UUID matchId, AnswerPayload payload) {
        GameSession session = activeSessions.get(matchId);
        if (session == null) {
            throw new IllegalStateException("No session for match: " + matchId);
        }

        RoundType roundType = session.getCurrentRoundType();
        if (roundType == null) {
            throw new IllegalStateException("No active round for match: " + matchId);
        }

        RulePlugin plugin = pluginRegistry.getPluginOrThrow(roundType);
        MatchContext ctx = session.buildContext(roundType);

        // Valider et traiter la réponse
        RoundState newState = plugin.onAnswer(ctx, payload);
        session.updateRoundState(newState);

        // Calculer le scoring si la réponse est correcte
        boolean correct = Boolean.TRUE.equals(newState.extra().get("correct"));
        ScoreResult scoreResult = ScoreResult.zero();

        if (correct) {
            scoreResult = scoringService.calculateCorrectAnswer(roundType, payload.team(), payload.playerId());
            session.addScore(payload.team(), scoreResult.points());
        }

        log.debug("Answer processed for match {}: correct={}, points={}", matchId, correct, scoreResult.points());

        return new AnswerResult(
                correct,
                scoreResult,
                newState,
                payload.playerId(),
                payload.team()
        );
    }

    /**
     * Termine une rubrique.
     */
    public void endRound(UUID matchId) {
        GameSession session = activeSessions.get(matchId);
        if (session == null) {
            return;
        }

        RoundType roundType = session.getCurrentRoundType();
        if (roundType != null) {
            RulePlugin plugin = pluginRegistry.getPluginOrThrow(roundType);
            MatchContext ctx = session.buildContext(roundType);
            plugin.applyScoring(ctx);
        }

        session.clearCurrentRound();
        log.info("Round ended for match {}", matchId);
    }

    /**
     * Termine la session de jeu.
     */
    public void endSession(UUID matchId) {
        GameSession session = activeSessions.remove(matchId);
        if (session != null) {
            log.info("Game session ended for match: {}. Final scores: A={}, B={}",
                    matchId, session.getScore(TeamSide.A), session.getScore(TeamSide.B));
        }
    }

    /**
     * Retourne les scores actuels.
     */
    public Map<TeamSide, Integer> getScores(UUID matchId) {
        GameSession session = activeSessions.get(matchId);
        if (session == null) {
            return Map.of();
        }
        return Map.of(
                TeamSide.A, session.getScore(TeamSide.A),
                TeamSide.B, session.getScore(TeamSide.B)
        );
    }

    /**
     * Session de jeu en mémoire.
     */
    public static class GameSession {
        private final UUID matchId;
        private final List<UUID> teamAPlayers;
        private final List<UUID> teamBPlayers;
        private final Map<TeamSide, Integer> scores = new EnumMap<>(TeamSide.class);
        private final Map<UUID, Integer> penaltyCounts = new HashMap<>();
        private final Set<UUID> suspendedPlayers = new HashSet<>();
        private RoundType currentRoundType;
        private RoundState currentRoundState;
        private Instant roundStartedAt;
        private int roundIndex = 0;

        public GameSession(UUID matchId, List<UUID> teamAPlayers, List<UUID> teamBPlayers) {
            this.matchId = matchId;
            this.teamAPlayers = new ArrayList<>(teamAPlayers);
            this.teamBPlayers = new ArrayList<>(teamBPlayers);
            this.scores.put(TeamSide.A, 0);
            this.scores.put(TeamSide.B, 0);
        }

        public MatchContext buildContext(RoundType roundType) {
            return new MatchContext(
                    matchId,
                    MatchStatus.PLAYING,
                    roundType,
                    roundIndex,
                    roundStartedAt != null ? roundStartedAt : Instant.now(),
                    Map.copyOf(scores),
                    Map.of(TeamSide.A, List.copyOf(teamAPlayers), TeamSide.B, List.copyOf(teamBPlayers)),
                    Map.copyOf(penaltyCounts),
                    suspendedPlayers.stream().collect(java.util.stream.Collectors.toMap(id -> id, id -> true)),
                    getLeadingTeam(),
                    currentRoundState
            );
        }

        public void setCurrentRound(RoundType type, RoundState state) {
            this.currentRoundType = type;
            this.currentRoundState = state;
            this.roundStartedAt = Instant.now();
            this.roundIndex++;
        }

        public void updateRoundState(RoundState state) {
            this.currentRoundState = state;
        }

        public void clearCurrentRound() {
            this.currentRoundType = null;
            this.currentRoundState = null;
            this.roundStartedAt = null;
        }

        public void addScore(TeamSide side, int points) {
            scores.merge(side, points, Integer::sum);
        }

        public int getScore(TeamSide side) {
            return scores.getOrDefault(side, 0);
        }

        public void addPenalty(UUID playerId) {
            penaltyCounts.merge(playerId, 1, Integer::sum);
        }

        public void suspendPlayer(UUID playerId) {
            suspendedPlayers.add(playerId);
        }

        public void unsuspendPlayer(UUID playerId) {
            suspendedPlayers.remove(playerId);
        }

        public RoundType getCurrentRoundType() {
            return currentRoundType;
        }

        public RoundState getCurrentRoundState() {
            return currentRoundState;
        }

        private TeamSide getLeadingTeam() {
            int scoreA = scores.getOrDefault(TeamSide.A, 0);
            int scoreB = scores.getOrDefault(TeamSide.B, 0);
            if (scoreA > scoreB) return TeamSide.A;
            if (scoreB > scoreA) return TeamSide.B;
            return null;
        }
    }

    /**
     * Résultat du traitement d'une réponse.
     */
    public record AnswerResult(
            boolean correct,
            ScoreResult scoreResult,
            RoundState roundState,
            UUID playerId,
            TeamSide team
    ) {}
}
