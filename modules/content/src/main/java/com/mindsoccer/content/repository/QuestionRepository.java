package com.mindsoccer.content.repository;

import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.protocol.enums.Country;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
import com.mindsoccer.protocol.enums.QuestionType;
import com.mindsoccer.protocol.enums.RoundType;
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

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID>, JpaSpecificationExecutor<QuestionEntity> {

    Page<QuestionEntity> findByActiveTrue(Pageable pageable);

    Page<QuestionEntity> findByThemeIdAndActiveTrue(UUID themeId, Pageable pageable);

    Page<QuestionEntity> findByDifficultyAndActiveTrue(Difficulty difficulty, Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true AND q.theme.id = :themeId " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByTheme(@Param("themeId") UUID themeId,
                                           @Param("excludeIds") List<UUID> excludeIds,
                                           Pageable pageable);

    /**
     * Trouve des questions aléatoires utilisables pour un mode de jeu donné.
     * Une question peut appartenir à plusieurs modes.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND :roundType MEMBER OF q.roundTypes " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByRoundType(@Param("roundType") RoundType roundType,
                                               @Param("excludeIds") List<UUID> excludeIds,
                                               Pageable pageable);

    /**
     * Trouve des questions par mode de jeu (paginé).
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true AND :roundType MEMBER OF q.roundTypes")
    Page<QuestionEntity> findByRoundTypeAndActiveTrue(@Param("roundType") RoundType roundType, Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.difficulty = :difficulty " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByDifficulty(@Param("difficulty") Difficulty difficulty,
                                                @Param("excludeIds") List<UUID> excludeIds,
                                                Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND :roundType MEMBER OF q.roundTypes AND q.difficulty = :difficulty " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByRoundTypeAndDifficulty(@Param("roundType") RoundType roundType,
                                                            @Param("difficulty") Difficulty difficulty,
                                                            @Param("excludeIds") List<UUID> excludeIds,
                                                            Pageable pageable);

    // ========== Queries par catégorie ==========

    /**
     * Trouve des questions aléatoires par catégorie.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND :category MEMBER OF q.categories " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByCategory(@Param("category") String category,
                                              @Param("excludeIds") List<UUID> excludeIds,
                                              Pageable pageable);

    /**
     * Trouve des questions par catégorie (paginé).
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true AND :category MEMBER OF q.categories")
    Page<QuestionEntity> findByCategoryAndActiveTrue(@Param("category") String category, Pageable pageable);

    /**
     * Liste toutes les catégories distinctes.
     */
    @Query("SELECT DISTINCT c FROM QuestionEntity q JOIN q.categories c WHERE q.active = true")
    List<String> findDistinctCategories();

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.theme.id = :themeId AND q.difficulty = :difficulty " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByThemeAndDifficulty(@Param("themeId") UUID themeId,
                                                        @Param("difficulty") Difficulty difficulty,
                                                        @Param("excludeIds") List<UUID> excludeIds,
                                                        Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.imposedLetter = :letter " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByImposedLetter(@Param("letter") String letter,
                                                   @Param("excludeIds") List<UUID> excludeIds,
                                                   Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.questionFormat = :format " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByFormat(@Param("format") QuestionFormat format,
                                            @Param("excludeIds") List<UUID> excludeIds,
                                            Pageable pageable);

    @Modifying
    @Query("UPDATE QuestionEntity q SET q.usageCount = q.usageCount + 1 WHERE q.id = :id")
    void incrementUsageCount(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE QuestionEntity q SET q.successCount = q.successCount + 1 WHERE q.id = :id")
    void incrementSuccessCount(@Param("id") UUID id);

    @Query("SELECT DISTINCT q.imposedLetter FROM QuestionEntity q " +
            "WHERE q.active = true AND q.imposedLetter IS NOT NULL")
    List<String> findDistinctImposedLetters();

    long countByThemeIdAndActiveTrue(UUID themeId);

    long countByDifficultyAndActiveTrue(Difficulty difficulty);

    /**
     * Compte les questions actives pour un mode de jeu donné.
     */
    @Query("SELECT COUNT(q) FROM QuestionEntity q WHERE q.active = true AND :roundType MEMBER OF q.roundTypes")
    long countByRoundTypeAndActiveTrue(@Param("roundType") RoundType roundType);

    /**
     * Compte les questions actives pour une catégorie donnée.
     */
    @Query("SELECT COUNT(q) FROM QuestionEntity q WHERE q.active = true AND :category MEMBER OF q.categories")
    long countByCategoryAndActiveTrue(@Param("category") String category);

    @Query("SELECT q.difficulty, COUNT(q) FROM QuestionEntity q WHERE q.active = true GROUP BY q.difficulty")
    List<Object[]> countByDifficultyGrouped();

    /**
     * Compte les questions par mode de jeu (pour stats).
     * Note: Une question peut être comptée plusieurs fois si elle appartient à plusieurs modes.
     */
    @Query("SELECT rt, COUNT(q) FROM QuestionEntity q JOIN q.roundTypes rt WHERE q.active = true GROUP BY rt")
    List<Object[]> countByRoundTypeGrouped();

    /**
     * Compte les questions par catégorie (pour stats).
     */
    @Query("SELECT c, COUNT(q) FROM QuestionEntity q JOIN q.categories c WHERE q.active = true GROUP BY c")
    List<Object[]> countByCategoryGrouped();

    // ========== Queries pour SAUT PATRIOTIQUE (par pays) ==========

    /**
     * Trouve des questions aléatoires pour un pays donné.
     * Utilisé pour le mode SAUT_PATRIOTIQUE.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true AND q.country = :country " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByCountry(@Param("country") Country country,
                                             @Param("excludeIds") List<UUID> excludeIds,
                                             Pageable pageable);

    Page<QuestionEntity> findByCountryAndActiveTrue(Country country, Pageable pageable);

    long countByCountryAndActiveTrue(Country country);

    @Query("SELECT q.country, COUNT(q) FROM QuestionEntity q WHERE q.active = true AND q.country IS NOT NULL GROUP BY q.country")
    List<Object[]> countByCountryGrouped();

    // ========== Queries par QuestionType ==========

    /**
     * Trouve des questions aléatoires par type.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true AND q.questionType = :type " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByQuestionType(@Param("type") QuestionType type,
                                                  @Param("excludeIds") List<UUID> excludeIds,
                                                  Pageable pageable);

    Page<QuestionEntity> findByQuestionTypeAndActiveTrue(QuestionType questionType, Pageable pageable);

    long countByQuestionTypeAndActiveTrue(QuestionType questionType);

    // ========== Queries combinées pour modes spécifiques ==========

    /**
     * Pour RANDONNEE_LEXICALE : Questions alphabétiques pour une lettre donnée.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.questionType = 'ALPHABETIQUE' AND q.imposedLetter = :letter " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomAlphabeticByLetter(@Param("letter") String letter,
                                                      @Param("excludeIds") List<UUID> excludeIds,
                                                      Pageable pageable);

    /**
     * Pour SPRINT_FINAL : Questions éclairs (rapides).
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.questionType = 'ECLAIR' " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomEclairQuestions(@Param("excludeIds") List<UUID> excludeIds,
                                                   Pageable pageable);

    /**
     * Pour CAPOEIRA : Questions sur le thème musique.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.theme.code = 'musique' " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomMusicQuestions(@Param("excludeIds") List<UUID> excludeIds,
                                                  Pageable pageable);

    /**
     * Pour ECHAPPEE : Questions géographiques.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.questionType = 'GEOGRAPHIQUE' " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomGeographicQuestions(@Param("excludeIds") List<UUID> excludeIds,
                                                       Pageable pageable);

    /**
     * Pour CIME : Questions de difficulté croissante.
     */
    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.difficulty = :difficulty " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomForCime(@Param("difficulty") Difficulty difficulty,
                                           @Param("excludeIds") List<UUID> excludeIds,
                                           Pageable pageable);
}
