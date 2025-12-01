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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Plugin RANDONNÉE LEXICALE - Chaîne de mots thématique.
 *
 * Règles:
 * - Un thème est donné (ex: "Animaux", "Pays", "Verbes")
 * - Les équipes doivent trouver des mots alternativement
 * - Chaque mot doit commencer par la dernière lettre du mot précédent
 * - +5 points par mot valide
 * - Pas de répétition de mots déjà utilisés
 * - 10 secondes par mot
 * - Bonus de longueur: +2 points si le mot a 8+ lettres
 */
@Component
public class RandonneeLexicalePlugin extends AbstractRulePlugin {

    private static final int BASE_POINTS = 5;
    private static final int LENGTH_BONUS = 2;
    private static final int LENGTH_THRESHOLD = 8;
    private static final long ANSWER_TIME_MS = 10_000L;
    private static final int MAX_WORDS = 20;

    private static final String CURRENT_TEAM_KEY = "currentTeam";
    private static final String LAST_LETTER_KEY = "lastLetter";
    private static final String USED_WORDS_KEY = "usedWords";
    private static final String WORDS_A_KEY = "wordsA";
    private static final String WORDS_B_KEY = "wordsB";
    private static final String POINTS_A_KEY = "pointsA";
    private static final String POINTS_B_KEY = "pointsB";
    private static final String CHAIN_LENGTH_KEY = "chainLength";

    public RandonneeLexicalePlugin(ScoringService scoringService, AnswerValidationService validationService) {
        super(scoringService, validationService);
    }

    @Override
    public RoundType type() {
        return RoundType.RANDONNEE_LEXICALE;
    }

    @Override
    public RoundState init(MatchContext ctx) {
        RoundState state = RoundState.initial(RoundType.RANDONNEE_LEXICALE);

        TeamSide startingTeam = ctx.leadingTeam() == null ? TeamSide.A :
                (ctx.leadingTeam() == TeamSide.A ? TeamSide.B : TeamSide.A);

        return withExtras(withRemainingTime(state.withPhase(RoundState.Phase.QUESTION_SHOWN), ANSWER_TIME_MS), Map.of(
                CURRENT_TEAM_KEY, startingTeam.name(),
                LAST_LETTER_KEY, "",
                USED_WORDS_KEY, new ArrayList<String>(),
                WORDS_A_KEY, 0,
                WORDS_B_KEY, 0,
                POINTS_A_KEY, 0,
                POINTS_B_KEY, 0,
                CHAIN_LENGTH_KEY, 0
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
            return finishRound(newState);
        }

        return newState;
    }

    @Override
    public RoundState onAnswer(MatchContext ctx, AnswerPayload payload) {
        RoundState state = ctx.currentRoundState();

        if (state.finished()) {
            return state;
        }

        TeamSide currentTeam = TeamSide.valueOf(getExtra(state, CURRENT_TEAM_KEY, "A"));

        if (payload.team() != currentTeam) {
            return state;
        }

        String word = payload.answer().trim().toUpperCase();

        if (word.isEmpty()) {
            return finishRound(state);
        }

        String lastLetter = getExtra(state, LAST_LETTER_KEY, "");

        // Vérifier la première lettre
        if (!lastLetter.isEmpty() && !word.startsWith(lastLetter)) {
            return finishRound(state);
        }

        // Vérifier si le mot a déjà été utilisé
        @SuppressWarnings("unchecked")
        List<String> usedWords = new ArrayList<>((List<String>) state.extra().getOrDefault(USED_WORDS_KEY, new ArrayList<>()));
        if (usedWords.contains(word)) {
            return finishRound(state);
        }

        // Le mot est valide (la validation du thème devrait être faite par le GameEngine)
        usedWords.add(word);

        boolean isTeamA = currentTeam == TeamSide.A;
        String wordsKey = isTeamA ? WORDS_A_KEY : WORDS_B_KEY;
        String pointsKey = isTeamA ? POINTS_A_KEY : POINTS_B_KEY;

        int words = getExtra(state, wordsKey, 0) + 1;
        int points = getExtra(state, pointsKey, 0);
        int chainLength = getExtra(state, CHAIN_LENGTH_KEY, 0) + 1;

        int earnedPoints = BASE_POINTS;
        if (word.length() >= LENGTH_THRESHOLD) {
            earnedPoints += LENGTH_BONUS;
        }
        points += earnedPoints;

        String newLastLetter = String.valueOf(word.charAt(word.length() - 1));

        // Vérifier la limite de mots
        if (chainLength >= MAX_WORDS) {
            RoundState newState = withExtras(state, Map.of(
                    wordsKey, words,
                    pointsKey, points,
                    CHAIN_LENGTH_KEY, chainLength,
                    USED_WORDS_KEY, usedWords,
                    LAST_LETTER_KEY, newLastLetter
            ));
            return finishRound(newState);
        }

        // Passer à l'autre équipe
        TeamSide nextTeam = getOppositeTeam(currentTeam);

        RoundState newState = new RoundState(
                state.type(),
                RoundState.Phase.QUESTION_SHOWN,
                state.questionIndex() + 1,
                null,
                null,
                ANSWER_TIME_MS,
                false,
                state.extra()
        );

        return withExtras(newState, Map.of(
                CURRENT_TEAM_KEY, nextTeam.name(),
                LAST_LETTER_KEY, newLastLetter,
                USED_WORDS_KEY, usedWords,
                wordsKey, words,
                pointsKey, points,
                CHAIN_LENGTH_KEY, chainLength
        ));
    }

    private RoundState finishRound(RoundState state) {
        int pointsA = getExtra(state, POINTS_A_KEY, 0);
        int pointsB = getExtra(state, POINTS_B_KEY, 0);
        int wordsA = getExtra(state, WORDS_A_KEY, 0);
        int wordsB = getExtra(state, WORDS_B_KEY, 0);
        int chainLength = getExtra(state, CHAIN_LENGTH_KEY, 0);

        String winner = "";
        if (pointsA > pointsB) {
            winner = TeamSide.A.name();
        } else if (pointsB > pointsA) {
            winner = TeamSide.B.name();
        }

        return withExtras(state.completed(), Map.of(
                "finalWordsA", wordsA,
                "finalWordsB", wordsB,
                "finalPointsA", pointsA,
                "finalPointsB", pointsB,
                "totalChainLength", chainLength,
                "roundWinner", winner
        ));
    }

    @Override
    public void applyScoring(MatchContext ctx) {
        // Scoring appliqué par le GameEngine
    }

    public int getBasePoints() {
        return BASE_POINTS;
    }

    public int getLengthBonus() {
        return LENGTH_BONUS;
    }

    public int getLengthThreshold() {
        return LENGTH_THRESHOLD;
    }
}
