package com.example.dataserverspringboot.entities.recommendations;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Recommendations} entity.
 *
 * <p>Extends {@link JpaRepository} with composite key type
 * {@link Recommendations.RecommendationsId}. Spring Data provides
 * all standard CRUD operations automatically.</p>
 *
 * <p>Custom query methods use two mechanisms:</p>
 * <ul>
 *   <li><b>Derived methods</b> — Spring Data derives the SQL from the method name
 *       (e.g. {@code findByMalId} → {@code WHERE mal_id = ?}).</li>
 *   <li><b>@Query JPQL</b> — used for {@link #countWithFilters} where both
 *       parameters are optional. The {@code :param IS NULL OR condition} pattern
 *       makes each filter optional without branching.</li>
 * </ul>
 */
@Hidden
@Repository
public interface RecommendationsRepository
        extends JpaRepository<Recommendations, Recommendations.RecommendationsId> {

    /**
     * Returns all recommendations where the source anime matches the given ID.
     *
     * <p>Spring Data derives: {@code SELECT * FROM recommendations WHERE mal_id = ?}.</p>
     *
     * <p>Also used by the search feature in {@link RecommendationsService}: the
     * service parses the search string to an {@link Integer} and calls this method.
     * If the string cannot be parsed, the service returns an empty page immediately
     * without calling this method.</p>
     *
     * @param malId    source anime MAL ID
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching recommendations
     */
    Page<Recommendations> findByMalId(Integer malId, Pageable pageable);

    /**
     * Returns all recommendations where the recommended anime matches the given ID.
     *
     * <p>Spring Data derives:
     * {@code SELECT * FROM recommendations WHERE recommendation_mal_id = ?}.</p>
     *
     * @param recommendationMalId recommended anime MAL ID
     * @param pageable            pagination and sorting parameters
     * @return paginated page of matching recommendations
     */
    Page<Recommendations> findByRecommendationMalId(
            Integer recommendationMalId, Pageable pageable);

    /**
     * Returns all recommendations matching both IDs simultaneously.
     *
     * <p>Spring Data derives the AND query automatically from the method name.</p>
     *
     * @param malId               source anime MAL ID
     * @param recommendationMalId recommended anime MAL ID
     * @param pageable            pagination and sorting parameters
     * @return paginated page of matching recommendations
     */
    Page<Recommendations> findByMalIdAndRecommendationMalId(
            Integer malId, Integer recommendationMalId, Pageable pageable);

    /**
     * Counts recommendations with optional filters.
     *
     * <p>Uses JPQL with the {@code :param IS NULL OR condition} pattern so that
     * both parameters are optional. When a parameter is {@code null},
     * {@code NULL IS NULL} evaluates to {@code TRUE} and the right side of
     * {@code OR} is never evaluated, effectively removing that filter.</p>
     *
     * @param malId               source anime MAL ID filter, or {@code null} to match all
     * @param recommendationMalId recommended anime MAL ID filter, or {@code null} to match all
     * @return count of recommendations matching all active filters
     */
    @Query("SELECT COUNT(r) FROM Recommendations r " +
            "WHERE (:malId IS NULL OR r.malId = :malId) " +
            "AND (:recommendationMalId IS NULL OR r.recommendationMalId = :recommendationMalId)")
    long countWithFilters(
            @Param("malId") Integer malId,
            @Param("recommendationMalId") Integer recommendationMalId);
}
