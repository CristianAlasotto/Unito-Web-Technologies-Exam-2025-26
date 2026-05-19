package com.example.dataserverspringboot.entities.details;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Hidden
@Repository
public interface DetailsRepository extends JpaRepository<Details, Integer> {

    /**
     * COMBINED FILTER QUERY - All filters applied with AND logic.
     *
     * Uses pure JPQL (no nativeQuery) so Hibernate correctly maps
     * camelCase field names to snake_case columns via @Column annotations.
     *
     * The "lower(bytea)" PostgreSQL error is avoided by pre-building
     * wildcard patterns in DetailsService.likePattern() before calling
     * this method. Parameters searchPattern and genresPattern arrive
     * already as "%value%" or null, so LIKE receives a typed non-null
     * String and Hibernate always infers VARCHAR correctly.
     *
     * @param searchPattern pre-built LIKE pattern e.g. "%naruto%" or null
     * @param genresPattern pre-built LIKE pattern e.g. "%action%" or null
     */
    @Query("SELECT e FROM Details e WHERE " +
            "(:searchPattern IS NULL OR LOWER(e.title) LIKE :searchPattern) AND " +
            "(:type IS NULL OR e.type = :type) AND " +
            "(:year IS NULL OR e.year = :year) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:rating IS NULL OR e.rating = :rating) AND " +
            "(:source IS NULL OR e.source = :source) AND " +
            "(:genresPattern IS NULL OR LOWER(e.genres) LIKE :genresPattern) AND " +
            "(:episodes IS NULL OR e.episodes = :episodes)")
    Page<Details> findWithCombinedFilters(
            @Param("searchPattern") String searchPattern,
            @Param("type") String type,
            @Param("year") Integer year,
            @Param("status") String status,
            @Param("rating") String rating,
            @Param("source") String source,
            @Param("genresPattern") String genresPattern,
            @Param("episodes") Integer episodes,
            Pageable pageable);

    /**
     * Search by title — case-insensitive partial match.
     * Accepts pre-built wildcard pattern from service layer.
     */
    @Query("SELECT e FROM Details e WHERE LOWER(e.title) LIKE :searchPattern")
    Page<Details> searchByTitle(@Param("searchPattern") String searchPattern, Pageable pageable);

    /** Find by exact type (e.g. "TV", "Movie") */
    Page<Details> findByType(String type, Pageable pageable);

    /** Find by exact broadcast year */
    Page<Details> findByYear(Integer year, Pageable pageable);

    /** Find by exact airing status */
    Page<Details> findByStatus(String status, Pageable pageable);

    /** Find by exact age rating */
    Page<Details> findByRating(String rating, Pageable pageable);

    /** Find by exact source material */
    Page<Details> findBySource(String source, Pageable pageable);

    /** Find by exact episode count */
    Page<Details> findByEpisodes(Integer episodes, Pageable pageable);

    /**
     * Find by genres — case-insensitive partial match.
     * Accepts pre-built wildcard pattern from service layer.
     */
    @Query("SELECT e FROM Details e WHERE LOWER(e.genres) LIKE :genrePattern")
    Page<Details> findByGenresContaining(@Param("genrePattern") String genrePattern, Pageable pageable);

    /**
     * Find recommendations for a specific anime.
     * Subquery across the Recommendations entity.
     */
    @Query("SELECT d FROM Details d WHERE d.malId IN " +
            "(SELECT r.recommendationMalId FROM " +
            "com.example.dataserverspringboot.entities.recommendations.Recommendations r " +
            "WHERE r.malId = :malId) " +
            "ORDER BY d.score DESC")
    Page<Details> findRecommendationsForAnime(@Param("malId") Integer malId, Pageable pageable);

    /**
     * Find all characters that appear in this anime.
     * JOIN through CharacterAnimeWorks → Characters.
     */
    @Query("SELECT c FROM com.example.dataserverspringboot.entities.characters.Characters c " +
            "WHERE c.characterMalId IN " +
            "(SELECT caw.characterMalId FROM " +
            "com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
            "WHERE caw.animeMalId = :malId) " +
            "ORDER BY c.favorites DESC NULLS LAST")
    Page<com.example.dataserverspringboot.entities.characters.Characters>
            findCharactersInAnime(@Param("malId") Integer malId, Pageable pageable);

    /** Count distinct characters in this anime */
    @Query("SELECT COUNT(DISTINCT caw.characterMalId) FROM " +
            "com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
            "WHERE caw.animeMalId = :malId")
    long countCharactersInAnime(@Param("malId") Integer malId);

    /** Count recommendations for a specific anime */
    @Query("SELECT COUNT(r) FROM " +
            "com.example.dataserverspringboot.entities.recommendations.Recommendations r " +
            "WHERE r.malId = :malId")
    long countRecommendationsForAnime(@Param("malId") Integer malId);

    // ── NULL / NOT NULL filtering ─────────────────────────────────────────────

    Page<Details> findBySynopsisIsNull(Pageable pageable);
    Page<Details> findBySynopsisIsNotNull(Pageable pageable);

    Page<Details> findByScoreIsNull(Pageable pageable);
    Page<Details> findByScoreIsNotNull(Pageable pageable);

    Page<Details> findByEndDateIsNull(Pageable pageable);
    Page<Details> findByEndDateIsNotNull(Pageable pageable);

    Page<Details> findByTitleJapaneseIsNull(Pageable pageable);
    Page<Details> findByTitleJapaneseIsNotNull(Pageable pageable);

    Page<Details> findBySeasonIsNull(Pageable pageable);
    Page<Details> findBySeasonIsNotNull(Pageable pageable);

    Page<Details> findByFavoritesIsNull(Pageable pageable);
    Page<Details> findByFavoritesIsNotNull(Pageable pageable);

    // ── NULL statistics ───────────────────────────────────────────────────────

    long countBySynopsisIsNull();
    long countByScoreIsNull();
    long countByEndDateIsNull();
    long countByTitleJapaneseIsNull();
    long countBySeasonIsNull();
    long countByFavoritesIsNull();
}