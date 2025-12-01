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
 * Plugin CROSS-DICTIONARY - Mots croisés et définitions.
 *
 * Règles:
 * - Grille de mots croisés virtuelle (6 mots)
 * - Chaque équipe choisit une définition à tour de rôle
 * - +10 points par mot trouvé
 * - Bonus de connexion: +5 points si le mot partage une lettre avec un précédent
 * - Bonus grille complète: +20 points
 */
@Component
public class CrossDictionaryPlugin extends AbstractRulePlugin {

    private static final int TOTAL_WORDS = 6;
    private static final int BASE_POINTS = 10;
    private static final int CONNECTION_BONUS = 5;
    private static final int COMPLETE_GRID_BONUS = 20;
    private static final long ANSWER_TIME_MS = 20_000L;
    private static final long SELECTION_TIME_MS = 10_000L;

    private static final String PHASE_KEY = "phase";
    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String WORDS_A_KEY = "wordsFoundA";
    private static final String WORDS_B_KEY = "wordsFoundB";
    private static final String SELECTED_WORD_KEY = "selectedWord";

    public CrossDictionaryPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.CROSS_DICTIONARY;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.CROSS_DICTIONARY);

        TeamSide startingTeam = ctx.leadingTeam() == null ? TeamSide.A :
                (ctx.leadingTeam() == TeamSide.A ? TeamSide.B : TeamSide.A);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANNOUNCE), SELECTION_TIME_MS), Map.of(
                PHASE_KEY, "SELECTION",
                CURRENT_TEAM_KEY, startingTeam.name(),
                WORDS_A_KEY, 0,
                WORDS_B_KEY, 0,
                SELECTED_WORD_KEY, -1
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

        String phase = getExtra(state, PHASE_KEY, "SELECTION");
        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));

        if (payload.team() != currentTeam) {
            return state;
        }

        if ("SELECTION".equals(phase)) {
            // L'équipe sélectionne un mot (l'index est encodé dans la réponse)
            int wordIndex;
            try {
                wordIndex = Integer.parseInt(payload.answer());
            } catch (NumberFormatException e) {
                wordIndex = state.questionIndex();
            }

            return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "ANSWER",
                    SELECTED_WORD_KEY, wordIndex
            ));
        }

        if ("ANSWER".equals(phase)) {
            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            boolean isTeamA = currentTeam == TeamSide.A;
            String wordsKey = isTeamA ? WORDS_A_KEY : WORDS_B_KEY;

            if (correct) {
                int words = getExtra(state, wordsKey, 0) + 1;
                RoundState newState = withExtra(state, wordsKey, words);
                return advanceToNextWord(newState);
            } else {
                return advanceToNextWord(state);
            }
        }

        return state;
    }

    private RoundState advanceToNextWord(RoundState state) {
        int nextIndex = state.questionIndex() + 1;

        if (nextIndex >= TOTAL_WORDS) {
            return finishRound(state);
        }

        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));
        TeamSide nextTeam = getOppositeTeam(currentTeam);

        RoundState newState = new RoundState(
                state.type(),
                RoundState.Phase.ANNOUNCE,
                nextIndex,
                null,
                null,
                SELECTION_TIME_MS,
                false,
                state.extra()
        );

        return withExtras(newState, Map.of(
                PHASE_KEY, "SELECTION",
                CURRENT_TEAM_KEY, nextTeam.name(),
                SELECTED_WORD_KEY, -1
        ));
    }

    private RoundState finishRound(RoundState state) {
        int wordsA = getExtra(state, WORDS_A_KEY, 0);
        int wordsB = getExtra(state, WORDS_B_KEY, 0);

        String bonusTeam = "";
        int bonus = 0;

        if (wordsA + wordsB == TOTAL_WORDS) {
            if (wordsA > wordsB) {
                bonusTeam = TeamSide.A.name();
                bonus = COMPLETE_GRID_BONUS;
            } else if (wordsB > wordsA) {
                bonusTeam = TeamSide.B.name();
                bonus = COMPLETE_GRID_BONUS;
            }
        }

        return withExtras(state.completed(), Map.of(
                "finalWordsA", wordsA,
                "finalWordsB", wordsB,
                "completeGridTeam", bonusTeam,
                "completeGridBonus", bonus
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "SELECTION");

        if ("SELECTION".equals(phase)) {
            return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "ANSWER",
                    SELECTED_WORD_KEY, state.questionIndex()
            ));
        } else {
            return advanceToNextWord(state);
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

    public int getConnectionBonus() {
        return CONNECTION_BONUS;
    }

    public int getCompleteGridBonus() {
        return COMPLETE_GRID_BONUS;
    }
}
