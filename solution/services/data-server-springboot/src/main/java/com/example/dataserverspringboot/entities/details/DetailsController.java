package com.example.dataserverspringboot.entities.details;

import com.example.dataserverspringboot.entities.characters.CharactersDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * REST API controller for the {@link Details} module.
 *
 * <p>Exposes six endpoints under {@code /api/details}:</p>
 * <ul>
 *   <li>{@code GET /api/details/{mal_id}} — single anime by MAL ID.</li>
 *   <li>{@code GET /api/details} — paginated list with optional filters.</li>
 *   <li>{@code GET /api/details/stats} — total record count.</li>
 *   <li>{@code GET /api/details/stats/null_counts} — NULL statistics.</li>
 *   <li>{@code GET /api/details/{mal_id}/characters} — characters in this anime.</li>
 *   <li>{@code GET /api/details/{mal_id}/recommendations} — recommended anime.</li>
 *   <li>{@code POST /api/details/update_score} — update anime score.</li>
 * </ul>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Only {@link DetailsService} is injected — no repository access.</li>
 *   <li>{@link DetailsDTO} and {@link CharactersDTO} objects are returned directly to
 *       {@link ResponseEntity}; Spring (Jackson) serialises them automatically
 *       using {@code @JsonProperty} annotations.</li>
 *   <li>Input validation is handled declaratively via {@code @Valid} and
 *       {@link UpdateScoreRequestDTO} — no manual null-checks needed.</li>
 * </ul>
 */
@Tag(name = "Details", description = "Anime details and relationships API")
@RestController
@RequestMapping("/api/details")
@CrossOrigin(origins = "*")
public class DetailsController {

    @Autowired
    private DetailsService service;

