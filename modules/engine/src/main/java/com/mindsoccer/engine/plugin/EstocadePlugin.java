package com.mindsoccer.engine.plugin;

import com.mindsoccer.engine.AnswerPayload;
import com.mindsoccer.engine.MatchContext;
import com.mindsoccer.engine.RoundState;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.service.AnswerValidationService;
import com.mindsoccer.scoring.service.ScoringService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * Plugin ESTOCADE - Attaque directe avec défense possible.
 *
 * Règles:
 * - Une équipe attaque, l'autre défend
 * - L'attaquant choisit une question parmi 3 thèmes
 * - Si l'attaquant répond correctement: +15 points
 * - Si l'attaquant échoue, le défenseur peut contre-attaquer: +10 points
 * - 4 manches par round, alternance des rôles
 */
@Component
public class EstocadePlugin extends AbstractRulePlugin {

    private static final int TOTAL_MANCHES = 4;
    private static final int ATTACK_POINTS = 15;
    private static final int COUNTER_POINTS = 10;
    private static final long ANSWER_TIME_MS = 12_000L;
    private static final long THEME_SELECTION_TIME_MS = 10_000L;

    private static final String PHASE_KEY = "phase"; // THEME_SELECTION, ATTACK, COUNTER
    private static final String ATTACKING_TEAM_KEY = "attackingTeam";
    private static final String SELECTED_THEME_KEY = "selectedTheme";

    public EstocadePlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.ESTOCADE;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.ESTOCADE);

        TeamSide attackingTeam = ctx.leadingTeam() == null ? TeamSide.A :
                (ctx.leadingTeam() == TeamSide.A ? TeamSide.B : TeamSide.A);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANNOUNCE), THEME_SELECTION_TIME_MS), Map.of(
                PHASE_KEY, "THEME_SELECTION",
                ATTACKING_TEAM_KEY, attackingTeam.name(),
                SELECTED_THEME_KEY, ""
        ));
    }

    @Override
    public RoundState onTick(MatchContext ctx, Duration dt) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        RoundState newState = decrementTime(state, dt);

        if (isTimeUp(newState)) {
            return handleTimeout(newState);
        }

        return newState;
    }

    @Override
    public RoundState onAnswer(MatchContext ctx, AnswerPayload payload) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        String phase = getExtra(state, PHASE_KEY, "ATTACK");
        TeamSide attackingTeam = TeamSide.valueOf(getExtra(state, ATTACKING_TEAM_KEY, "A"));

        // Sélection de thème (utilise answer comme code du thème)
        if ("THEME_SELECTION".equals(phase)) {
            if (payload.team() != attackingTeam) {
                return state;
            }

            return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "ATTACK",
                    SELECTED_THEME_KEY, payload.answer()
            ));
        }

        // Phase d'attaque
        if ("ATTACK".equals(phase)) {
            if (payload.team() != attackingTeam) {
                return state;
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            if (correct) {
                return advanceToNextManche(state);
            } else {
                // Contre-attaque possible
                TeamSide defendingTeam = getOppositeTeam(attackingTeam);
                return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                        PHASE_KEY, "COUNTER"
                ));
            }
        }

        // Phase de contre-attaque
        if ("COUNTER".equals(phase)) {
            TeamSide defendingTeam = getOppositeTeam(attackingTeam);
            if (payload.team() != defendingTeam) {
                return state;
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);
            // Que la contre-attaque réussisse ou non, on passe à la manche suivante
            return advanceToNextManche(state);
        }

        return state;
    }

    private RoundState advanceToNextManche(RoundState state) {
        int nextIndex = state.questionIndex() + 1;

        if (nextIndex >= TOTAL_MANCHES) {
            return state.completed();
        }

        // Alterner les rôles
        TeamSide currentAttacker = TeamSide.valueOf(getExtra(state, ATTACKING_TEAM_KEY, "A"));
        TeamSide newAttacker = getOppositeTeam(currentAttacker);

        RoundState newState = new RoundState(
                state.type(),
                RoundState.Phase.ANNOUNCE,
                nextIndex,
                null,
                null,
                THEME_SELECTION_TIME_MS,
                false,
                state.extra()
        );

        return withExtras(newState, Map.of(
                PHASE_KEY, "THEME_SELECTION",
                ATTACKING_TEAM_KEY, newAttacker.name(),
                SELECTED_THEME_KEY, ""
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "ATTACK");

        if ("THEME_SELECTION".equals(phase)) {
            // Auto-sélection d'un thème
            return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "ATTACK",
                    SELECTED_THEME_KEY, "AUTO"
            ));
        } else if ("ATTACK".equals(phase)) {
            // L'attaque a échoué par timeout, contre-attaque possible
            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "COUNTER"
            ));
        } else {
            // Contre-attaque échouée par timeout
            return advanceToNextManche(state);
        }
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring appliqué par le GameEngine
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }

    public int getAttackPoints() {
        return ATTACK_POINTS;
    }

    public int getCounterPoints() {
        return COUNTER_POINTS;
    }
}
