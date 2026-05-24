package com.example.dataserverspringboot.entities.profiles;

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
 * REST API controller for the {@link Profiles} module.
 *
 * <p>Exposes four endpoints under {@code /api/profiles}:</p>
 * <ul>
 *   <li>{@code GET /api/profiles/{username}} — single profile by username.</li>
 *   <li>{@code GET /api/profiles} — paginated list with optional filters.</li>
 *   <li>{@code GET /api/profiles/stats} — total record count.</li>
 *   <li>{@code GET /api/profiles/stats/null_counts} — NULL statistics.</li>
 * </ul>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Only {@link ProfilesService} is injected — no repository access.</li>
 *   <li>{@link ProfilesDTO} objects are returned directly to
 *       {@link ResponseEntity}; Spring (Jackson) serialises them to JSON
 *       automatically using the {@code @JsonProperty} annotations on the DTO
 *       getters for snake_case field names.</li>
 *   <li>Every method returns {@link ResponseEntity} to allow full control
 *       over HTTP status codes, as shown in the professor's slides.</li>
 * </ul>
 */
@Tag(name = "Profile", description = "Website users and relationships API")
@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*")
public class ProfilesController {

    @Autowired
    private ProfilesService service;

    /**
     * Returns a single user profile looked up by username.
     *
     * <p>Calls {@link ProfilesService#getById(String)} and unwraps the
     * {@link Optional}. Returns {@code 404 Not Found} if no profile with
     * that username exists, otherwise {@code 200 OK} with the
     * {@link ProfilesDTO} body serialised by Jackson.</p>
     *
     * @param username the username to look up (path variable, primary key)
     * @return {@link ResponseEntity} with the {@link ProfilesDTO},
     *         or {@code 404} with an error body
     */
    @Operation(summary = "Get profile by Username",
            description = "Retrieve a single user profile using their username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found successfully",
                    content = @Content(schema = @Schema(implementation = ProfilesDTO.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    @GetMapping("/{username}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Username (Primary Key)", example = "Xinil", required = true)
            @PathVariable("username") String username) {

        Optional<ProfilesDTO> dto = service.getById(username);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",    "Profile not found",
                    "username", username));
        }

        return ResponseEntity.ok(dto.get());
    }

    /**
     * Returns a paginated list of {@link ProfilesDTO} matching optional filters.
     *
     * <p>Supports three pagination modes (page-based takes priority over
     * limit/offset when both sets of parameters are present):</p>
     * <ul>
     *   <li>{@code page} + {@code pageSize} — page-based; response includes
     *       {@code page}, {@code pageSize}, {@code totalPages}, {@code items}.</li>
     *   <li>{@code limit} + {@code offset} — response includes
     *       {@code limit}, {@code offset}, {@code total}, {@code items}.</li>
     *   <li>No pagination parameters — returns the first 10 records as a plain list.</li>
     * </ul>
     *
     * <p>Optional filters (mutually exclusive; null/not-null filters take priority):</p>
     * <ul>
     *   <li>{@code search} — case-insensitive partial match on username.</li>
     *   <li>{@code gender} — exact match on gender.</li>
     *   <li>{@code location} — exact match on location.</li>
     *   <li>{@code nullFilter} — field name to filter with IS NULL
     *       ({@code gender}, {@code birthday}, {@code location}).</li>
     *   <li>{@code notNullFilter} — field name to filter with IS NOT NULL.</li>
     * </ul>
     *
     * <p>The {@code sort} parameter accepts a comma-separated list of field names.
     * Prefix a name with {@code -} for descending order. {@code NULL} values are
     * always sorted last. A stable tiebreaker on {@code username} is always appended.</p>
     *
     * @param search        case-insensitive partial match on username
     * @param sort          sort expression, e.g. {@code "-joined,username"}
     * @param gender        exact gender filter
     * @param location      exact location filter
     * @param nullFilter    field name for IS NULL filter
     * @param notNullFilter field name for IS NOT NULL filter
     * @param limit         maximum results (limit/offset mode)
     * @param offset        records to skip (limit/offset mode)
     * @param page          1-indexed page number (page-based mode)
     * @param pageSize      records per page (page-based mode)
     * @return {@link ResponseEntity} with a paginated or plain-list body of
     *         {@link ProfilesDTO}
     */
    @Operation(summary = "Get all profiles",
            description = "Retrieve paginated list of profiles with optional filters and sorting. " +
                    "NULL values are always sorted last.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search by username (case-insensitive)", example = "Xinil")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-joined")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by gender", example = "Male")
            @RequestParam(required = false) String gender,

            @Parameter(description = "Filter by location", example = "California")
            @RequestParam(required = false) String location,

            @Parameter(description = "Filter records where field IS NULL", example = "location")
            @RequestParam(required = false) String nullFilter,

            @Parameter(description = "Filter records where field IS NOT NULL", example = "gender")
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
            Page<ProfilesDTO> pageResult = service.findWithFilters(
                    search, gender, location, nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  pageResult.getTotalElements(),
                    "items",  pageResult.getContent()));

        } else if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<ProfilesDTO> pageResult = service.findWithFilters(
                    search, gender, location, nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<ProfilesDTO> pageResult = service.findWithFilters(
                    search, gender, location, nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    /**
     * Returns the total number of profile records in the database.
     *
     * @return {@link ResponseEntity} with body {@code {"total": N}}
     */
    @Operation(summary = "Get statistics",
            description = "Get total count of profiles in the database")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    /**
     * Returns NULL value statistics for each nullable field.
     *
     * <p>Calls {@link ProfilesService#getNullCounts()} and includes the
     * total record count for percentage calculations on the client.</p>
     *
     * @return {@link ResponseEntity} with body
     *         {@code {"null_counts": {...}, "total_records": N}}
     */
    @Operation(summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field "
                    + "(gender, birthday, location)")
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
     * <p>Each comma-separated token is interpreted as a field name. A leading
     * {@code -} means descending; no prefix means ascending. {@code NULL} values
     * are always placed last via {@link Sort.Order#nullsLast()}. A stable
     * tiebreaker on {@code username} is always appended so that results are
     * deterministic across pages.</p>
     *
     * @param sort comma-separated sort expression, e.g. {@code "-joined,username"}
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
        orders.add(Sort.Order.asc("username"));
        return Sort.by(orders);
    }
}
