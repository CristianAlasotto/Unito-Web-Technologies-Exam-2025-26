package com.example.dataserverspringboot.entities.recommendations;

import com.example.dataserverspringboot.entities.details.Details;
import com.example.dataserverspringboot.entities.details.DetailsRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Hidden
@Service
public class RecommendationsService {

    @Autowired
    private RecommendationsRepository repository;

    @Autowired
    private DetailsRepository detailsRepository;

    /**
     * Fetch a single recommendation by its composite key.
     *
     * @param id composite key (malId + recommendationMalId)
     * @return Optional containing the RecommendationsDTO if found, empty otherwise
     */
    public Optional<RecommendationsDTO> getById(Recommendations.RecommendationsId id) {
        return repository.findById(id)
                         .map(RecommendationsDTO::fromEntity);
    }

    /**
     * Total number of recommendation records in the database.
     */
    public long count() {
        return repository.count();
    }

    /**
     * Find recommendations with optional filters, returning a paginated page of DTOs.
     *
     * HOW IT WORKS:
     *   Filters are routed to the most specific repository method available:
     *
     *   - search (string): parsed to Integer and used as malId equality match.
     *     If the string is not a valid integer, an empty page is returned
     *     immediately — IDs are integers and cannot match a non-numeric string.
     *
     *   - malId + recommendationMalId both present: findByMalIdAndRecommendationMalId
     *   - malId only:                               findByMalId
     *   - recommendationMalId only:                 findByRecommendationMalId
     *   - search only:                              findByMalId (after parsing)
     *   - no filters:                               findAll
     *
     *   The result Page<Recommendations> is mapped to Page<RecommendationsDTO>
     *   so the raw JPA entity never leaves the service layer.
     *
     * @param search              string parsed to Integer for malId search
     * @param malId               exact source anime ID filter
     * @param recommendationMalId exact recommended anime ID filter
     * @param pageable            pagination and sorting parameters
     * @return a Page of RecommendationsDTO matching all active filters
     */
    public Page<RecommendationsDTO> findWithFilters(
            String search, Integer malId, Integer recommendationMalId, Pageable pageable) {

        // If search string is provided, parse it to Integer and use as malId filter.
        // Non-integer strings can never match an integer ID column → return empty page.
        if (search != null && !search.isBlank()) {
            try {
                Integer searchId = Integer.parseInt(search.trim());
                return repository.findByMalId(searchId, pageable)
                        .map(RecommendationsDTO::fromEntity);
            } catch (NumberFormatException e) {
                // Non-integer search → no results possible
                return Page.empty(pageable);
            }
        }

        // Both filters active
        if (malId != null && recommendationMalId != null) {
            return repository.findByMalIdAndRecommendationMalId(
                    malId, recommendationMalId, pageable)
                    .map(RecommendationsDTO::fromEntity);
        }

        // Only malId
        if (malId != null) {
            return repository.findByMalId(malId, pageable)
                    .map(RecommendationsDTO::fromEntity);
        }

        // Only recommendationMalId
        if (recommendationMalId != null) {
            return repository.findByRecommendationMalId(recommendationMalId, pageable)
                    .map(RecommendationsDTO::fromEntity);
        }

        // No filters → return all
        return repository.findAll(pageable)
                .map(RecommendationsDTO::fromEntity);
    }

    /**
     * Find recommendations with full anime details for both source and
     * recommended anime.
     *
     * HOW IT WORKS:
     *   1. The appropriate repository method is called based on which filters
     *      are active (same routing logic as findWithFilters above).
     *   2. For each Recommendations row, detailsRepository.findById() fetches
     *      the Details entity for both malId and recommendationMalId.
     *   3. RecommendationsDTO.fromEntityWithDetails() extracts only the needed
     *      fields — the raw Details entity never leaves this method.
     *   4. The result is wrapped in a PageImpl to preserve pagination metadata.
     *
     * @param malId               filter by source anime ID (null = all)
     * @param recommendationMalId filter by recommended anime ID (null = all)
     * @param pageable            pagination parameters
     * @return a Page of enriched RecommendationsDTO with both anime details embedded
     */
    public Page<RecommendationsDTO> findAllWithDetails(
            Integer malId, Integer recommendationMalId, Pageable pageable) {

        // Fetch the recommendations page using the appropriate filter
        Page<Recommendations> recommendations;

        if (malId != null && recommendationMalId != null) {
            recommendations = repository.findByMalIdAndRecommendationMalId(
                    malId, recommendationMalId, pageable);
        } else if (malId != null) {
            recommendations = repository.findByMalId(malId, pageable);
        } else if (recommendationMalId != null) {
            recommendations = repository.findByRecommendationMalId(
                    recommendationMalId, pageable);
        } else {
            recommendations = repository.findAll(pageable);
        }

        // For each recommendation, fetch both anime's details and build the DTO
        List<RecommendationsDTO> results = recommendations.getContent().stream()
                .map(rec -> {
                    Optional<Details> sourceAnime =
                            detailsRepository.findById(rec.getMalId());
                    Optional<Details> recommendedAnime =
                            detailsRepository.findById(rec.getRecommendationMalId());

                    return RecommendationsDTO.fromEntityWithDetails(
                            rec,
                            sourceAnime.orElse(null),
                            recommendedAnime.orElse(null)
                    );
                })
                .collect(Collectors.toList());

        // Wrap in PageImpl to preserve total count from the original paginated query
        return new PageImpl<>(results, pageable, recommendations.getTotalElements());
    }
}