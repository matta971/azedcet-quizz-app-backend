package com.mindsoccer.content.repository;

import com.mindsoccer.content.entity.JackpotQuestionEntity;
import com.mindsoccer.protocol.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository pour les questions à indices (Jackpot, Estocade).
 */
@Repository
public interface JackpotQuestionRepository extends JpaRepository<JackpotQuestionEntity, UUID>,
        JpaSpecificationExecutor<JackpotQuestionEntity> {

    Page<JackpotQuestionEntity> findByActiveTrue(Pageable pageable);

    Page<JackpotQuestionEntity> findByDifficultyAndActiveTrue(Difficulty difficulty, Pageable pageable);

    Page<JackpotQuestionEntity> findByThemeIdAndActiveTrue(UUID themeId, Pageable pageable);

    Page<JackpotQuestionEntity> findByCategoryAndActiveTrue(String category, Pageable pageable);

    /**
     * Trouve des questions aléatoires pour le mode JACKPOT.
     */
    @Query("SELECT q FROM JackpotQuestionEntity q WHERE q.active = true " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<JackpotQuestionEntity> findRandomQuestions(@Param("excludeIds") List<UUID> excludeIds,
                                                    Pageable pageable);

    /**
     * Trouve des questions aléatoires par difficulté.
     */
    @Query("SELECT q FROM JackpotQuestionEntity q WHERE q.active = true " +
            "AND q.difficulty = :difficulty " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<JackpotQuestionEntity> findRandomByDifficulty(@Param("difficulty") Difficulty difficulty,
                                                       @Param("excludeIds") List<UUID> excludeIds,
                                                       Pageable pageable);

    /**
     * Trouve des questions aléatoires par thème.
     */
    @Query("SELECT q FROM JackpotQuestionEntity q WHERE q.active = true " +
            "AND q.theme.id = :themeId " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<JackpotQuestionEntity> findRandomByTheme(@Param("themeId") UUID themeId,
                                                  @Param("excludeIds") List<UUID> excludeIds,
                                                  Pageable pageable);

    /**
     * Trouve des questions aléatoires par catégorie.
     */
    @Query("SELECT q FROM JackpotQuestionEntity q WHERE q.active = true " +
            "AND q.category = :category " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<JackpotQuestionEntity> findRandomByCategory(@Param("category") String category,
                                                     @Param("excludeIds") List<UUID> excludeIds,
                                                     Pageable pageable);

    @Modifying
    @Query("UPDATE JackpotQuestionEntity q SET q.usageCount = q.usageCount + 1 WHERE q.id = :id")
    void incrementUsageCount(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE JackpotQuestionEntity q SET q.successAtHint1 = q.successAtHint1 + 1, q.successCount = q.successCount + 1 WHERE q.id = :id")
    void incrementSuccessAtHint1(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE JackpotQuestionEntity q SET q.successAtHint2 = q.successAtHint2 + 1, q.successCount = q.successCount + 1 WHERE q.id = :id")
    void incrementSuccessAtHint2(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE JackpotQuestionEntity q SET q.successAtHint3 = q.successAtHint3 + 1, q.successCount = q.successCount + 1 WHERE q.id = :id")
    void incrementSuccessAtHint3(@Param("id") UUID id);

    long countByActiveTrue();

    long countByDifficultyAndActiveTrue(Difficulty difficulty);

    long countByThemeIdAndActiveTrue(UUID themeId);

    @Query("SELECT DISTINCT q.category FROM JackpotQuestionEntity q WHERE q.active = true AND q.category IS NOT NULL")
    List<String> findDistinctCategories();

    @Query("SELECT q.difficulty, COUNT(q) FROM JackpotQuestionEntity q WHERE q.active = true GROUP BY q.difficulty")
    List<Object[]> countByDifficultyGrouped();

    @Query("SELECT q.category, COUNT(q) FROM JackpotQuestionEntity q WHERE q.active = true AND q.category IS NOT NULL GROUP BY q.category")
    List<Object[]> countByCategoryGrouped();
}
