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
import java.util.UUID;

/**
 * Plugin DUEL - Face à face entre deux joueurs.
 *
 * Règles:
 * - Un joueur de chaque équipe s'affronte
 * - 3 questions par duel
 * - Le premier à buzzer peut répondre
 * - Bonne réponse: +10 points
 * - Mauvaise réponse: l'adversaire peut voler (+5 points)
 * - Le gagnant du duel marque un bonus de +10 points
 */
@Component

public class DuelPlugin extends AbstractRulePlugin {

    private static final int QUESTIONS_PER_DUEL = 3;
    private static final int CORRECT_POINTS = 10;
    private static final int STEAL_POINTS = 5;
    private static final int DUEL_WINNER_BONUS = 10;
    private static final long ANSWER_TIME_MS = 10_000L;

    private static final String PHASE_KEY = "phase"; // BUZZER, ANSWER, STEAL
    private static final String BUZZER_WINNER_KEY = "buzzerWinner";
    private static final String DUEL_SCORE_A_KEY = "duelScoreA";
    private static final String DUEL_SCORE_B_KEY = "duelScoreB";
    private static final String DUELIST_A_KEY = "duelistA";
    private static final String DUELIST_B_KEY = "duelistB";

    public DuelPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.DUEL;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.DUEL);

        // Sélectionner les duellistes (premier joueur de chaque équipe)
        UUID duelistA = ctx.players().get(TeamSide.A).isEmpty() ? null : ctx.players().get(TeamSide.A).get(0);
        UUID duelistB = ctx.players().get(TeamSide.B).isEmpty() ? null : ctx.players().get(TeamSide.B).get(0);

        return withExtras(state.withPhase(RoundState.Phase.ANSWER_WINDOW), Map.of(
                PHASE_KEY, "BUZZER",
                BUZZER_WINNER_KEY, "",
                DUEL_SCORE_A_KEY, 0,
                DUEL_SCORE_B_KEY, 0,
                DUELIST_A_KEY, duelistA != null ? duelistA.toString() : "",
                DUELIST_B_KEY, duelistB != null ? duelistB.toString() : ""
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

        String phase = getExtra(state, PHASE_KEY, "BUZZER");
        String buzzerWinner = getExtra(state, BUZZER_WINNER_KEY, "");
        String playerIdStr = payload.playerId().toString();

        // Phase buzzer - enregistrer le premier à buzzer
        if ("BUZZER".equals(phase)) {
            String duelistA = getExtra(state, DUELIST_A_KEY, "");
            String duelistB = getExtra(state, DUELIST_B_KEY, "");

            if (!playerIdStr.equals(duelistA) && !playerIdStr.equals(duelistB)) {
                return state; // Pas un duelliste
            }

            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "ANSWER",
                    BUZZER_WINNER_KEY, playerIdStr
            ));
        }

        // Phase réponse
        if ("ANSWER".equals(phase)) {
            if (!playerIdStr.equals(buzzerWinner)) {
                return state; // Ce n'est pas le gagnant du buzzer
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            if (correct) {
                return processCorrectAnswer(state, payload.team(), CORRECT_POINTS);
            } else {
                // Passer en phase STEAL
                return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                        PHASE_KEY, "STEAL"
                ));
            }
        }

        // Phase vol
        if ("STEAL".equals(phase)) {
            String duelistA = getExtra(state, DUELIST_A_KEY, "");
            String duelistB = getExtra(state, DUELIST_B_KEY, "");
            String expectedStealer = buzzerWinner.equals(duelistA) ? duelistB : duelistA;

            if (!playerIdStr.equals(expectedStealer)) {
                return state; // Ce n'est pas l'adversaire
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            if (correct) {
                TeamSide stealerTeam = buzzerWinner.equals(duelistA) ? TeamSide.B : TeamSide.A;
                return processCorrectAnswer(state, stealerTeam, STEAL_POINTS);
            } else {
                return advanceToNextQuestion(state);
            }
        }

        return state;
    }

    private RoundState processCorrectAnswer(RoundState state, TeamSide team, int points) {
        String scoreKey = team == TeamSide.A ? DUEL_SCORE_A_KEY : DUEL_SCORE_B_KEY;
        int score = getExtra(state, scoreKey, 0) + 1;

        RoundState newState = withExtra(state, scoreKey, score);
        return advanceToNextQuestion(newState);
    }

    private RoundState advanceToNextQuestion(RoundState state) {
        int nextIndex = state.questionIndex() + 1;

        if (nextIndex >= QUESTIONS_PER_DUEL) {
            return finishDuel(state);
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
                BUZZER_WINNER_KEY, ""
        ));
    }

    private RoundState finishDuel(RoundState state) {
        int scoreA = getExtra(state, DUEL_SCORE_A_KEY, 0);
        int scoreB = getExtra(state, DUEL_SCORE_B_KEY, 0);

        TeamSide winner = null;
        if (scoreA > scoreB) {
            winner = TeamSide.A;
        } else if (scoreB > scoreA) {
            winner = TeamSide.B;
        }

        return withExtras(state.completed(), Map.of(
                "duelWinner", winner != null ? winner.name() : "TIE",
                "bonusPoints", winner != null ? DUEL_WINNER_BONUS : 0
        ));
    }

    private RoundState handleTimeout(RoundState state) {
        String phase = getExtra(state, PHASE_KEY, "BUZZER");

        if ("STEAL".equals(phase)) {
            return advanceToNextQuestion(state);
        } else if ("ANSWER".equals(phase)) {
            return withExtras(withRemainingTime(state, ANSWER_TIME_MS), Map.of(
                    PHASE_KEY, "STEAL"
            ));
        } else {
            // BUZZER timeout - personne n'a buzzé
            return advanceToNextQuestion(state);
        }
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring appliqué par le GameEngine
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }

    public int getCorrectPoints() {
        return CORRECT_POINTS;
    }

    public int getStealPoints() {
        return STEAL_POINTS;
    }

    public int getDuelWinnerBonus() {
        return DUEL_WINNER_BONUS;
    }
}
