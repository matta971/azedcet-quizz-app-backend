package com.mindsoccer.content.repository;

import com.mindsoccer.content.entity.QuestionEntity;
import com.mindsoccer.protocol.enums.Difficulty;
import com.mindsoccer.protocol.enums.QuestionFormat;
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

    Page<QuestionEntity> findByRoundTypeAndActiveTrue(RoundType roundType, Pageable pageable);

    Page<QuestionEntity> findByDifficultyAndActiveTrue(Difficulty difficulty, Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true AND q.theme.id = :themeId " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByTheme(@Param("themeId") UUID themeId,
                                           @Param("excludeIds") List<UUID> excludeIds,
                                           Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true AND q.roundType = :roundType " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByRoundType(@Param("roundType") RoundType roundType,
                                               @Param("excludeIds") List<UUID> excludeIds,
                                               Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.difficulty = :difficulty " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByDifficulty(@Param("difficulty") Difficulty difficulty,
                                                @Param("excludeIds") List<UUID> excludeIds,
                                                Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.active = true " +
            "AND q.roundType = :roundType AND q.difficulty = :difficulty " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<QuestionEntity> findRandomByRoundTypeAndDifficulty(@Param("roundType") RoundType roundType,
                                                            @Param("difficulty") Difficulty difficulty,
                                                            @Param("excludeIds") List<UUID> excludeIds,
                                                            Pageable pageable);

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

    long countByRoundTypeAndActiveTrue(RoundType roundType);

    long countByDifficultyAndActiveTrue(Difficulty difficulty);

    @Query("SELECT q.difficulty, COUNT(q) FROM QuestionEntity q WHERE q.active = true GROUP BY q.difficulty")
    List<Object[]> countByDifficultyGrouped();

    @Query("SELECT q.roundType, COUNT(q) FROM QuestionEntity q WHERE q.active = true AND q.roundType IS NOT NULL GROUP BY q.roundType")
    List<Object[]> countByRoundTypeGrouped();
}
