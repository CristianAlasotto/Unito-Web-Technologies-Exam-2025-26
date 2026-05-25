package com.example.dataserverspringboot.entities.personvoiceworks;

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
 * REST API controller for the {@link PersonVoiceWorks} module.
 *
 * <p>Exposes four endpoints under {@code /api/person_voice_works}:</p>
 * <ul>
 *   <li>{@code GET /api/person_voice_works/{id}} — single record by composite key.</li>
 *   <li>{@code GET /api/person_voice_works} — paginated list with optional filters.</li>
 *   <li>{@code GET /api/person_voice_works/stats} — total record count.</li>
 *   <li>{@code GET /api/person_voice_works/stats/null_counts} — NULL statistics.</li>
 * </ul>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Only {@link PersonVoiceWorksService} is injected — no repository access.</li>
 *   <li>{@link PersonVoiceWorksDTO} objects are returned directly to
 *       {@link ResponseEntity}; Spring (Jackson) serialises them to JSON
 *       automatically using the {@code @JsonProperty} annotations on the DTO
 *       getters for snake_case field names.</li>
 *   <li>Every method returns {@link ResponseEntity} to allow full control
 *       over HTTP status codes, as shown in the professor's slides.</li>
 * </ul>
 */
@Tag(name = "Person Voice Works", description = "Anime voice actors with their roles and languages API")
@RestController
@RequestMapping("/api/person_voice_works")
@CrossOrigin(origins = "*")
public class PersonVoiceWorksController {

    @Autowired
    private PersonVoiceWorksService service;

    /**
     * Returns a single voice work record looked up by its composite key.
     *
     * <p>The composite key is passed as a path variable and Spring automatically
     * binds it to {@link PersonVoiceWorks.PersonVoiceWorksId}. Returns
     * {@code 404 Not Found} if no record matches the key.</p>
     *
     * @param id composite key ({@code personMalId + characterMalId + animeMalId})
     * @return {@link ResponseEntity} with the {@link PersonVoiceWorksDTO},
     *         or {@code 404} with an error body
     */
    @Operation(summary = "Get specific voice work entry",
            description = "Retrieve a single voice acting record using the composite key "
                    + "(person_mal_id + character_mal_id + anime_mal_id)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found successfully",
                    content = @Content(schema = @Schema(implementation = PersonVoiceWorksDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Composite ID", required = true)
            @PathVariable PersonVoiceWorks.PersonVoiceWorksId id) {

        Optional<PersonVoiceWorksDTO> dto = service.getById(id);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "PersonVoiceWorks not found",
                    "id",    id));
        }

        return ResponseEntity.ok(dto.get());
    }

    /**
     * Returns a paginated list of {@link PersonVoiceWorksDTO} matching optional filters.
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
     *   <li>{@code search} — case-insensitive partial match on language.</li>
     *   <li>{@code language} — exact language match.</li>
     *   <li>{@code role} — exact role match.</li>
     *   <li>{@code person_mal_id} — exact person ID match.</li>
     *   <li>{@code character_mal_id} — exact character ID match.</li>
     *   <li>{@code anime_mal_id} — exact anime ID match.</li>
     *   <li>{@code nullFilter} — field name for IS NULL filter ({@code role}, {@code language}).</li>
     *   <li>{@code notNullFilter} — field name for IS NOT NULL filter.</li>
     * </ul>
     *
     * @param search         case-insensitive partial match on language
     * @param sort           sort expression, e.g. {@code "-role"}
     * @param language       exact language filter
     * @param role           exact role filter
     * @param person_mal_id  exact person ID filter
     * @param character_mal_id exact character ID filter
     * @param anime_mal_id   exact anime ID filter
     * @param nullFilter     field name for IS NULL filter
     * @param notNullFilter  field name for IS NOT NULL filter
     * @param limit          maximum results (limit/offset mode)
     * @param offset         records to skip (limit/offset mode)
     * @param page           1-indexed page number (page-based mode)
     * @param pageSize       records per page (page-based mode)
     * @return {@link ResponseEntity} with a paginated or plain-list body of
     *         {@link PersonVoiceWorksDTO}
     */
    @Operation(summary = "Get all voice works",
            description = "Retrieve paginated list of voice works with optional filters and sorting. "
                    + "NULL values are always sorted last.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search by language (case-insensitive)", example = "Japanese")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-role")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by language", example = "Japanese")
            @RequestParam(required = false) String language,

            @Parameter(description = "Filter by role", example = "Main")
            @RequestParam(required = false) String role,

            @Parameter(description = "Filter by person MAL ID", example = "1")
            @RequestParam(required = false) Integer person_mal_id,

            @Parameter(description = "Filter by character MAL ID", example = "1")
            @RequestParam(required = false) Integer character_mal_id,

            @Parameter(description = "Filter by anime MAL ID", example = "1")
            @RequestParam(required = false) Integer anime_mal_id,

            @Parameter(description = "Filter records where field IS NULL", example = "role")
            @RequestParam(required = false) String nullFilter,

            @Parameter(description = "Filter records where field IS NOT NULL", example = "language")
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
            Page<PersonVoiceWorksDTO> pageResult = service.findWithFilters(
                    search, language, role,
                    person_mal_id, character_mal_id, anime_mal_id,
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
            Page<PersonVoiceWorksDTO> pageResult = service.findWithFilters(
                    search, language, role,
                    person_mal_id, character_mal_id, anime_mal_id,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<PersonVoiceWorksDTO> pageResult = service.findWithFilters(
                    search, language, role,
                    person_mal_id, character_mal_id, anime_mal_id,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    /**
     * Returns the total number of voice work records in the database.
     *
     * @return {@link ResponseEntity} with body {@code {"total": N}}
     */
    @Operation(summary = "Get statistics",
            description = "Get total count of voice work records")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    /**
     * Returns NULL value statistics for each nullable field.
     *
     * <p>Calls {@link PersonVoiceWorksService#getNullCounts()} and includes
     * the total record count for percentage calculations on the client.</p>
     *
     * @return {@link ResponseEntity} with body
     *         {@code {"null_counts": {...}, "total_records": N}}
     */
    @Operation(summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field (role, language)")
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
     * on the three composite key fields are always appended.</p>
     *
     * @param sort comma-separated sort expression, e.g. {@code "-role,language"}
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
        orders.add(Sort.Order.asc("personMalId"));
        orders.add(Sort.Order.asc("characterMalId"));
        orders.add(Sort.Order.asc("animeMalId"));
        return Sort.by(orders);
    }
}
