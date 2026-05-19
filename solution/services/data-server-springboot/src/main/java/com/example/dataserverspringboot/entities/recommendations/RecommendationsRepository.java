package com.example.dataserverspringboot.entities.recommendations;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Hidden
@Repository
public interface RecommendationsRepository
        extends JpaRepository<Recommendations, Recommendations.RecommendationsId> {

    /**
     * Find by source anime ID.
     * Spring Data derives: SELECT * FROM recommendations WHERE mal_id = ?
     *
     * Also used by the "search" feature: the service parses the search
     * string to an Integer and calls this method. If the string is not a
     * valid integer, the service returns an empty page without calling this.
     */
    Page<Recommendations> findByMalId(Integer malId, Pageable pageable);

    /**
     * Find by recommended anime ID.
     * Spring Data derives: SELECT * FROM recommendations WHERE recommendation_mal_id = ?
     */
    Page<Recommendations> findByRecommendationMalId(
            Integer recommendationMalId, Pageable pageable);

    /**
     * Find by both IDs simultaneously.
     * Spring Data derives the AND query automatically.
     */
    Page<Recommendations> findByMalIdAndRecommendationMalId(
            Integer malId, Integer recommendationMalId, Pageable pageable);

    /**
     * Count with optional filters — used by the stats endpoint.
     * Uses :param IS NULL OR condition so both parameters are optional.
     */
    @Query("SELECT COUNT(r) FROM Recommendations r " +
            "WHERE (:malId IS NULL OR r.malId = :malId) " +
            "AND (:recommendationMalId IS NULL OR r.recommendationMalId = :recommendationMalId)")
    long countWithFilters(
            @Param("malId") Integer malId,
            @Param("recommendationMalId") Integer recommendationMalId);
}