package com.mindsoccer.engine.plugin;

import com.mindsoccer.engine.AnswerPayload;
import com.mindsoccer.engine.MatchContext;
import com.mindsoccer.engine.RoundState;
import com.mindsoccer.protocol.enums.RoundType;
import com.mindsoccer.protocol.enums.TeamSide;
import com.mindsoccer.scoring.service.AnswerValidationService;
import com.mindsoccer.scoring.service.ScoringService;
import com.mindsoccer.shared.util.GameConstants;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * Plugin SMASH (A et B).
 *
 * Règles:
 * - Question affichée à tout le monde
 * - Équipe concernée a 3 secondes pour annoncer qu'elle va répondre (buzzer)
 * - Si timeout, l'équipe adverse peut "smasher" (voler la question)
 * - Si smash et bonne réponse: +10 points pour l'équipe qui smash
 * - Si smash et mauvaise réponse: +10 points pour l'équipe d'origine
 */
@Component

public class SmashPlugin extends AbstractRulePlugin {

    private static final String SMASHED_KEY = "smashed";
    private static final String SMASH_TEAM_KEY = "smashTeam";
    private static final String ANNOUNCED_KEY = "announced";
    private static final String ORIGINAL_TEAM_KEY = "originalTeam";
    private static final String ANNOUNCE_DEADLINE_KEY = "announceDeadline";

    private final RoundType roundType;

    public SmashPlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
        this.roundType = RoundType.SMASH_A;
    }

    protected SmashPlugin(ScoringService scoringService, AnswerValidationService validationService, RoundType type) {
        super(scoringService, validationService);
        this.roundType = type;
    }

    @Override
    public RoundType type() {
        return roundType;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        TeamSide originalTeam = roundType == RoundType.SMASH_A ? TeamSide.A : TeamSide.B;
        RoundState state = RoundState.initial(roundType)
                .withPhase(RoundState.Phase.ANNOUNCE);

        return withExtras(state, Map.of(
                SMASHED_KEY, false,
                ANNOUNCED_KEY, false,
                ORIGINAL_TEAM_KEY, originalTeam,
                ANNOUNCE_DEADLINE_KEY, System.currentTimeMillis() + GameConstants.SMASH_ANNOUNCE_TIMEOUT_MS
        ));
    }

    @Override
    public RoundState onTick(MatchContext ctx, Duration dt) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        // Phase d'annonce: vérifier le timeout
        if (state.phase() == RoundState.Phase.ANNOUNCE) {
            long deadline = getExtra(state, ANNOUNCE_DEADLINE_KEY, 0L);
            boolean announced = getExtra(state, ANNOUNCED_KEY, false);

            if (!announced && System.currentTimeMillis() > deadline) {
                // Timeout d'annonce: l'équipe adverse peut smasher
                return withExtras(state, Map.of(
                        SMASHED_KEY, true,
                        SMASH_TEAM_KEY, getOppositeTeam(getExtra(state, ORIGINAL_TEAM_KEY, TeamSide.A))
                )).withPhase(RoundState.Phase.ANSWER_WINDOW);
            }
        }

        // Décrémenter le temps si en phase de réponse
        if (state.phase() == RoundState.Phase.ANSWER_WINDOW) {
            RoundState newState = decrementTime(state, dt);
            if (isTimeUp(newState)) {
                return handleTimeout(newState);
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

        boolean smashed = getExtra(state, SMASHED_KEY, false);
        TeamSide originalTeam = getExtra(state, ORIGINAL_TEAM_KEY, TeamSide.A);

        // Gérer l'annonce (buzzer)
        if (state.phase() == RoundState.Phase.ANNOUNCE) {
            if (payload.team() == originalTeam) {
                // L'équipe annonce qu'elle va répondre
                return withExtras(state, Map.of(ANNOUNCED_KEY, true))
                        .withPhase(RoundState.Phase.ANSWER_WINDOW);
            }
            return state; // Ignorer les buzzers de l'autre équipe pendant l'annonce
        }

        // Phase de réponse
        if (state.phase() == RoundState.Phase.ANSWER_WINDOW) {
            TeamSide respondingTeam = smashed ?
                    getExtra(state, SMASH_TEAM_KEY, getOppositeTeam(originalTeam)) :
                    originalTeam;

            if (payload.team() != respondingTeam) {
                return state; // Pas le tour de cette équipe
            }

            // Valider la réponse
            boolean correct = validateAnswer(payload.answer(), getExpectedAnswer(state), null);

            // Stocker le résultat pour le scoring
            return withExtras(state.completed(), Map.of(
                    "correct", correct,
                    "respondingTeam", respondingTeam,
                    "smashed", smashed,
                    "originalTeam", originalTeam
            ));
        }

        return state;
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring géré par le GameEngine
    }

    private RoundState handleTimeout(RoundState state) {
        // Timeout de réponse: l'équipe perd
        boolean smashed = getExtra(state, SMASHED_KEY, false);
        TeamSide originalTeam = getExtra(state, ORIGINAL_TEAM_KEY, TeamSide.A);

        return withExtras(state.completed(), Map.of(
                "correct", false,
                "timeout", true,
                "smashed", smashed,
                "originalTeam", originalTeam
        ));
    }

    private String getExpectedAnswer(RoundState state) {
        return (String) state.extra().getOrDefault("expectedAnswer", "");
    }

    /**
     * Factory pour créer le plugin SMASH_B.
     */
    @Component
    public static class SmashBPlugin extends SmashPlugin {
        public SmashBPlugin(ScoringService scoringService, AnswerValidationService validationService) {
            super(scoringService, validationService, RoundType.SMASH_B);
        }
    }
}
