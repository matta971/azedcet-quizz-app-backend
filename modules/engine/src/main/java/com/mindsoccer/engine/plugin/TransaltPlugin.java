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
 * Plugin TRANSALT - Questions de traduction et linguistique.
 *
 * Règles:
 * - 8 questions de traduction (Français ↔ autre langue)
 * - Questions alternées entre les équipes
 * - +12 points par bonne traduction
 * - Bonus de prononciation: +3 points (évalué par l'arbitre en mode présentiel)
 * - L'autre équipe peut voler: +8 points
 */
@Component
public class TransaltPlugin extends AbstractRulePlugin {

    private static final int TOTAL_QUESTIONS = 8;
    private static final int BASE_POINTS = 12;
    private static final int PRONUNCIATION_BONUS = 3;
    private static final int STEAL_POINTS = 8;
    private static final long ANSWER_TIME_MS = 15_000L;

    private static final String PHASE_KEY = "phase";
    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String PRONUNCIATION_PENDING_KEY = "pronunciationPending";

    public TransaltPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.TRANSALT;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.TRANSALT);

        TeamSide startingTeam = ctx.leadingTeam() == null ? TeamSide.A :
                (ctx.leadingTeam() == TeamSide.A ? TeamSide.B : TeamSide.A);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                PHASE_KEY, "ANSWER",
                CURRENT_TEAM_KEY, startingTeam.name(),
                PRONUNCIATION_PENDING_KEY, false
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

        String phase = getExtra(state, PHASE_KEY, "ANSWER");
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));

        if ("STEAL".equals(phase)) {
            TeamSide expectedTeam = getOppositeTeam(currentTeam);
            if (payload.team() != expectedTeam) {
                return state;
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);
            return advanceToNextQuestion(state);
        }

        if (payload.team() != currentTeam) {
            return state;
        }

        boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

        if (correct) {
            // Bonne réponse, le bonus de prononciation peut être ajouté par l'arbitre
            RoundState newState = withExtra(state, PRONUNCIATION_PENDING_KEY, true);
            return advanceToNextQuestion(newState);
        } else {
            TeamSide oppositeTeam = getOppositeTeam(currentTeam);
            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "STEAL",
                    CURRENT_TEAM_KEY, oppositeTeam.name()
            ));
        }
    }

    private RoundState advanceToNextQuestion(RoundState state) {
        int nextIndex = state.questionIndex() + 1;

        if (nextIndex >= TOTAL_QUESTIONS) {
            return state.completed();
        }

        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
        TeamSide nextTeam = getOppositeTeam(currentTeam);

        RoundState newState = new RoundState(
                state.type(),
                RoundState.Phase.QUESTION_SHOWN,
                nextIndex,
                null,
                null,
                ANSWER_TIME_MS,
                false,
                state.extra()
        );

        return withExtras(newState, Map.of(
                PHASE_KEY, "ANSWER",
                CURRENT_TEAM_KEY, nextTeam.name(),
                PRONUNCIATION_PENDING_KEY, false
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "ANSWER");

        if ("STEAL".equals(phase)) {
            return advanceToNextQuestion(state);
        } else {
            TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
            TeamSide oppositeTeam = getOppositeTeam(currentTeam);
            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "STEAL",
                    CURRENT_TEAM_KEY, oppositeTeam.name()
            ));
        }
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring appliqué par le GameEngine
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }

    public int getBasePoints() {
        return BASE_POINTS;
    }

    public int getStealPoints() {
        return STEAL_POINTS;
    }

    public int getPronunciationBonus() {
        return PRONUNCIATION_BONUS;
    }
}
