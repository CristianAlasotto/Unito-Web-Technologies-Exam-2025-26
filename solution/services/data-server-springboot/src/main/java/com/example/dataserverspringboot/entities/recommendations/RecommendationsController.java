package com.example.dataserverspringboot.entities.recommendations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API controller for the {@link Recommendations} module.
 *
 * <p>Exposes four endpoints under {@code /api/recommendations}:</p>
 * <ul>
 *   <li>{@code GET /api/recommendations} — paginated list of recommendation ID pairs.</li>
 *   <li>{@code GET /api/recommendations/stats} — total record count.</li>
 *   <li>{@code GET /api/recommendations/single} — single record by composite key.</li>
 *   <li>{@code GET /api/recommendations/details} — enriched list including anime details.</li>
 * </ul>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Only {@link RecommendationsService} is injected — no repository access.</li>
 *   <li>{@link RecommendationsDTO} objects are returned directly to
 *       {@link ResponseEntity}; Spring (Jackson) serialises them to JSON
 *       automatically using the {@code @JsonProperty} annotations on the DTO
 *       getters for snake_case field names.</li>
 *   <li>Every method returns {@link ResponseEntity} to allow full control
 *       over HTTP status codes, as shown in the professor's slides.</li>
 * </ul>
 */
@Tag(name = "Recommendations", description = "Anime recommendations API")
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationsController {

    @Autowired
    private RecommendationsService service;

    /**
     * Returns a paginated list of recommendation ID pairs.
     *
     * <p>Supports three pagination modes (page-based takes priority):</p>
     * <ul>
     *   <li>{@code page} + {@code pageSize} — page-based pagination.</li>
     *   <li>{@code limit} + {@code offset} — limit/offset pagination.</li>
     *   <li>No parameters — returns the first 10 records.</li>
     * </ul>
     *
     * <p>Optional filters:</p>
     * <ul>
     *   <li>{@code search} — parsed to {@link Integer} and matched against
     *       {@code mal_id}. Non-integer strings return an empty list.</li>
     *   <li>{@code mal_id} — exact match on source anime ID.</li>
     *   <li>{@code recommendation_mal_id} — exact match on recommended anime ID.</li>
     * </ul>
     *
     * <p>The {@code sort} parameter accepts a comma-separated list of field names.
     * Prefix a field name with {@code -} for descending order. {@code NULL} values
     * are always sorted last.</p>
     *
     * @param search              string search on {@code mal_id} (integer match)
     * @param sort                sort expression, e.g. {@code -mal_id}
     * @param malId               exact source anime ID filter
     * @param recommendationMalId exact recommended anime ID filter
     * @param limit               maximum number of results (limit/offset mode)
     * @param offset              number of records to skip (limit/offset mode)
     * @param page                1-indexed page number (page-based mode)
     * @param pageSize            records per page (page-based mode)
     * @return {@link ResponseEntity} with a paginated body of {@link RecommendationsDTO}
     */
    @Operation(summary = "Get all recommendations",
            description = "Retrieve paginated list of recommendation ID pairs (Source -> Recommended).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search by source anime ID (integer match)", example = "1")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "mal_id")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by source anime ID", example = "1")
            @RequestParam(value = "mal_id", required = false) Integer malId,

            @Parameter(description = "Filter by recommended anime ID", example = "5")
            @RequestParam(value = "recommendation_mal_id", required = false) Integer recommendationMalId,

            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(required = false) Integer limit,

            @Parameter(description = "Offset for pagination", example = "0")
            @RequestParam(required = false) Integer offset,

            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {

        boolean usePageBased   = (page != null || pageSize != null);
        boolean useLimitOffset = (limit != null || offset != null) && !usePageBased;

        Sort sortObj = parseSortParameter(sort);

        if (useLimitOffset) {
            int finalLimit  = (limit  != null) ? limit  : 10;
            int finalOffset = (offset != null) ? offset : 0;

            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit, sortObj);
            Page<RecommendationsDTO> pageResult =
                    service.findWithFilters(search, malId, recommendationMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  pageResult.getTotalElements(),
                    "items",  pageResult.getContent()));

        } else if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<RecommendationsDTO> pageResult =
                    service.findWithFilters(search, malId, recommendationMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<RecommendationsDTO> pageResult =
                    service.findWithFilters(search, malId, recommendationMalId, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    /**
     * Returns the total number of recommendation records in the database.
     *
     * @return {@link ResponseEntity} with body {@code {"total": N}}
     */
    @Operation(summary = "Get statistics", description = "Total count of recommendation records")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    /**
     * Returns a single recommendation looked up by its composite key.
     *
     * <p>Both {@code mal_id} and {@code recommendation_mal_id} are required.
     * If either is missing, {@code 400 Bad Request} is returned. If the composite
     * key does not exist in the database, {@code 404 Not Found} is returned.</p>
     *
     * @param malId               source anime MAL ID (required)
     * @param recommendationMalId recommended anime MAL ID (required)
     * @return {@link ResponseEntity} containing the {@link RecommendationsDTO},
     *         or an error body with status 400 or 404
     */
    @Operation(summary = "Get specific recommendation",
            description = "Retrieve a single recommendation using the composite key (mal_id + recommendation_mal_id)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found successfully",
                    content = @Content(schema = @Schema(implementation = RecommendationsDTO.class))),
            @ApiResponse(responseCode = "400", description = "Missing key fields",  content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",           content = @Content)
    })
    @GetMapping("/single")
    public ResponseEntity<?> getSingle(
            @Parameter(description = "Source anime MAL ID", required = true)
            @RequestParam(value = "mal_id", required = false) Integer malId,

            @Parameter(description = "Recommended anime MAL ID", required = true)
            @RequestParam(value = "recommendation_mal_id", required = false) Integer recommendationMalId) {

        if (malId == null || recommendationMalId == null) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "All key fields required: mal_id, recommendation_mal_id",
                    "usage", "GET /api/recommendations/single?mal_id=1&recommendation_mal_id=5"));
        }

        Recommendations.RecommendationsId id =
                new Recommendations.RecommendationsId(malId, recommendationMalId);

        Optional<RecommendationsDTO> dto = service.getById(id);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",                 "Recommendation not found",
                    "mal_id",                malId,
                    "recommendation_mal_id", recommendationMalId));
        }

        return ResponseEntity.ok(dto.get());
    }

    /**
     * Returns a paginated list of enriched recommendations, each containing
     * key fields from both the source and the recommended anime.
     *
     * <p>Supports the same two pagination modes as {@link #getAll} (page-based
     * takes priority over limit/offset). The {@code sort} parameter is not
     * supported on this endpoint — results are ordered by the database default.</p>
     *
     * <p>Optional filters: {@code mal_id} and {@code recommendation_mal_id}.</p>
     *
     * @param malId               source anime ID filter, or {@code null} to match all
     * @param recommendationMalId recommended anime ID filter, or {@code null} to match all
     * @param limit               maximum number of results (limit/offset mode)
     * @param offset              number of records to skip (limit/offset mode)
     * @param page                1-indexed page number (page-based mode)
     * @param pageSize            records per page (page-based mode)
     * @return {@link ResponseEntity} with a paginated body of enriched
     *         {@link RecommendationsDTO}
     */
    @Operation(summary = "Get recommendations with full anime details",
            description = "Retrieve recommendations including key fields from both source and recommended anime.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "List retrieved successfully",
            content = @Content(schema = @Schema(implementation = RecommendationsDTO.class))))
    @GetMapping("/details")
    public ResponseEntity<?> getAllWithDetails(
            @Parameter(description = "Filter by source anime ID", example = "1")
            @RequestParam(value = "mal_id", required = false) Integer malId,

            @Parameter(description = "Filter by recommended anime ID", example = "5")
            @RequestParam(value = "recommendation_mal_id", required = false) Integer recommendationMalId,

            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(required = false) Integer limit,

            @Parameter(description = "Offset for pagination", example = "0")
            @RequestParam(required = false) Integer offset,

            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {

        boolean usePageBased   = (page != null || pageSize != null);
        boolean useLimitOffset = (limit != null || offset != null) && !usePageBased;

        if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);
            Page<RecommendationsDTO> results =
                    service.findAllWithDetails(malId, recommendationMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", results.getTotalPages(),
                    "total",      results.getTotalElements(),
                    "items",      results.getContent()));

        } else if (useLimitOffset) {
            int finalLimit  = (limit  != null) ? limit  : 10;
            int finalOffset = (offset != null) ? offset : 0;

            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit);
            Page<RecommendationsDTO> results =
                    service.findAllWithDetails(malId, recommendationMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  results.getTotalElements(),
                    "items",  results.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10);
            Page<RecommendationsDTO> results =
                    service.findAllWithDetails(malId, recommendationMalId, pageable);

            return ResponseEntity.ok(results.getContent());
        }
    }

    /**
     * Converts the {@code sort} query parameter string into a Spring
     * {@link Sort} object.
     *
     * <p>Each token in the comma-separated {@code sort} string is interpreted as
     * a field name. A leading {@code -} indicates descending order; no prefix
     * means ascending. {@code NULL} values are always placed last via
     * {@link Sort.Order#nullsLast()}.</p>
     *
     * <p>A stable tiebreaker on {@code malId} and {@code recommendationMalId}
     * is always appended so that results are deterministic across pages.</p>
     *
     * @param sort comma-separated sort expression, e.g. {@code "mal_id,-recommendation_mal_id"}
     * @return a {@link Sort} object ready to pass to {@link PageRequest}
     */
    private Sort parseSortParameter(String sort) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null && !sort.isEmpty()) {
            for (String field : sort.split(",")) {
                field = field.trim();
                if (field.startsWith("-")) {
                    orders.add(Sort.Order.by(field.substring(1))
                            .with(Sort.Direction.DESC).nullsLast());
                } else {
                    orders.add(Sort.Order.by(field)
                            .with(Sort.Direction.ASC).nullsLast());
                }
            }
        }
        orders.add(Sort.Order.asc("malId"));
        orders.add(Sort.Order.asc("recommendationMalId"));
        return Sort.by(orders);
    }
}
