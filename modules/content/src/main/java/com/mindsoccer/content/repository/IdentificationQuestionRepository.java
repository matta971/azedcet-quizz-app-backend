package com.mindsoccer.content.repository;

import com.mindsoccer.content.entity.IdentificationQuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IdentificationQuestionRepository extends JpaRepository<IdentificationQuestionEntity, UUID> {

    Page<IdentificationQuestionEntity> findByActiveTrue(Pageable pageable);

    Page<IdentificationQuestionEntity> findByCategoryAndActiveTrue(String category, Pageable pageable);

    @Query("SELECT q FROM IdentificationQuestionEntity q WHERE q.active = true " +
            "AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<IdentificationQuestionEntity> findRandomActive(@Param("excludeIds") List<UUID> excludeIds,
                                                        Pageable pageable);

    @Query("SELECT q FROM IdentificationQuestionEntity q WHERE q.active = true " +
            "AND q.category = :category AND q.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<IdentificationQuestionEntity> findRandomByCategory(@Param("category") String category,
                                                            @Param("excludeIds") List<UUID> excludeIds,
                                                            Pageable pageable);

    @Modifying
    @Query("UPDATE IdentificationQuestionEntity q SET q.usageCount = q.usageCount + 1 WHERE q.id = :id")
    void incrementUsageCount(@Param("id") UUID id);

    @Query("SELECT DISTINCT q.category FROM IdentificationQuestionEntity q WHERE q.active = true AND q.category IS NOT NULL")
    List<String> findDistinctCategories();

    long countByActiveTrue();

    long countByCategoryAndActiveTrue(String category);
}
