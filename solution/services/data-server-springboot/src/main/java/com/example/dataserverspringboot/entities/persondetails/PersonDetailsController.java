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
 * REST API Controller for PersonDetails.
 *
 * DESIGN:
 *   - Controller only receives requests, delegates to service, and returns responses.
 *   - No repository is injected here — all data access goes through PersonDetailsService.
 *   - DTOs are returned directly — Spring (Jackson) converts them to JSON automatically,
 *     using @JsonProperty annotations on the DTO getters for snake_case field names.
 */
@Tag(name = "Person Details", description = "Voice actor and staff information API")
@RestController
@RequestMapping("/api/person_details")
@CrossOrigin(origins = "*")
public class PersonDetailsController {

    @Autowired
    private PersonDetailsService service;

    // ── GET /api/person_details/{person_mal_id} ───────────────────────────────

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

        // Jackson serialises PersonDetailsDTO to JSON automatically via @JsonProperty
        return ResponseEntity.ok(dto.get());
    }

    // ── GET /api/person_details/{person_mal_id}/details ──────────────────────

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

        // Returns Page<DetailsDTO> — Jackson serialises each DetailsDTO automatically
        Page<DetailsDTO> animePage = service.getAnimeWorksForPerson(person_mal_id, pageable);

        return ResponseEntity.ok(Map.of(
                "person_mal_id", person_mal_id,
                "page",          finalPage,
                "pageSize",      finalPageSize,
                "totalPages",    animePage.getTotalPages(),
                "totalItems",    animePage.getTotalElements(),
                "items",         animePage.getContent()));
    }

    // ── GET /api/person_details ───────────────────────────────────────────────

    @Operation(summary = "Get all people",
            description = "Retrieve paginated list of people with optional filters and sorting. " +
                    "NULL values are always sorted last.")
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

    // ── GET /api/person_details/stats ─────────────────────────────────────────

    @Operation(summary = "Get statistics", description = "Get total count of people in the database")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    // ── GET /api/person_details/stats/null_counts ─────────────────────────────

    @Operation(summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field " +
                    "(website_url, given_name, family_name, birthday, relevant_location)")
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
        return Sort.by(orders);
    }
}
