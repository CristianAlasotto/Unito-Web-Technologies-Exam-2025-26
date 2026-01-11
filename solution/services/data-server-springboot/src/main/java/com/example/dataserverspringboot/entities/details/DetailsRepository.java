package com.example.dataserverspringboot.entities.details;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Details entity
 * Provides database operations for the details table
 */
@Repository
public interface DetailsRepository extends JpaRepository<Details, Integer> {

    // Basic methods are already provided by JpaRepository:
    // - findAll() - get all anime
    // - findById(id) - get one anime
    // - count() - count total anime
    // - save() - insert or update
    // - deleteById(id) - delete
    // - existsById(id) - check if exists

    /**
     * Find anime by title (case-insensitive, partial match)
     * Used by: /api/anime/search?title=xxx
     */
    List<Details> findByTitleContainingIgnoreCase(String title);

    /**
     * Find anime by type (TV, Movie, OVA, etc.)
     * Used by: /api/anime/type/{type}
     */
    List<Details> findByType(String type);

    /**
     * Find top 10 anime by score (descending order)
     * Used by: /api/anime/top-rated
     */
    List<Details> findTop10ByOrderByScoreDesc();

    /**
     * Find anime by genre (custom query)
     * Used by: /api/anime/genre/{genre}
     */
    @Query("SELECT d FROM Details d WHERE LOWER(d.genres) LIKE LOWER(CONCAT('%', :genre, '%'))")
    List<Details> findByGenreContaining(@Param("genre") String genre);

    /**
     * Find anime by score range (native SQL query)
     * Used by: /api/anime/score?minScore=8&maxScore=10
     */
    @Query(value = "SELECT * FROM details WHERE score BETWEEN :minScore AND :maxScore ORDER BY score DESC",
            nativeQuery = true)
    List<Details> findByScoreRange(@Param("minScore") Double minScore,
                                   @Param("maxScore") Double maxScore);
}