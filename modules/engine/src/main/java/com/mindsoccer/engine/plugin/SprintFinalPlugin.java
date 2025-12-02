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
import java.util.Map;

/**
 * Plugin SPRINT FINAL.
 *
 * Règles:
 * - Dernière rubrique du match
 * - 20 questions en buzzer (première équipe à buzzer répond)
 * - 10 points par bonne réponse
 * - Questions rapides, pas de temps de réflexion
 * - Rubrique décisive pour départager les équipes
 */
@Component
@Profile("!test")
public class SprintFinalPlugin extends AbstractRulePlugin {

    private static final String TEAM_A_SCORE_KEY = "teamAScore";
    private static final String TEAM_B_SCORE_KEY = "teamBScore";
    private static final String QUESTIONS_ASKED_KEY = "questionsAsked";
    private static final String BUZZER_TEAM_KEY = "buzzerTeam";
    private static final String WAITING_BUZZER_KEY = "waitingBuzzer";

    public SprintFinalPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.SPRINT_FINAL;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.SPRINT_FINAL);
        return withExtras(state, Map.of(
                TEAM_A_SCORE_KEY, 0,
                TEAM_B_SCORE_KEY, 0,
                QUESTIONS_ASKED_KEY, 0,
                WAITING_BUZZER_KEY, true
        )).withPhase(RoundState.Phase.QUESTION_SHOWN);
    }

    @Override
    public RoundState onTick(MatchContext ctx, Duration dt) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        // En phase de réponse, vérifier le timeout
        if (state.phase() == RoundState.Phase.ANSWER_WINDOW) {
            RoundState newState = decrementTime(state, dt);
            if (isTimeUp(newState)) {
                // Timeout: question perdue, passer à la suivante
                return advanceToNextQuestion(newState);
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

        boolean waitingBuzzer = getExtra(state, WAITING_BUZZER_KEY, true);

        // Phase de buzzer
        if (waitingBuzzer && state.phase() == RoundState.Phase.QUESTION_SHOWN) {
            // Premier buzzer
            return withExtras(state, Map.of(
                    BUZZER_TEAM_KEY, payload.team(),
                    WAITING_BUZZER_KEY, false
            )).withPhase(RoundState.Phase.ANSWER_WINDOW);
        }

        // Phase de réponse
        if (state.phase() == RoundState.Phase.ANSWER_WINDOW) {
            TeamSide buzzerTeam = getExtra(state, BUZZER_TEAM_KEY, payload.team());

            if (payload.team() != buzzerTeam) {
                return state; // Pas l'équipe qui a buzzé
            }

            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            // Mettre à jour le score
            String scoreKey = buzzerTeam == TeamSide.A ? TEAM_A_SCORE_KEY : TEAM_B_SCORE_KEY;
            int currentScore = getExtra(state, scoreKey, 0);
            if (correct) {
                currentScore += GameConstants.DEFAULT_POINTS;
            }

            RoundState newState = withExtras(state, Map.of(
                    scoreKey, currentScore,
                    "lastCorrect", correct,
                    "lastTeam", buzzerTeam
            ));

            return advanceToNextQuestion(newState);
        }

        return state;
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring appliqué après chaque question
    }

    private RoundState advanceToNextQuestion(RoundState state) {
        int questionsAsked = getExtra(state, QUESTIONS_ASKED_KEY, 0) + 1;

        if (questionsAsked >= GameConstants.SPRINT_FINAL_QUESTION_COUNT) {
            // Round terminé
            return withExtra(state.completed(), QUESTIONS_ASKED_KEY, questionsAsked);
        }

        // Prochaine question
        return withExtras(state, Map.of(
                QUESTIONS_ASKED_KEY, questionsAsked,
                WAITING_BUZZER_KEY, true,
                BUZZER_TEAM_KEY, null
        )).withPhase(RoundState.Phase.TRANSITION);
    }

    /**
     * Obtient le score final de l'équipe pour cette rubrique.
     */
    public int getTeamScore(RoundState state, TeamSide team) {
        String key = team == TeamSide.A ? TEAM_A_SCORE_KEY : TEAM_B_SCORE_KEY;
        return getExtra(state, key, 0);
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }
}
