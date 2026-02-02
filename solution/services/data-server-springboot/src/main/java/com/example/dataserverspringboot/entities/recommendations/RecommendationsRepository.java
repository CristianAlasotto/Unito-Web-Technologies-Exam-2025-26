package com.example.dataserverspringboot.entities.recommendations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationsRepository extends JpaRepository<Recommendations, Recommendations.RecommendationsId> {

    /**
     * Search by mal_id (case-insensitive, partial match)
     */
    @Query("SELECT e FROM Recommendations e WHERE LOWER(CAST(e.malId AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Recommendations> searchByMalId(@Param("search") String search, Pageable pageable);

    /**
     * Find by mal_id
     */
    Page<Recommendations> findByMalId(Integer malId, Pageable pageable);

    /**
     * Find by recommendation_mal_id
     */
    Page<Recommendations> findByRecommendationMalId(Integer recommendationMalId, Pageable pageable);

    /**
     * Find by both mal_id and recommendation_mal_id
     */
    Page<Recommendations> findByMalIdAndRecommendationMalId(Integer malId, Integer recommendationMalId, Pageable pageable);

    /**
     * Count recommendations with filters
     */
    @Query("SELECT COUNT(r) FROM Recommendations r " +
           "WHERE (:malId IS NULL OR r.malId = :malId) " +
           "AND (:recommendationMalId IS NULL OR r.recommendationMalId = :recommendationMalId)")
    long countWithFilters(@Param("malId") Integer malId, 
                         @Param("recommendationMalId") Integer recommendationMalId);
}
