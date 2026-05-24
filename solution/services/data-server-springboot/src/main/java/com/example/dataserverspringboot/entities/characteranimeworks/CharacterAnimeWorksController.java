package com.example.dataserverspringboot.entities.characteranimeworks;

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
 * REST API controller for the {@link CharacterAnimeWorks} module.
 *
 * <p>Exposes four endpoints under {@code /api/character_anime_works}:</p>
 * <ul>
 *   <li>{@code GET /api/character_anime_works/{id}} — single record by composite key.</li>
 *   <li>{@code GET /api/character_anime_works} — paginated list with optional filters.</li>
 *   <li>{@code GET /api/character_anime_works/stats} — total record count.</li>
 *   <li>{@code GET /api/character_anime_works/stats/null_counts} — NULL statistics.</li>
 * </ul>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Only {@link CharacterAnimeWorksService} is injected — no repository access.</li>
 *   <li>{@link CharacterAnimeWorksDTO} objects are returned directly to
 *       {@link ResponseEntity}; Spring (Jackson) serialises them automatically
 *       using {@code @JsonProperty} annotations on the DTO getters.</li>
 *   <li>Every method returns {@link ResponseEntity} to allow full control
 *       over HTTP status codes.</li>
 * </ul>
 */
@Tag(name = "Character Anime Works", description = "Anime characters with role and relationships API")
@RestController
@RequestMapping("/api/character_anime_works")
@CrossOrigin(origins = "*")
public class CharacterAnimeWorksController {

    @Autowired
    private CharacterAnimeWorksService service;

    /**
     * Returns a single record looked up by its composite key.
     *
     * <p>The composite key is passed as a path variable and Spring automatically
     * binds it to {@link CharacterAnimeWorks.CharacterAnimeWorksId}. Returns
     * {@code 404 Not Found} if no record matches the key.</p>
     *
     * @param id composite key ({@code characterMalId + animeMalId})
     * @return {@link ResponseEntity} with the {@link CharacterAnimeWorksDTO},
     *         or {@code 404} with an error body
     */
    @Operation(
            summary = "Get character work by Composite ID",
            description = "Retrieve a single character work entry using character ID and anime ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entry found successfully",
                    content = @Content(schema = @Schema(implementation = CharacterAnimeWorksDTO.class))),
            @ApiResponse(responseCode = "404", description = "Entry not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Composite ID (character_mal_id + anime_mal_id)", required = true)
            @PathVariable CharacterAnimeWorks.CharacterAnimeWorksId id) {

        Optional<CharacterAnimeWorksDTO> dto = service.getById(id);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "CharacterAnimeWorks not found",
                    "id",    id));
        }

        return ResponseEntity.ok(dto.get());
    }

    /**
     * Returns a paginated list of {@link CharacterAnimeWorksDTO} matching optional filters.
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
     * <p>Optional filters (null/not-null take absolute priority):</p>
     * <ul>
     *   <li>{@code search} — case-insensitive partial match on role.</li>
     *   <li>{@code role} — exact role match.</li>
     *   <li>{@code character_mal_id} — exact character ID match.</li>
     *   <li>{@code anime_mal_id} — exact anime ID match.</li>
     *   <li>{@code nullFilter} — field name for IS NULL filter
     *       ({@code character_name} or {@code role}).</li>
     *   <li>{@code notNullFilter} — field name for IS NOT NULL filter.</li>
     * </ul>
     *
     * @param search         partial match on role
     * @param sort           sort expression, e.g. {@code "-role"}
     * @param role           exact role filter
     * @param character_mal_id exact character ID filter
     * @param anime_mal_id   exact anime ID filter
     * @param nullFilter     field name for IS NULL filter
     * @param notNullFilter  field name for IS NOT NULL filter
     * @param limit          maximum results (limit/offset mode)
     * @param offset         records to skip (limit/offset mode)
     * @param page           1-indexed page number (page-based mode)
     * @param pageSize       records per page (page-based mode)
     * @return {@link ResponseEntity} with paginated or plain-list body of
     *         {@link CharacterAnimeWorksDTO}
     */
    @Operation(
            summary = "Get all character works",
            description = "Retrieve paginated list of character works with optional filters and sorting. "
                    + "NULL values are always sorted last.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search by role (case-insensitive)", example = "Main")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-role")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by role", example = "Supporting")
            @RequestParam(required = false) String role,

            @Parameter(description = "Filter by character MAL ID", example = "1")
            @RequestParam(required = false) Integer character_mal_id,

            @Parameter(description = "Filter by anime MAL ID", example = "1")
            @RequestParam(required = false) Integer anime_mal_id,

            @Parameter(description = "Filter records where field IS NULL", example = "character_name")
            @RequestParam(required = false) String nullFilter,

            @Parameter(description = "Filter records where field IS NOT NULL", example = "role")
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
            Page<CharacterAnimeWorksDTO> pageResult = service.findWithFilters(
                    search, role, character_mal_id, anime_mal_id,
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
            Page<CharacterAnimeWorksDTO> pageResult = service.findWithFilters(
                    search, role, character_mal_id, anime_mal_id,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<CharacterAnimeWorksDTO> pageResult = service.findWithFilters(
                    search, role, character_mal_id, anime_mal_id,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    /**
     * Returns the total number of character-anime records in the database.
     *
     * @return {@link ResponseEntity} with body {@code {"total": N}}
     */
    @Operation(summary = "Get statistics",
            description = "Get total count of character work records")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    /**
     * Returns NULL value statistics for each nullable non-key field.
     *
     * @return {@link ResponseEntity} with body
     *         {@code {"null_counts": {"character_name": N, "role": N}, "total_records": N}}
     */
    @Operation(
            summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field (character_name, role)")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "NULL statistics retrieved successfully"))
    @GetMapping("/stats/null_counts")
    public ResponseEntity<Map<String, Object>> getNullCounts() {
        return ResponseEntity.ok(Map.of(
                "null_counts",   service.getNullCounts(),
                "total_records", service.count()));
    }

    /**
     * Converts the {@code sort} query parameter string into a Spring
     * {@link Sort} object.
     *
     * <p>Each comma-separated token is a field name. A leading {@code -}
     * means descending; no prefix means ascending. {@code NULL} values are
     * always placed last via {@link Sort.Order#nullsLast()}. Stable tiebreakers
     * on both composite key fields ({@code characterMalId} and {@code animeMalId})
     * are always appended for deterministic pagination.</p>
     *
     * @param sort comma-separated sort expression, e.g. {@code "-role"}
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
        orders.add(Sort.Order.asc("characterMalId"));
        orders.add(Sort.Order.asc("animeMalId"));
        return Sort.by(orders);
    }
}