    /**
     * Returns a single anime looked up by its MAL ID.
     *
     * <p>Returns {@code 404 Not Found} if no anime with that ID exists,
     * otherwise {@code 200 OK} with the {@link DetailsDTO} serialised by Jackson.</p>
     *
     * @param malId anime MAL ID (path variable, primary key)
     * @return {@link ResponseEntity} with the {@link DetailsDTO}, or {@code 404}
     */
    @Operation(summary = "Get anime by ID",
            description = "Retrieve detailed information about a specific anime by its MyAnimeList ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime found successfully",
                    content = @Content(schema = @Schema(implementation = DetailsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Anime not found", content = @Content)
    })
    @GetMapping("/{mal_id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Anime MAL ID", example = "1", required = true)
            @PathVariable("mal_id") Integer malId) {

        Optional<DetailsDTO> dto = service.getById(malId);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",  "Anime not found",
                    "mal_id", malId));
        }

        return ResponseEntity.ok(dto.get());
    }

    /**
     * Returns a paginated list of {@link DetailsDTO} matching optional filters.
     *
     * <p>Supports three pagination modes (page-based takes priority over
     * limit/offset when both sets of parameters are present):</p>
     * <ul>
     *   <li>{@code page} + {@code pageSize} — response includes
     *       {@code page}, {@code pageSize}, {@code totalPages}, {@code items}.</li>
     *   <li>{@code limit} + {@code offset} — response includes
     *       {@code limit}, {@code offset}, {@code total}, {@code items}.</li>
     *   <li>No pagination parameters — returns the first 10 records as a plain list.</li>
     * </ul>
     *
     * <p>Optional filters (null/not-null filters take absolute priority):</p>
     * <ul>
     *   <li>{@code search} — case-insensitive partial match on title.</li>
     *   <li>{@code type} — exact type match.</li>
     *   <li>{@code year} — exact broadcast year match.</li>
     *   <li>{@code status} — exact airing status match.</li>
     *   <li>{@code rating} — exact age rating match.</li>
     *   <li>{@code source} — exact source material match.</li>
     *   <li>{@code genres} — case-insensitive partial match on genres.</li>
     *   <li>{@code episodes} — exact episode count match.</li>
     *   <li>{@code nullFilter} — field name for IS NULL filter.</li>
     *   <li>{@code notNullFilter} — field name for IS NOT NULL filter.</li>
     * </ul>
     *
     * @param search        partial match on title
     * @param sort          sort expression, e.g. {@code "-score"}
     * @param type          exact type filter
     * @param year          exact year filter
     * @param status        exact status filter
     * @param rating        exact rating filter
     * @param source        exact source filter
     * @param genres        partial match on genres
     * @param episodes      exact episode count filter
     * @param nullFilter    field name for IS NULL filter
     * @param notNullFilter field name for IS NOT NULL filter
     * @param limit         maximum results (limit/offset mode)
     * @param offset        records to skip (limit/offset mode)
     * @param page          1-indexed page number (page-based mode)
     * @param pageSize      records per page (page-based mode)
     * @return {@link ResponseEntity} with paginated or plain-list body of {@link DetailsDTO}
     */
    @Operation(summary = "Get all anime",
            description = "Retrieve paginated anime with optional filters and sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search anime title (case-insensitive)", example = "Cowboy Bebop")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-score")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by anime type", example = "TV")
            @RequestParam(required = false) String type,

            @Parameter(description = "Filter by release year", example = "1998")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "Filter by airing status", example = "Finished Airing")
            @RequestParam(required = false) String status,

            @Parameter(description = "Filter by age rating", example = "PG-13")
            @RequestParam(required = false) String rating,

            @Parameter(description = "Filter by source material", example = "Manga")
            @RequestParam(required = false) String source,

            @Parameter(description = "Filter by genre (partial match)", example = "Action")
            @RequestParam(required = false) String genres,

            @Parameter(description = "Filter by exact episode count", example = "26")
            @RequestParam(required = false) Integer episodes,

            @Parameter(description = "Filter records where field IS NULL", example = "synopsis")
            @RequestParam(required = false) String nullFilter,

            @Parameter(description = "Filter records where field IS NOT NULL", example = "score")
            @RequestParam(required = false) String notNullFilter,

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
            Page<DetailsDTO> pageResult = service.findWithFilters(
                    search, type, year, status, rating, source, genres, episodes,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  pageResult.getTotalElements(),
                    "items",  pageResult.getContent()));

        } else if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<DetailsDTO> pageResult = service.findWithFilters(
                    search, type, year, status, rating, source, genres, episodes,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<DetailsDTO> pageResult = service.findWithFilters(
                    search, type, year, status, rating, source, genres, episodes,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    /**
     * Returns the total number of anime records in the database.
     *
     * @return {@link ResponseEntity} with body {@code {"total": N}}
     */
    @Operation(summary = "Get statistics",
            description = "Get total count of anime in the database")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    /**
     * Returns NULL value statistics for each nullable field.
     *
     * @return {@link ResponseEntity} with body
     *         {@code {"null_counts": {...}, "total_records": N}}
     */
    @Operation(summary = "Get NULL value statistics",
            description = "Count of NULL values for nullable fields: synopsis, score, "
                    + "end_date, title_japanese, season, favorites")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "NULL statistics retrieved successfully"))
    @GetMapping("/stats/null_counts")
    public ResponseEntity<Map<String, Object>> getNullCounts() {
        return ResponseEntity.ok(Map.of(
                "null_counts",   service.getNullCounts(),
                "total_records", service.count()));
    }

    /**
     * Returns the paginated list of characters that appear in the given anime.
     *
     * <p>First checks existence via {@link DetailsService#existsById(Integer)} to
     * return a meaningful {@code 404} before the join query. Then delegates to
     * {@link DetailsService#getCharactersForAnime}, which returns
     * {@code Page<CharactersDTO>} — no raw entity reaches the controller.</p>
     *
     * @param malId    anime MAL ID
     * @param page     1-indexed page number (default 1)
     * @param pageSize results per page (default 10)
     * @return {@link ResponseEntity} with paginated body of {@link CharactersDTO},
     *         or {@code 404} if the anime does not exist
     */
    @Operation(summary = "Get characters in anime",
            description = "Retrieve all characters that appear in this anime, sorted by favorites")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Characters retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Anime not found", content = @Content)
    })
    @GetMapping("/{mal_id}/characters")
    public ResponseEntity<?> getCharacters(
            @Parameter(description = "Anime MAL ID", example = "1", required = true)
            @PathVariable("mal_id") Integer malId,

            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "Results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {

        if (!service.existsById(malId)) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",  "Anime not found",
                    "mal_id", malId));
        }

        int finalPage     = (page     != null) ? page     : 1;
        int finalPageSize = (pageSize != null) ? pageSize : 10;

        Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);
        Page<CharactersDTO> charactersPage = service.getCharactersForAnime(malId, pageable);

        return ResponseEntity.ok(Map.of(
                "mal_id",     malId,
                "page",       finalPage,
                "pageSize",   finalPageSize,
                "totalPages", charactersPage.getTotalPages(),
                "totalItems", charactersPage.getTotalElements(),
                "items",      charactersPage.getContent()));
    }

    /**
     * Returns a paginated list of anime recommended as similar to the given anime.
     *
     * <p>Fetches the source anime first for inclusion in the response body, then
     * delegates to {@link DetailsService#getRecommendationsForAnime}. Returns
     * {@code 404 Not Found} if the source anime does not exist.</p>
     *
     * @param malId    source anime MAL ID
     * @param page     1-indexed page number (default 1)
     * @param pageSize results per page (default 10)
     * @return {@link ResponseEntity} with paginated body of {@link DetailsDTO},
     *         or {@code 404} if the source anime does not exist
     */
    @Operation(summary = "Get anime recommendations",
            description = "Get recommended anime similar to the specified anime, sorted by score")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Anime not found", content = @Content)
    })
    @GetMapping("/{mal_id}/recommendations")
    public ResponseEntity<?> getRecommendations(
            @Parameter(description = "Anime MAL ID", example = "1", required = true)
            @PathVariable("mal_id") Integer malId,

            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {

        Optional<DetailsDTO> anime = service.getById(malId);
        if (anime.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",  "Anime not found",
                    "mal_id", malId));
        }

        int finalPage     = (page     != null) ? page     : 1;
        int finalPageSize = (pageSize != null) ? pageSize : 10;

        Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize,
                Sort.by("score").descending());

        Page<DetailsDTO> recommendationsPage =
                service.getRecommendationsForAnime(malId, pageable);

        return ResponseEntity.ok(Map.of(
                "anime",           anime.get(),
                "page",            finalPage,
                "pageSize",        finalPageSize,
                "totalPages",      recommendationsPage.getTotalPages(),
                "total",           recommendationsPage.getTotalElements(),
                "recommendations", recommendationsPage.getContent()));
    }

    /**
     * Updates the score for the specified anime.
     *
     * <p>{@code @Valid} triggers bean validation on {@link UpdateScoreRequestDTO}
     * before this method runs. If validation fails, Spring automatically returns
     * {@code 400 Bad Request} — no manual check is needed here.</p>
     *
     * <p>Delegates to {@link DetailsService#updateScore(Integer, java.math.BigDecimal)}
     * and returns {@code 404 Not Found} if the anime does not exist.</p>
     *
     * @param request the validated request body containing {@code mal_id} and {@code score}
     * @return {@link ResponseEntity} with body
     *         {@code {"success": true, "message": "...", "anime": DetailsDTO}},
     *         or {@code 404} with error body
     */
    @Operation(summary = "Update anime score",
            description = "Update the score for a specific anime. "
                    + "Body must contain mal_id (integer) and score (0.00 to 10.00).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Score updated successfully",
                    content = @Content(schema = @Schema(implementation = DetailsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Anime not found",    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input — validation failed", content = @Content)
    })
    @PostMapping("/update_score")
    public ResponseEntity<?> updateScore(
            @Valid @RequestBody UpdateScoreRequestDTO request) {

        Optional<DetailsDTO> updated = service.updateScore(request.getMalId(), request.getScore());

        if (updated.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error",   "Anime not found",
                    "mal_id",  request.getMalId()));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Score updated successfully",
                "anime",   updated.get()));
    }

    /**
     * Converts the {@code sort} query parameter string into a Spring
     * {@link Sort} object.
     *
     * <p>Each comma-separated token is a field name. A leading {@code -}
     * means descending; no prefix means ascending. {@code NULL} values are
     * always placed last via {@link Sort.Order#nullsLast()}. A stable
     * tiebreaker on {@code malId} is always appended.</p>
     *
     * @param sort comma-separated sort expression, e.g. {@code "-score,title"}
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
        return Sort.by(orders);
    }
}
