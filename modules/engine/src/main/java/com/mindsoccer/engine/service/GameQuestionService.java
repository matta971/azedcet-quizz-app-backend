package com.mindsoccer.engine.service;

import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.content.service.QuestionService;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.RoundType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service qui fournit les questions pour les rounds de jeu.
 * Fait le lien entre le GameEngine et le ContentService.
 */
@Service
public class GameQuestionService {

    private static final Logger log = LoggerFactory.getLogger(GameQuestionService.class);

    private final QuestionService questionService;

    // Cache des questions utilisées par match (pour éviter les répétitions)
    private final Map<UUID, Set<UUID>> usedQuestionsPerMatch = new ConcurrentHashMap<>();

    // Cache des questions préchargées par match
    private final Map<UUID, Queue<QuestionEntity>> questionQueuePerMatch = new ConcurrentHashMap<>();

    public GameQuestionService(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Précharge les questions pour un round de type donné.
     *
     * @param matchId   L'ID du match
     * @param roundType Le type de round
     * @param count     Le nombre de questions à précharger
     */
    public void preloadQuestions(UUID matchId, RoundType roundType, int count) {
        Set<UUID> usedIds = usedQuestionsPerMatch.computeIfAbsent(matchId, k -> new HashSet<>());

        List<QuestionEntity> questions = questionService.getRandomByRoundType(
                roundType, count, usedIds
        );

        if (questions.isEmpty()) {
            // Fallback: charger des questions sans filtre de roundType
            questions = questionService.getRandomByDifficulty(
                    Difficulty.MEDIUM, count, usedIds
            );
        }

        Queue<QuestionEntity> queue = questionQueuePerMatch.computeIfAbsent(
                matchId, k -> new LinkedList<>()
        );
        queue.addAll(questions);

        log.info("Préchargé {} questions pour match {} (round {})", questions.size(), matchId, roundType);
    }

    /**
     * Récupère la prochaine question pour un match.
     *
     * @param matchId L'ID du match
     * @return La prochaine question ou null si épuisé
     */
    public QuestionEntity getNextQuestion(UUID matchId) {
        Queue<QuestionEntity> queue = questionQueuePerMatch.get(matchId);
        if (queue == null || queue.isEmpty()) {
            log.warn("Plus de questions disponibles pour match {}", matchId);
            return null;
        }

        QuestionEntity question = queue.poll();

        // Marquer comme utilisée
        Set<UUID> usedIds = usedQuestionsPerMatch.computeIfAbsent(matchId, k -> new HashSet<>());
        usedIds.add(question.getId());

        return question;
    }

    /**
     * Récupère une question spécifique pour le mode DUEL.
     */
    public QuestionEntity getDuelQuestion(UUID matchId, int questionIndex) {
        Set<UUID> usedIds = usedQuestionsPerMatch.computeIfAbsent(matchId, k -> new HashSet<>());

        List<QuestionEntity> questions = questionService.getRandomByRoundType(
                RoundType.DUEL, 1, usedIds
        );

        if (questions.isEmpty()) {
            // Fallback sur n'importe quelle question MEDIUM
            questions = questionService.getRandomByDifficulty(Difficulty.MEDIUM, 1, usedIds);
        }

        if (!questions.isEmpty()) {
            QuestionEntity q = questions.get(0);
            usedIds.add(q.getId());
            return q;
        }

        return null;
    }

    /**
     * Convertit une QuestionEntity en données pour le round (pour le state extra).
     */
    public Map<String, Object> toRoundData(QuestionEntity question) {
        if (question == null) {
            return Map.of();
        }

        return Map.of(
                "questionId", question.getId().toString(),
                "questionTextFr", question.getTextFr(),
                "questionTextEn", question.getTextEn() != null ? question.getTextEn() : question.getTextFr(),
                "expectedAnswer", question.getAnswer(),
                "alternativeAnswers", question.getAlternativeAnswers() != null
                        ? new ArrayList<>(question.getAlternativeAnswers())
                        : List.of(),
                "difficulty", question.getDifficulty().name(),
                "theme", question.getTheme() != null ? question.getTheme().getNameFr() : ""
        );
    }

    /**
     * Enregistre l'utilisation d'une question.
     */
    public void recordQuestionUsage(UUID questionId, boolean correct) {
        questionService.recordUsage(questionId, correct);
    }

    /**
     * Nettoie les données d'un match terminé.
     */
    public void cleanupMatch(UUID matchId) {
        usedQuestionsPerMatch.remove(matchId);
        questionQueuePerMatch.remove(matchId);
        log.debug("Nettoyage des questions pour match {}", matchId);
    }

    /**
     * Retourne le nombre de questions restantes pour un match.
     */
    public int getRemainingQuestionsCount(UUID matchId) {
        Queue<QuestionEntity> queue = questionQueuePerMatch.get(matchId);
        return queue != null ? queue.size() : 0;
    }
}
