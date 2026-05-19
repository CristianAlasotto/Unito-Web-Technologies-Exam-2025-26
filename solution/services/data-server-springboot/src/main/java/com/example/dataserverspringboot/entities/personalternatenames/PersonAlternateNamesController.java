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
 * REST API Controller for PersonAlternateNames.
 *
 * DESIGN:
 *   - Controller only receives requests, delegates to service, and returns responses.
 *   - No repository is injected here — all data access goes through PersonAlternateNamesService.
 *   - DTOs are returned directly — Spring (Jackson) converts them to JSON automatically,
 *     using @JsonProperty annotations on the DTO getters for snake_case field names.
 */
@Tag(name = "Person Alternate Names", description = "Anime alternate name of staff people and relationships API")
@RestController
@RequestMapping("/api/person_alternate_names")
@CrossOrigin(origins = "*")
public class PersonAlternateNamesController {

    @Autowired
    private PersonAlternateNamesService service;

    // ── GET /api/person_alternate_names ───────────────────────────────────────

    @Operation(
            summary = "Get all person alternate names",
            description = "Retrieve paginated list of alternate names with optional filters and sorting."
    )
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

    // ── GET /api/person_alternate_names/stats ─────────────────────────────────

    @Operation(summary = "Get statistics", description = "Get total count of alternate name records")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    // ── GET /api/person_alternate_names/single ────────────────────────────────

    @Operation(
            summary = "Get specific alternate name entry",
            description = "Retrieve a single alternate name record using the composite key (person_mal_id + alt_name)"
    )
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

        // Jackson serialises PersonAlternateNamesDTO to JSON automatically via @JsonProperty
        return ResponseEntity.ok(dto.get());
    }

    // ── Private helper ────────────────────────────────────────────────────────

    /**
     * Parse the sort query parameter into a Spring Sort object.
     * Prefix a field name with "-" for descending order (e.g. "-alt_name").
     * NULL values are always sorted last.
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
        // Composite key tiebreakers
        orders.add(Sort.Order.asc("personMalId"));
        orders.add(Sort.Order.asc("altName"));
        return Sort.by(orders);
    }
}
