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
import java.util.Random;

/**
 * Plugin JACKPOT - Questions avec multiplicateurs aléatoires.
 *
 * Règles:
 * - 6 questions au total
 * - Chaque question a un multiplicateur aléatoire (x1, x2, x3)
 * - Base: 10 points x multiplicateur
 * - Mode buzzer: le premier à buzzer peut répondre
 * - Mauvaise réponse: l'autre équipe peut voler (sans multiplicateur)
 * - Super Jackpot: si on obtient 3 fois le x3, bonus de +30 points
 */
@Component

public class JackpotPlugin extends AbstractRulePlugin {

    private static final int TOTAL_QUESTIONS = 6;
    private static final int BASE_POINTS = 10;
    private static final int STEAL_POINTS = 10;
    private static final int SUPER_JACKPOT_BONUS = 30;
    private static final long ANSWER_TIME_MS = 10_000L;

    private static final String PHASE_KEY = "phase"; // BUZZER, ANSWER, STEAL
    private static final String BUZZER_WINNER_KEY = "buzzerWinner";
    private static final String CURRENT_MULTIPLIER_KEY = "currentMultiplier";
    private static final String X3_COUNT_A_KEY = "x3CountA";
    private static final String X3_COUNT_B_KEY = "x3CountB";

    private final Random random = new Random();

    public JackpotPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.JACKPOT;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.JACKPOT);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.ANSWER_WINDOW), ANSWER_TIME_MS), Map.of(
                PHASE_KEY, "BUZZER",
                BUZZER_WINNER_KEY, "",
                CURRENT_MULTIPLIER_KEY, generateMultiplier(),
                X3_COUNT_A_KEY, 0,
                X3_COUNT_B_KEY, 0
        ));
    }

    private int generateMultiplier() {
        int roll = random.nextInt(100);
        if (roll < 50) {
            return 1; // 50% chance
        } else if (roll < 85) {
            return 2; // 35% chance
        } else {
            return 3; // 15% chance
        }
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

        String phase = getExtra(state, PHASE_KEY, "BUZZER");
        String buzzerWinner = getExtra(state, BUZZER_WINNER_KEY, "");
        String playerIdStr = payload.playerId().toString();

        // Phase buzzer
        if ("BUZZER".equals(phase)) {
            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "ANSWER",
                    BUZZER_WINNER_KEY, playerIdStr
            ));
        }

        // Phase vol
        if ("STEAL".equals(phase)) {
            // L'équipe adverse peut voler
            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            if (correct) {
                return advanceToNextQuestion(state);
            } else {
                return advanceToNextQuestion(state);
            }
        }

        // Phase réponse
        if ("ANSWER".equals(phase)) {
            if (!playerIdStr.equals(buzzerWinner)) {
                return state;
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            if (correct) {
                int multiplier = getExtra(state, CURRENT_MULTIPLIER_KEY, 1);

                // Comptabiliser les x3
                if (multiplier == 3) {
                    String x3Key = payload.team() == TeamSide.A ? X3_COUNT_A_KEY : X3_COUNT_B_KEY;
                    int x3Count = getExtra(state, x3Key, 0) + 1;
                    return advanceToNextQuestion(withExtra(state, x3Key, x3Count));
                }

                return advanceToNextQuestion(state);
            } else {
                return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                        PHASE_KEY, "STEAL"
                ));
            }
        }

        return state;
    }

    private RoundState advanceToNextQuestion(RoundState state) {
        int nextIndex = state.questionIndex() + 1;

        if (nextIndex >= TOTAL_QUESTIONS) {
            return finishRound(state);
        }

        RoundState newState = new RoundState(
                state.type(),
                RoundState.Phase.ANSWER_WINDOW,
                nextIndex,
                null,
                null,
                ANSWER_TIME_MS,
                false,
                state.extra()
        );

        return withExtras(newState, Map.of(
                PHASE_KEY, "BUZZER",
                BUZZER_WINNER_KEY, "",
                CURRENT_MULTIPLIER_KEY, generateMultiplier()
        ));
    }

    private RoundState finishRound(RoundState state) {
        int x3CountA = getExtra(state, X3_COUNT_A_KEY, 0);
        int x3CountB = getExtra(state, X3_COUNT_B_KEY, 0);

        String superJackpotTeam = "";
        int bonus = 0;

        if (x3CountA >= 3) {
            superJackpotTeam = TeamSide.A.name();
            bonus = SUPER_JACKPOT_BONUS;
        }
        if (x3CountB >= 3) {
            superJackpotTeam = TeamSide.B.name();
            bonus = SUPER_JACKPOT_BONUS;
        }

        return withExtras(state.completed(), Map.of(
                "superJackpotTeam", superJackpotTeam,
                "superJackpotBonus", bonus
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "BUZZER");

        if ("BUZZER".equals(phase)) {
            return advanceToNextQuestion(state);
        } else if ("STEAL".equals(phase)) {
            return advanceToNextQuestion(state);
        } else {
            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "STEAL"
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

    public int getSuperJackpotBonus() {
        return SUPER_JACKPOT_BONUS;
    }
}
