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
     * FIX: Back to pure JPQL (no nativeQuery) to avoid the "column e.malid does
     * not exist" pagination bug that native queries cause with camelCase field names.
     *
     * The "lower(bytea)" fix is handled upstream: DetailsService pre-builds the
     * wildcard strings (e.g. "%attack%") before calling this method, so we use a
     * plain LIKE instead of LOWER(CONCAT(...)). Hibernate can always infer the type
     * of a non-null String parameter correctly, so the bytea error never occurs.
     *
     * Parameters:
     *   searchPattern - already wrapped with %, e.g. "%naruto%", or null to skip
     *   genresPattern - already wrapped with %, e.g. "%action%", or null to skip
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
     * Search by title (case-insensitive, partial match).
     * FIX: Accepts pre-built wildcard pattern from service layer.
     */
    @Query("SELECT e FROM Details e WHERE LOWER(e.title) LIKE :searchPattern")
    Page<Details> searchByTitle(@Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Find by type
     */
    Page<Details> findByType(String type, Pageable pageable);

    /**
     * Find by year
     */
    Page<Details> findByYear(Integer year, Pageable pageable);

    /**
     * Find by status
     */
    Page<Details> findByStatus(String status, Pageable pageable);

    /**
     * Find by rating
     */
    Page<Details> findByRating(String rating, Pageable pageable);

    /**
     * Find by source
     */
    Page<Details> findBySource(String source, Pageable pageable);

    /**
     * Find by episodes (exact match)
     */
    Page<Details> findByEpisodes(Integer episodes, Pageable pageable);

    /**
     * Find by genres (case-insensitive, partial match).
     * FIX: Accepts pre-built wildcard pattern from service layer.
     */
    @Query("SELECT e FROM Details e WHERE LOWER(e.genres) LIKE :genrePattern")
    Page<Details> findByGenresContaining(@Param("genrePattern") String genrePattern, Pageable pageable);

    /**
     * Find recommendations for a specific anime using subquery
     */
    @Query("SELECT d FROM Details d WHERE d.malId IN " +
            "(SELECT r.recommendationMalId FROM com.example.dataserverspringboot.entities.recommendations.Recommendations r WHERE r.malId = :malId) " +
            "ORDER BY d.score DESC")
    Page<Details> findRecommendationsForAnime(@Param("malId") Integer malId, Pageable pageable);

    /**
     * JOIN: Find all characters that appear in this anime
     * details → character_anime_works → characters
     */
    @Query("SELECT c FROM com.example.dataserverspringboot.entities.characters.Characters c WHERE c.characterMalId IN " +
            "(SELECT caw.characterMalId FROM com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
            "WHERE caw.animeMalId = :malId) " +
            "ORDER BY c.favorites DESC NULLS LAST")
    Page<com.example.dataserverspringboot.entities.characters.Characters> findCharactersInAnime(@Param("malId") Integer malId, Pageable pageable);

    /**
     * COUNT: Total characters in this anime
     */
    @Query("SELECT COUNT(DISTINCT caw.characterMalId) FROM com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
            "WHERE caw.animeMalId = :malId")
    long countCharactersInAnime(@Param("malId") Integer malId);

    /**
     * Count recommendations for a specific anime
     */
    @Query("SELECT COUNT(r) FROM com.example.dataserverspringboot.entities.recommendations.Recommendations r WHERE r.malId = :malId")
    long countRecommendationsForAnime(@Param("malId") Integer malId);

    // ============================================================
    // NULL FILTERING METHODS
    // ============================================================

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

    long countByFavoritesIsNull();
    long countBySynopsisIsNull();
    long countByScoreIsNull();
    long countByEndDateIsNull();
    long countByTitleJapaneseIsNull();
    long countBySeasonIsNull();
}