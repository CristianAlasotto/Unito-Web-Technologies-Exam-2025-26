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

/**
 * Service layer for the {@link Recommendations} module.
 *
 * <p>Contains all business logic for querying recommendation records.
 * All public methods return {@link RecommendationsDTO} or
 * {@code Page<RecommendationsDTO>} — the raw {@link Recommendations} entity
 * never leaves this layer.</p>
 *
 * <p>Two repositories are injected:</p>
 * <ul>
 *   <li>{@link RecommendationsRepository} — for all recommendation queries.</li>
 *   <li>{@link DetailsRepository} — used only inside {@link #findAllWithDetails}
 *       to fetch the {@link Details} entity for each anime; the entity is
 *       consumed immediately inside
 *       {@link RecommendationsDTO#fromEntityWithDetails} and never returned.</li>
 * </ul>
 */
@Hidden
@Service
public class RecommendationsService {

    @Autowired
    private RecommendationsRepository repository;

    @Autowired
    private DetailsRepository detailsRepository;

    /**
     * Fetches a single recommendation by its composite key.
     *
     * <p>Calls {@link RecommendationsRepository#findById} and maps the result
     * to a {@link RecommendationsDTO} via {@link RecommendationsDTO#fromEntity}.
     * Returns an empty {@link Optional} if no record matches the key.</p>
     *
     * @param id composite key ({@code malId} + {@code recommendationMalId})
     * @return {@link Optional} containing the {@link RecommendationsDTO} if found,
     *         empty otherwise
     */
    public Optional<RecommendationsDTO> getById(Recommendations.RecommendationsId id) {
        return repository.findById(id)
                         .map(RecommendationsDTO::fromEntity);
    }

    /**
     * Returns the total number of recommendation records in the database.
     *
     * @return total record count
     */
    public long count() {
        return repository.count();
    }

    /**
     * Returns a paginated page of {@link RecommendationsDTO} matching the given filters.
     *
     * <p>Filter routing logic:</p>
     * <ol>
     *   <li>If {@code search} is provided, it is parsed to {@link Integer} and
     *       used as an exact {@code malId} filter via
     *       {@link RecommendationsRepository#findByMalId}. If the string is not a
     *       valid integer, {@link Page#empty(Pageable)} is returned immediately —
     *       IDs are integers and a non-numeric string can never match.</li>
     *   <li>If both {@code malId} and {@code recommendationMalId} are non-null,
     *       {@link RecommendationsRepository#findByMalIdAndRecommendationMalId}
     *       is called.</li>
     *   <li>If only {@code malId} is non-null,
     *       {@link RecommendationsRepository#findByMalId} is called.</li>
     *   <li>If only {@code recommendationMalId} is non-null,
     *       {@link RecommendationsRepository#findByRecommendationMalId} is called.</li>
     *   <li>If no filters are active, {@code findAll(pageable)} is called.</li>
     * </ol>
     *
     * <p>Every repository call is followed by {@code .map(RecommendationsDTO::fromEntity)}
     * so the raw entity never reaches the controller.</p>
     *
     * @param search              string parsed to {@link Integer} for {@code malId} search;
     *                            non-integer strings return an empty page immediately
     * @param malId               exact source anime ID filter, or {@code null}
     * @param recommendationMalId exact recommended anime ID filter, or {@code null}
     * @param pageable            pagination and sorting parameters
     * @return paginated page of {@link RecommendationsDTO} matching all active filters
     */
    public Page<RecommendationsDTO> findWithFilters(
            String search, Integer malId, Integer recommendationMalId, Pageable pageable) {

        if (search != null && !search.isBlank()) {
            try {
                Integer searchId = Integer.parseInt(search.trim());
                return repository.findByMalId(searchId, pageable)
                        .map(RecommendationsDTO::fromEntity);
            } catch (NumberFormatException e) {
                return Page.empty(pageable);
            }
        }

        if (malId != null && recommendationMalId != null) {
            return repository.findByMalIdAndRecommendationMalId(
                    malId, recommendationMalId, pageable)
                    .map(RecommendationsDTO::fromEntity);
        }

        if (malId != null) {
            return repository.findByMalId(malId, pageable)
                    .map(RecommendationsDTO::fromEntity);
        }

        if (recommendationMalId != null) {
            return repository.findByRecommendationMalId(recommendationMalId, pageable)
                    .map(RecommendationsDTO::fromEntity);
        }

        return repository.findAll(pageable)
                .map(RecommendationsDTO::fromEntity);
    }

    /**
     * Returns a paginated page of enriched {@link RecommendationsDTO} where each
     * record also contains key fields from both the source and the recommended
     * {@link Details} entities.
     *
     * <p>Processing steps:</p>
     * <ol>
     *   <li>The appropriate repository method is called based on which filters
     *       are active, using the same routing logic as
     *       {@link #findWithFilters(String, Integer, Integer, Pageable)}
     *       (without the string search branch).</li>
     *   <li>For each {@link Recommendations} row in the result page,
     *       {@link DetailsRepository#findById} is called twice — once for the
     *       source anime and once for the recommended anime.</li>
     *   <li>{@link RecommendationsDTO#fromEntityWithDetails} consumes the two
     *       {@link Details} entities and extracts only the needed fields. The raw
     *       entities never leave this method.</li>
     *   <li>The resulting {@link List} is wrapped in a {@link PageImpl} to
     *       preserve the total element count from the original paginated query.</li>
     * </ol>
     *
     * @param malId               source anime ID filter, or {@code null} to match all
     * @param recommendationMalId recommended anime ID filter, or {@code null} to match all
     * @param pageable            pagination parameters
     * @return paginated page of enriched {@link RecommendationsDTO}
     */
    public Page<RecommendationsDTO> findAllWithDetails(
            Integer malId, Integer recommendationMalId, Pageable pageable) {

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

        List<RecommendationsDTO> results = recommendations.getContent().stream()
                .map(rec -> {
                    Optional<Details> sourceAnime =
                            detailsRepository.findById(rec.getMalId());
                    Optional<Details> recommendedAnime =
                            detailsRepository.findById(rec.getRecommendationMalId());
                    return RecommendationsDTO.fromEntityWithDetails(
                            rec,
                            sourceAnime.orElse(null),
                            recommendedAnime.orElse(null));
                })
                .collect(Collectors.toList());

        return new PageImpl<>(results, pageable, recommendations.getTotalElements());
    }
}
