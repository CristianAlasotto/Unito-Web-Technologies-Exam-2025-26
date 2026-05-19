package com.example.dataserverspringboot.entities.personanimeworks;

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
 * REST API Controller for PersonAnimeWorks.
 *
 * DESIGN:
 *   - Controller only receives requests, delegates to service, and returns responses.
 *   - No repository is injected here — all data access goes through PersonAnimeWorksService.
 *   - DTOs are returned directly — Spring (Jackson) converts them to JSON automatically,
 *     using @JsonProperty annotations on the DTO getters for snake_case field names.
 */
@Tag(name = "Person Anime Works", description = "Anime people staff with position and relationships API")
@RestController
@RequestMapping("/api/person_anime_works")
@CrossOrigin(origins = "*")
public class PersonAnimeWorksController {

    @Autowired
    private PersonAnimeWorksService service;

    // ── GET /api/person_anime_works ───────────────────────────────────────────

    @Operation(
            summary = "Get all person anime works",
            description = "Retrieve paginated list of works with optional filters for position, person ID, or anime ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search by position (case-insensitive)", example = "Director")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-position")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by position (exact match)", example = "Director")
            @RequestParam(required = false) String position,

            @Parameter(description = "Filter by person MAL ID", example = "1")
            @RequestParam(value = "person_mal_id", required = false) Integer personMalId,

            @Parameter(description = "Filter by anime MAL ID", example = "1")
            @RequestParam(value = "anime_mal_id", required = false) Integer animeMalId,

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
            Page<PersonAnimeWorksDTO> pageResult = service.findWithFilters(
                    search, position, personMalId, animeMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  pageResult.getTotalElements(),
                    "items",  pageResult.getContent()));

        } else if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<PersonAnimeWorksDTO> pageResult = service.findWithFilters(
                    search, position, personMalId, animeMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<PersonAnimeWorksDTO> pageResult = service.findWithFilters(
                    search, position, personMalId, animeMalId, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    // ── GET /api/person_anime_works/stats ─────────────────────────────────────

    @Operation(summary = "Get statistics", description = "Get total count of person anime work records")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    // ── GET /api/person_anime_works/single ────────────────────────────────────

    @Operation(
            summary = "Get specific work entry",
            description = "Retrieve a single work record using the composite key (person_mal_id + position + anime_mal_id)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found successfully",
                    content = @Content(schema = @Schema(implementation = PersonAnimeWorksDTO.class))),
            @ApiResponse(responseCode = "400", description = "Missing key fields",  content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",           content = @Content)
    })
    @GetMapping("/single")
    public ResponseEntity<?> getSingle(
            @Parameter(description = "Person MAL ID", required = true)
            @RequestParam(value = "person_mal_id", required = false) Integer personMalId,

            @Parameter(description = "Position", required = true)
            @RequestParam(required = false) String position,

            @Parameter(description = "Anime MAL ID", required = true)
            @RequestParam(value = "anime_mal_id", required = false) Integer animeMalId) {

        if (personMalId == null || position == null || animeMalId == null) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "All key fields required: person_mal_id, position, anime_mal_id",
                    "usage", "GET /api/person_anime_works/single?person_mal_id=1&position=Director&anime_mal_id=1"));
        }

        PersonAnimeWorks.PersonAnimeWorksId id =
                new PersonAnimeWorks.PersonAnimeWorksId(personMalId, position, animeMalId);

        Optional<PersonAnimeWorksDTO> dto = service.getById(id);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",         "PersonAnimeWorks not found",
                    "person_mal_id", personMalId,
                    "position",      position,
                    "anime_mal_id",  animeMalId));
        }

        // Jackson serialises PersonAnimeWorksDTO to JSON automatically via @JsonProperty
        return ResponseEntity.ok(dto.get());
    }

    // ── Private helper ────────────────────────────────────────────────────────

    /**
     * Parse the sort query parameter into a Spring Sort object.
     * Prefix a field name with "-" for descending order (e.g. "-position").
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
        orders.add(Sort.Order.asc("animeMalId"));
        return Sort.by(orders);
    }
}
