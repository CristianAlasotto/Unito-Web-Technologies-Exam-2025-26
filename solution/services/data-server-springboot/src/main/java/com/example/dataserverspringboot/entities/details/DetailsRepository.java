package com.example.dataserverspringboot.entities.details;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailsRepository extends JpaRepository<Details, Integer> {

    // ============================================
    // Filtering Methods
    // ============================================

    /**
     * Find by type (TV, Movie, OVA, etc.)
     */
    Page<Details> findByType(String type, Pageable pageable);

    /**
     * Find by year
     */
    Page<Details> findByYear(Integer year, Pageable pageable);

    /**
     * Find by status (Finished Airing, Currently Airing, etc.)
     */
    Page<Details> findByStatus(String status, Pageable pageable);

    /**
     * Find by rating (G, PG, PG-13, R, etc.)
     */
    Page<Details> findByRating(String rating, Pageable pageable);

    /**
     * Find by source (Manga, Original, Light novel, etc.)
     */
    Page<Details> findBySource(String source, Pageable pageable);

    /**
     * Combined filter: type and year
     */
    Page<Details> findByTypeAndYear(String type, Integer year, Pageable pageable);

    // ============================================
    // Search Methods
    // ============================================

    /**
     * Search by title (case-insensitive, partial match)
     */
    @Query("SELECT d FROM Details d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Details> searchByTitle(@Param("search") String search, Pageable pageable);

    /**
     * Search in title and title_japanese
     */
    @Query("SELECT d FROM Details d WHERE " +
            "LOWER(d.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.title_japanese) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Details> searchInAllTitles(@Param("search") String search, Pageable pageable);

    // ============================================
    // Sorting/Filtering Combinations
    // ============================================

    /**
     * Find by genre (searches in genres field)
     */
    @Query("SELECT d FROM Details d WHERE LOWER(d.genres) LIKE LOWER(CONCAT('%', :genre, '%'))")
    Page<Details> findByGenreContaining(@Param("genre") String genre, Pageable pageable);

    /**
     * Find by theme
     */
    @Query("SELECT d FROM Details d WHERE LOWER(d.themes) LIKE LOWER(CONCAT('%', :theme, '%'))")
    Page<Details> findByThemeContaining(@Param("theme") String theme, Pageable pageable);

    /**
     * Find by score range
     */
    @Query("SELECT d FROM Details d WHERE d.score BETWEEN :minScore AND :maxScore")
    Page<Details> findByScoreBetween(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore, Pageable pageable);

    /**
     * Find top rated (score >= threshold)
     */
    @Query("SELECT d FROM Details d WHERE d.score >= :minScore ORDER BY d.score DESC")
    Page<Details> findTopRated(@Param("minScore") Double minScore, Pageable pageable);
}