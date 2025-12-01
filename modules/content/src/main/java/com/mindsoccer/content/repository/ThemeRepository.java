package com.mindsoccer.content.repository;

import com.mindsoccer.content.entity.ThemeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ThemeRepository extends JpaRepository<ThemeEntity, UUID> {

    Optional<ThemeEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<ThemeEntity> findByActiveTrue();

    Page<ThemeEntity> findByActiveTrue(Pageable pageable);

    @Query("SELECT t FROM ThemeEntity t WHERE t.active = true ORDER BY FUNCTION('RANDOM')")
    List<ThemeEntity> findRandomActiveThemes(Pageable pageable);

    @Query("SELECT t FROM ThemeEntity t WHERE t.active = true AND t.id NOT IN :excludeIds ORDER BY FUNCTION('RANDOM')")
    List<ThemeEntity> findRandomActiveThemesExcluding(@Param("excludeIds") List<UUID> excludeIds, Pageable pageable);

    @Query("SELECT t FROM ThemeEntity t WHERE LOWER(t.nameFr) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ThemeEntity> searchThemes(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(q) FROM QuestionEntity q WHERE q.theme.id = :themeId AND q.active = true")
    long countActiveQuestionsByThemeId(@Param("themeId") UUID themeId);
}
