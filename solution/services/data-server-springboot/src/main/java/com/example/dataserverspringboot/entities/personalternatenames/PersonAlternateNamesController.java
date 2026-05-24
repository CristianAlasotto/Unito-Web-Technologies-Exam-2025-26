package com.example.dataserverspringboot.entities.personalternatenames;

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
 * REST API controller for the {@link PersonAlternateNames} module.
 *
 * <p>Exposes three endpoints under {@code /api/person_alternate_names}:</p>
 * <ul>
 *   <li>{@code GET /api/person_alternate_names} — paginated list with optional filters.</li>
 *   <li>{@code GET /api/person_alternate_names/stats} — total record count.</li>
 *   <li>{@code GET /api/person_alternate_names/single} — single record by composite key.</li>
 * </ul>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Only {@link PersonAlternateNamesService} is injected — no repository access.</li>
 *   <li>{@link PersonAlternateNamesDTO} objects are returned directly to
 *       {@link ResponseEntity}; Spring (Jackson) serialises them to JSON
 *       automatically using the {@code @JsonProperty} annotations on the DTO
 *       getters for snake_case field names.</li>
 *   <li>Every method returns {@link ResponseEntity} to allow full control
 *       over HTTP status codes.</li>
 * </ul>
 */
@Tag(name = "Person Alternate Names", description = "Anime alternate name of staff people and relationships API")
@RestController
@RequestMapping("/api/person_alternate_names")
@CrossOrigin(origins = "*")
public class PersonAlternateNamesController {

    @Autowired
    private PersonAlternateNamesService service;

    /**
     * Returns a paginated list of {@link PersonAlternateNamesDTO} matching optional filters.
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
     * <p>Optional filters (applied in priority order by the service):</p>
     * <ul>
     *   <li>{@code search} — case-insensitive partial match on the alternate name.</li>
     *   <li>{@code person_mal_id} — exact person ID match.</li>
     * </ul>
     *
     * <p>The {@code sort} parameter accepts a comma-separated list of field names.
     * Prefix a field name with {@code -} for descending order. {@code NULL} values
     * are always sorted last. Tiebreakers on both composite key fields are always
     * appended for deterministic pagination.</p>
     *
     * @param search      case-insensitive partial match on alternate name
     * @param sort        sort expression, e.g. {@code "alt_name"}
     * @param personMalId exact person ID filter
     * @param limit       maximum results (limit/offset mode)
     * @param offset      records to skip (limit/offset mode)
     * @param page        1-indexed page number (page-based mode)
     * @param pageSize    records per page (page-based mode)
     * @return {@link ResponseEntity} with paginated or plain-list body of
     *         {@link PersonAlternateNamesDTO}
     */
    @Operation(
            summary = "Get all person alternate names",
            description = "Retrieve paginated list of alternate names with optional filters and sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search by alternate name (case-insensitive)", example = "Miyazaki")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "alt_name")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by person MAL ID", example = "1")
            @RequestParam(value = "person_mal_id", required = false) Integer personMalId,

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
            Page<PersonAlternateNamesDTO> pageResult =
                    service.findWithFilters(search, personMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  pageResult.getTotalElements(),
                    "items",  pageResult.getContent()));

        } else if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<PersonAlternateNamesDTO> pageResult =
                    service.findWithFilters(search, personMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<PersonAlternateNamesDTO> pageResult =
                    service.findWithFilters(search, personMalId, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    /**
     * Returns the total number of alternate name records in the database.
     *
     * @return {@link ResponseEntity} with body {@code {"total": N}}
     */
    @Operation(summary = "Get statistics",
            description = "Get total count of alternate name records")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    /**
     * Returns a single alternate name record looked up by its composite key.
     *
     * <p>Both {@code person_mal_id} and {@code altName} are required.
     * If either is missing, {@code 400 Bad Request} is returned with a usage hint.
     * If the composite key does not exist, {@code 404 Not Found} is returned.</p>
     *
     * @param personMalId person MAL ID (required)
     * @param altName     alternate name (required)
     * @return {@link ResponseEntity} containing the {@link PersonAlternateNamesDTO},
     *         or an error body with status 400 or 404
     */
    @Operation(
            summary = "Get specific alternate name entry",
            description = "Retrieve a single alternate name record using the composite key "
                    + "(person_mal_id + alt_name)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found successfully",
                    content = @Content(schema = @Schema(implementation = PersonAlternateNamesDTO.class))),
            @ApiResponse(responseCode = "400", description = "Missing key fields",  content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",           content = @Content)
    })
    @GetMapping("/single")
    public ResponseEntity<?> getSingle(
            @Parameter(description = "Person MAL ID", required = true)
            @RequestParam(value = "person_mal_id", required = false) Integer personMalId,

            @Parameter(description = "Alternate name", required = true)
            @RequestParam(required = false) String altName) {

        if (personMalId == null || altName == null) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "All key fields required: person_mal_id, alt_name",
                    "usage", "GET /api/person_alternate_names/single?person_mal_id=1&altName=Miyazaki+Hayao"));
        }

        PersonAlternateNames.PersonAlternateNamesId id =
                new PersonAlternateNames.PersonAlternateNamesId(personMalId, altName);

        Optional<PersonAlternateNamesDTO> dto = service.getById(id);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",         "PersonAlternateNames not found",
                    "person_mal_id", personMalId,
                    "alt_name",      altName));
        }

        return ResponseEntity.ok(dto.get());
    }

    /**
     * Converts the {@code sort} query parameter string into a Spring
     * {@link Sort} object.
     *
     * <p>Each comma-separated token is a field name. A leading {@code -}
     * means descending; no prefix means ascending. {@code NULL} values are
     * always placed last via {@link Sort.Order#nullsLast()}. Stable tiebreakers
     * on both composite key fields ({@code personMalId} and {@code altName})
     * are always appended for deterministic pagination.</p>
     *
     * @param sort comma-separated sort expression, e.g. {@code "alt_name"}
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
        orders.add(Sort.Order.asc("altName"));
        return Sort.by(orders);
    }
}
