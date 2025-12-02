package com.mindsoccer.engine.plugin;

import com.mindsoccer.engine.AnswerPayload;
import com.mindsoccer.engine.MatchContext;
import com.mindsoccer.engine.RoundState;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.service.AnswerValidationService;
import com.mindsoccer.scoring.service.ScoringService;
import com.mindsoccer.shared.util.GameConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Plugin PANIER.
 *
 * Règles:
 * - 4 thèmes sont proposés aux équipes
 * - Les équipes choisissent leurs thèmes à tour de rôle
 * - Chaque équipe répond à 4 questions de ses thèmes choisis
 * - 10 points par bonne réponse
 */
@Component
@Profile("!test")
public class PanierPlugin extends AbstractRulePlugin {

    private static final String THEMES_KEY = "themes";
    private static final String TEAM_A_THEMES_KEY = "teamAThemes";
    private static final String TEAM_B_THEMES_KEY = "teamBThemes";
    private static final String SELECTION_PHASE_KEY = "selectionPhase";
    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String QUESTIONS_ANSWERED_KEY = "questionsAnswered";

    public PanierPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.PANIER;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.PANIER);
        return withExtras(state, Map.of(
                SELECTION_PHASE_KEY, true,
                CURRENT_TEAM_KEY, TeamSide.A,
                QUESTIONS_ANSWERED_KEY, 0,
                TEAM_A_THEMES_KEY, List.of(),
                TEAM_B_THEMES_KEY, List.of()
        ));
    }

    @Override
    public RoundState onTick(MatchContext ctx, Duration dt) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        // Décrémenter le temps si en phase de question
        if (state.phase() == RoundState.Phase.ANSWER_WINDOW) {
            RoundState newState = decrementTime(state, dt);
            if (isTimeUp(newState)) {
                // Timeout: passer à la question suivante
                return advanceQuestion(newState);
            }
            return newState;
        }

        return state;
    }

    @Override
    public RoundState onAnswer(MatchContext ctx, AnswerPayload payload) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        boolean selectionPhase = getExtra(state, SELECTION_PHASE_KEY, true);

        if (selectionPhase) {
            // Gérer la sélection de thème
            return handleThemeSelection(state, payload);
        }

        // Phase de questions
        TeamSide currentTeam = getExtra(state, CURRENT_TEAM_KEY, TeamSide.A);
        if (payload.team() != currentTeam) {
            return state; // Ignorer
        }

        boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

        // Stocker le résultat
        RoundState newState = withExtra(state, "lastCorrect", correct);

        return advanceQuestion(newState);
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring géré par le GameEngine
    }

    @SuppressWarnings("unchecked")
    private RoundState handleThemeSelection(RoundState state, AnswerPayload payload) {
        TeamSide currentTeam = getExtra(state, CURRENT_TEAM_KEY, TeamSide.A);

        if (payload.team() != currentTeam) {
            return state;
        }

        // La réponse contient l'ID du thème choisi
        UUID themeId = UUID.fromString(payload.answer());

        List<UUID> teamAThemes = getExtra(state, TEAM_A_THEMES_KEY, List.of());
        List<UUID> teamBThemes = getExtra(state, TEAM_B_THEMES_KEY, List.of());

        if (currentTeam == TeamSide.A) {
            teamAThemes = new java.util.ArrayList<>(teamAThemes);
            teamAThemes.add(themeId);
        } else {
            teamBThemes = new java.util.ArrayList<>(teamBThemes);
            teamBThemes.add(themeId);
        }

        // Vérifier si la sélection est terminée
        int totalSelected = teamAThemes.size() + teamBThemes.size();
        if (totalSelected >= GameConstants.PANIER_THEME_COUNT) {
            // Passer à la phase de questions
            return withExtras(state, Map.of(
                    SELECTION_PHASE_KEY, false,
                    CURRENT_TEAM_KEY, TeamSide.A,
                    TEAM_A_THEMES_KEY, teamAThemes,
                    TEAM_B_THEMES_KEY, teamBThemes
            )).withPhase(RoundState.Phase.QUESTION_SHOWN);
        }

        // Alterner l'équipe pour la prochaine sélection
        TeamSide nextTeam = getOppositeTeam(currentTeam);
        return withExtras(state, Map.of(
                CURRENT_TEAM_KEY, nextTeam,
                TEAM_A_THEMES_KEY, teamAThemes,
                TEAM_B_THEMES_KEY, teamBThemes
        ));
    }

    private RoundState advanceQuestion(RoundState state) {
        int answered = getExtra(state, QUESTIONS_ANSWERED_KEY, 0) + 1;
        TeamSide currentTeam = getExtra(state, CURRENT_TEAM_KEY, TeamSide.A);

        // Chaque équipe répond à PANIER_QUESTION_COUNT questions
        int questionsPerTeam = GameConstants.PANIER_QUESTION_COUNT;
        int totalQuestions = questionsPerTeam * 2;

        if (answered >= totalQuestions) {
            return withExtra(state.completed(), QUESTIONS_ANSWERED_KEY, answered);
        }

        // Alterner les équipes après chaque question
        TeamSide nextTeam = getOppositeTeam(currentTeam);

        return withExtras(state, Map.of(
                QUESTIONS_ANSWERED_KEY, answered,
                CURRENT_TEAM_KEY, nextTeam
        )).withPhase(RoundState.Phase.TRANSITION);
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }
}
