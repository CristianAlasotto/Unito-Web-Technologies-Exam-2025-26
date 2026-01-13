package com.example.dataserverspringboot.entities.details;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Details entity
 * Now with Pageable support to limit results
 */
@Repository
public interface DetailsRepository extends JpaRepository<Details, Integer> {

    // Basic methods provided by JpaRepository:
    // - findAll() - get all records
    // - findAll(Pageable) - get limited records with pagination
    // - findById(id) - get one anime
    // - count() - count total anime

    /**
     * Find anime by title (case-insensitive, partial match)
     * Limited by Pageable parameter
     */
    List<Details> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Find anime by type
     * Limited by Pageable parameter
     */
    List<Details> findByType(String type, Pageable pageable);

    /**
     * Find top 10 anime by score (descending order)
     * Already limited to 10 by method name
     */
    List<Details> findTop10ByOrderByScoreDesc();

    /**
     * Find anime by genre (custom query)
     * Limited by Pageable parameter
     */
    @Query("SELECT d FROM Details d WHERE LOWER(d.genres) LIKE LOWER(CONCAT('%', :genre, '%'))")
    List<Details> findByGenreContaining(@Param("genre") String genre, Pageable pageable);

    /**
     * Find anime by score range (native SQL query)
     * Limited by Pageable parameter
     */
    @Query(value = "SELECT * FROM details WHERE score BETWEEN :minScore AND :maxScore ORDER BY score DESC",
            nativeQuery = true)
    List<Details> findByScoreRange(@Param("minScore") Double minScore,
                                   @Param("maxScore") Double maxScore,
                                   Pageable pageable);
}