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
     * Search by title (case-insensitive, partial match)
     */
    @Query("SELECT e FROM Details e WHERE LOWER(CAST(e.title AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Details> searchByTitle(@Param("search") String search, Pageable pageable);

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
     * Find recommendations for a specific anime using subquery
     */
    @Query("SELECT d FROM Details d WHERE d.malId IN " +
           "(SELECT r.recommendationMalId FROM com.example.dataserverspringboot.entities.recommendations.Recommendations r WHERE r.malId = :malId) " +
           "ORDER BY d.score DESC")
    Page<Details> findRecommendationsForAnime(@Param("malId") Integer malId, Pageable pageable);

    /**
     * Count recommendations for a specific anime
     */
    @Query("SELECT COUNT(r) FROM com.example.dataserverspringboot.entities.recommendations.Recommendations r WHERE r.malId = :malId")
    long countRecommendationsForAnime(@Param("malId") Integer malId);

    // ============================================================
    // NULL FILTERING METHODS
    // ============================================================

    /**
     * Find all anime where synopsis IS NULL
     */
    Page<Details> findBySynopsisIsNull(Pageable pageable);

    /**
     * Find all anime where synopsis IS NOT NULL
     */
    Page<Details> findBySynopsisIsNotNull(Pageable pageable);

    /**
     * Find all anime where score IS NULL (unrated)
     */
    Page<Details> findByScoreIsNull(Pageable pageable);

    /**
     * Find all anime where score IS NOT NULL (rated)
     */
    Page<Details> findByScoreIsNotNull(Pageable pageable);

    /**
     * Find all anime where end_date IS NULL (currently airing or unknown)
     */
    Page<Details> findByEndDateIsNull(Pageable pageable);

    /**
     * Find all anime where end_date IS NOT NULL (finished airing)
     */
    Page<Details> findByEndDateIsNotNull(Pageable pageable);

    /**
     * Find all anime where title_japanese IS NULL
     */
    Page<Details> findByTitleJapaneseIsNull(Pageable pageable);

    /**
     * Find all anime where title_japanese IS NOT NULL
     */
    Page<Details> findByTitleJapaneseIsNotNull(Pageable pageable);

    /**
     * Find all anime where season IS NULL (movies, OVAs, etc.)
     */
    Page<Details> findBySeasonIsNull(Pageable pageable);

    /**
     * Find all anime where season IS NOT NULL (seasonal anime)
     */
    Page<Details> findBySeasonIsNotNull(Pageable pageable);

    // Count methods for statistics

    /**
     * Find all characters where favorites IS NULL
     */
    Page<Details> findByFavoritesIsNull(Pageable pageable);

    /**
     * Find all characters where favorites IS NOT NULL
     */
    Page<Details> findByFavoritesIsNotNull(Pageable pageable);

    /**
     * Count characters with null favorites
     */
    long countByFavoritesIsNull();

    /**
     * Count anime with null synopsis
     */
    long countBySynopsisIsNull();

    /**
     * Count anime with null score
     */
    long countByScoreIsNull();

    /**
     * Count anime with null end_date
     */
    long countByEndDateIsNull();

    /**
     * Count anime with null title_japanese
     */
    long countByTitleJapaneseIsNull();

    /**
     * Count anime with null season
     */
    long countBySeasonIsNull();
}
