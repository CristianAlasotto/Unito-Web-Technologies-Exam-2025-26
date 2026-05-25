package com.example.dataserverspringboot.entities.persondetails;

import com.example.dataserverspringboot.entities.details.DetailsDTO;
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
 * REST API controller for the {@link PersonDetails} module.
 *
 * <p>Exposes five endpoints under {@code /api/person_details}:</p>
 * <ul>
 *   <li>{@code GET /api/person_details/{person_mal_id}} — single person by ID.</li>
 *   <li>{@code GET /api/person_details/{person_mal_id}/details} — anime works list.</li>
 *   <li>{@code GET /api/person_details} — paginated list with optional filters.</li>
 *   <li>{@code GET /api/person_details/stats} — total record count.</li>
 *   <li>{@code GET /api/person_details/stats/null_counts} — NULL statistics.</li>
 * </ul>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Only {@link PersonDetailsService} is injected — no repository access.</li>
 *   <li>{@link PersonDetailsDTO} and {@link DetailsDTO} objects are returned
 *       directly to {@link ResponseEntity}; Spring (Jackson) serialises them
 *       to JSON automatically using {@code @JsonProperty} annotations.</li>
 *   <li>Every method returns {@link ResponseEntity} to allow full control
 *       over HTTP status codes.</li>
 * </ul>
 */
@Tag(name = "Person Details", description = "Voice actor and staff information API")
@RestController
@RequestMapping("/api/person_details")
@CrossOrigin(origins = "*")
public class PersonDetailsController {

    @Autowired
    private PersonDetailsService service;

    /**
     * Returns a single person looked up by their MAL ID.
     *
     * <p>Calls {@link PersonDetailsService#getById(Integer)} and unwraps the
     * {@link Optional}. Returns {@code 404 Not Found} if no person with that
     * ID exists, otherwise {@code 200 OK} with the {@link PersonDetailsDTO}
     * body serialised by Jackson.</p>
     *
     * @param person_mal_id the person MAL ID (path variable, primary key)
     * @return {@link ResponseEntity} with the {@link PersonDetailsDTO},
     *         or {@code 404} with an error body
     */
    @Operation(summary = "Get person by ID",
            description = "Retrieve a single person by their MyAnimeList ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Person found successfully",
                    content = @Content(schema = @Schema(implementation = PersonDetailsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content)
    })
    @GetMapping("/{person_mal_id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Person MAL ID", example = "1", required = true)
            @PathVariable Integer person_mal_id) {

        Optional<PersonDetailsDTO> dto = service.getById(person_mal_id);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",         "Person not found",
                    "person_mal_id", person_mal_id));
        }

        return ResponseEntity.ok(dto.get());
    }

    /**
     * Returns the paginated list of anime this person has worked on.
     *
     * <p>First checks existence via {@link PersonDetailsService#existsById(Integer)}
     * to return a meaningful {@code 404} before attempting the join query.
     * Then delegates to {@link PersonDetailsService#getAnimeWorksForPerson},
     * which executes the cross-entity JPQL join and returns
     * {@code Page<DetailsDTO>} — the raw entity never reaches the controller.</p>
     *
     * @param person_mal_id the person to look up anime works for
     * @param page          1-indexed page number (default 1)
     * @param pageSize      results per page (default 10)
     * @return {@link ResponseEntity} with paginated body of {@link DetailsDTO},
     *         or {@code 404} if the person does not exist
     */
    @Operation(summary = "Get anime works",
            description = "Retrieve all anime this person has worked on, sorted by score")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime list retrieved"),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content)
    })
    @GetMapping("/{person_mal_id}/details")
    public ResponseEntity<?> getAnimeWorks(
            @Parameter(description = "Person MAL ID", example = "1", required = true)
            @PathVariable Integer person_mal_id,

            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "Results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {

        if (!service.existsById(person_mal_id)) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",         "Person not found",
                    "person_mal_id", person_mal_id));
        }

        int finalPage     = (page     != null) ? page     : 1;
        int finalPageSize = (pageSize != null) ? pageSize : 10;

        Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);

        Page<DetailsDTO> animePage = service.getAnimeWorksForPerson(person_mal_id, pageable);

        return ResponseEntity.ok(Map.of(
                "person_mal_id", person_mal_id,
                "page",          finalPage,
                "pageSize",      finalPageSize,
                "totalPages",    animePage.getTotalPages(),
                "totalItems",    animePage.getTotalElements(),
                "items",         animePage.getContent()));
    }

    /**
     * Returns a paginated list of {@link PersonDetailsDTO} matching optional filters.
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
     *   <li>{@code search} — case-insensitive partial match on name.</li>
     *   <li>{@code city} — case-insensitive partial match on location.</li>
     *   <li>{@code nullFilter} — field name for IS NULL filter.</li>
     *   <li>{@code notNullFilter} — field name for IS NOT NULL filter.</li>
     * </ul>
     *
     * @param search        case-insensitive partial match on name
     * @param sort          sort expression, e.g. {@code "-favorites"}
     * @param city          case-insensitive partial match on location
     * @param nullFilter    field name for IS NULL filter
     * @param notNullFilter field name for IS NOT NULL filter
     * @param limit         maximum results (limit/offset mode)
     * @param offset        records to skip (limit/offset mode)
     * @param page          1-indexed page number (page-based mode)
     * @param pageSize      records per page (page-based mode)
     * @return {@link ResponseEntity} with paginated or plain-list body of
     *         {@link PersonDetailsDTO}
     */
    @Operation(summary = "Get all people",
            description = "Retrieve paginated list of people with optional filters and sorting. "
                    + "NULL values are always sorted last.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "People retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search by name (case-insensitive)", example = "Miyazaki")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-favorites")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by city/location", example = "Tokyo")
            @RequestParam(required = false) String city,

            @Parameter(description = "Filter records where field IS NULL", example = "website_url")
            @RequestParam(required = false) String nullFilter,

            @Parameter(description = "Filter records where field IS NOT NULL", example = "birthday")
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
            Page<PersonDetailsDTO> pageResult = service.findWithFilters(
                    search, city, nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  pageResult.getTotalElements(),
                    "items",  pageResult.getContent()));

        } else if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<PersonDetailsDTO> pageResult = service.findWithFilters(
                    search, city, nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<PersonDetailsDTO> pageResult = service.findWithFilters(
                    search, city, nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    /**
     * Returns the total number of person records in the database.
     *
     * @return {@link ResponseEntity} with body {@code {"total": N}}
     */
    @Operation(summary = "Get statistics",
            description = "Get total count of people in the database")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    /**
     * Returns NULL value statistics for each nullable field.
     *
     * <p>Calls {@link PersonDetailsService#getNullCounts()} and includes the
     * total record count for percentage calculations on the client.</p>
     *
     * @return {@link ResponseEntity} with body
     *         {@code {"null_counts": {...}, "total_records": N}}
     */
    @Operation(summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field "
                    + "(website_url, given_name, family_name, birthday, relevant_location)")
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
     * always placed last via {@link Sort.Order#nullsLast()}. A stable
     * tiebreaker on {@code personMalId} is always appended.</p>
     *
     * @param sort comma-separated sort expression, e.g. {@code "-favorites,name"}
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
        return Sort.by(orders);
    }
}
