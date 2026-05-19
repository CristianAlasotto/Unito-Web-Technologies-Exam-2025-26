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
 * REST API Controller for PersonVoiceWorks.
 *
 * DESIGN:
 *   - Controller only receives requests, delegates to service, and returns responses.
 *   - No repository is injected here — all data access goes through PersonVoiceWorksService.
 *   - DTOs are returned directly — Spring (Jackson) converts them to JSON automatically,
 *     using @JsonProperty annotations on the DTO getters for snake_case field names.
 */
@Tag(name = "Person Voice Works", description = "Anime voice actors with their roles and languages API")
@RestController
@RequestMapping("/api/person_voice_works")
@CrossOrigin(origins = "*")
public class PersonVoiceWorksController {

    @Autowired
    private PersonVoiceWorksService service;

    // ── GET /api/person_voice_works/{id} ──────────────────────────────────────

    @Operation(summary = "Get specific voice work entry",
            description = "Retrieve a single voice acting record using the composite key " +
                    "(person_mal_id + character_mal_id + anime_mal_id)")
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

        // Jackson serialises PersonVoiceWorksDTO to JSON automatically via @JsonProperty
        return ResponseEntity.ok(dto.get());
    }

    // ── GET /api/person_voice_works ───────────────────────────────────────────

    @Operation(summary = "Get all voice works",
            description = "Retrieve paginated list of voice works with optional filters and sorting. " +
                    "NULL values are always sorted last.")
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

    // ── GET /api/person_voice_works/stats ─────────────────────────────────────

    @Operation(summary = "Get statistics", description = "Get total count of voice work records")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    // ── GET /api/person_voice_works/stats/null_counts ─────────────────────────

    @Operation(summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field (role, language)")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "NULL statistics retrieved successfully"))
    @GetMapping("/stats/null_counts")
    public ResponseEntity<Map<String, Object>> getNullCounts() {
        return ResponseEntity.ok(Map.of(
                "null_counts",   service.getNullCounts(),
                "total_records", service.count()));
    }

    // ── Private helper ────────────────────────────────────────────────────────

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
