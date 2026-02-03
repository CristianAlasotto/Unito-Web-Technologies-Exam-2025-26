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
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for CharacterAnimeWorks
 * Returns field names in snake_case to match database columns
 */
@Tag(name = "Character Anime Works", description = "Anime characters with role and relationships API")
@RestController
@RequestMapping("/api/character_anime_works")
@CrossOrigin(origins = "*")
public class CharacterAnimeWorksController {

    @Autowired
    private CharacterAnimeWorksService service;

    @Operation(
            summary = "Get character work by Composite ID",
            description = "Retrieve a single character work entry using character ID and anime ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Entry found successfully",
                    content = @Content(schema = @Schema(implementation = CharacterAnimeWorks.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entry not found",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Composite ID (format depends on Spring conversion, typically JSON or comma-separated)", required = true)
            @PathVariable CharacterAnimeWorks.CharacterAnimeWorksId id,

            @Parameter(description = "Comma-separated list of fields to return", example = "role,character_name")
            @RequestParam(required = false) String fields) {

        Optional<CharacterAnimeWorks> entity = service.getById(id);

        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "CharacterAnimeWorks not found");
            error.put("id", id);
            return ResponseEntity.status(404).body(error);
        }

        CharacterAnimeWorks data = entity.get();

        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }

        // Return all fields with snake_case names
        return ResponseEntity.ok(toSnakeCaseMap(data));
    }

    @Operation(
            summary = "Get work summary",
            description = "Retrieve a brief summary of the character's role in an anime"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Summary retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entry not found",
                    content = @Content
            )
    })
    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getSummary(
            @Parameter(description = "Composite ID", required = true)
            @PathVariable CharacterAnimeWorks.CharacterAnimeWorksId id) {
        Optional<CharacterAnimeWorks> entity = service.getById(id);

        if (entity.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        CharacterAnimeWorks data = entity.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("character_mal_id", data.getCharacterMalId());
        summary.put("anime_mal_id", data.getAnimeMalId());
        summary.put("character_name", data.getCharacterName());
        summary.put("role", data.getRole());

        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Get all character works",
            description = "Retrieve paginated list of character works with optional filters, sorting, and field selection. NULL values are always sorted last."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameters",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Comma-separated fields to return", example = "role,character_name")
            @RequestParam(required = false) String fields,
            @Parameter(description = "Search by role (case-insensitive)", example = "Main")
            @RequestParam(required = false) String search,
            @Parameter(description = "Sort field (prefix with - for descending)", example = "-role")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Filter by specific Role", example = "Supporting")
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by Character MAL ID", example = "1")
            @RequestParam(required = false) Integer character_mal_id,
            @Parameter(description = "Filter by Anime MAL ID", example = "1")
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

        boolean useLimitOffset = (limit != null || offset != null);
        boolean usePageBased = (page != null || pageSize != null);

        Sort sortObj = parseSortParameter(sort);

        if (useLimitOffset) {
            int finalLimit = (limit != null) ? limit : 10;
            int finalOffset = (offset != null) ? offset : 0;

            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit, sortObj);
            Page<CharacterAnimeWorks> pageResult = service.findWithFilters(
                search,
                role,
                character_mal_id,
                anime_mal_id,
                nullFilter,
                notNullFilter,
                pageable);

            List<CharacterAnimeWorks> results = pageResult.getContent();
            long totalCount = pageResult.getTotalElements();

            if (fields != null && !fields.isEmpty()) {
                List<Map<String, Object>> filteredResults = results.stream()
                    .map(entity -> filterFields(entity, fields))
                    .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("limit", finalLimit);
                response.put("offset", finalOffset);
                response.put("total", totalCount);
                response.put("items", filteredResults);
                return ResponseEntity.ok(response);
            }

            // Convert all entities to snake_case
            List<Map<String, Object>> snakeCaseResults = results.stream()
                .map(this::toSnakeCaseMap)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("limit", finalLimit);
            response.put("offset", finalOffset);
            response.put("total", totalCount);
            response.put("items", snakeCaseResults);
            return ResponseEntity.ok(response);

        } else if (usePageBased) {
            int finalPage = (page != null) ? page : 1;
            int finalPageSize = (pageSize != null) ? pageSize : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<CharacterAnimeWorks> pageResult = service.findWithFilters(
                search,
                role,
                character_mal_id,
                anime_mal_id,
                nullFilter,
                notNullFilter,
                pageable);

            List<CharacterAnimeWorks> results = pageResult.getContent();
            long totalPages = pageResult.getTotalPages();

            if (fields != null && !fields.isEmpty()) {
                List<Map<String, Object>> filteredResults = results.stream()
                    .map(entity -> filterFields(entity, fields))
                    .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("page", finalPage);
                response.put("pageSize", finalPageSize);
                response.put("totalPages", totalPages);
                response.put("items", filteredResults);
                return ResponseEntity.ok(response);
            }

            // Convert all entities to snake_case
            List<Map<String, Object>> snakeCaseResults = results.stream()
                .map(this::toSnakeCaseMap)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("page", finalPage);
            response.put("pageSize", finalPageSize);
            response.put("totalPages", totalPages);
            response.put("items", snakeCaseResults);
            return ResponseEntity.ok(response);

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<CharacterAnimeWorks> pageResult = service.findWithFilters(
                search,
                role,
                character_mal_id,
                anime_mal_id,
                nullFilter,
                notNullFilter,
                pageable);

            List<CharacterAnimeWorks> results = pageResult.getContent();

            if (fields != null && !fields.isEmpty()) {
                List<Map<String, Object>> filteredResults = results.stream()
                    .map(entity -> filterFields(entity, fields))
                    .collect(Collectors.toList());
                return ResponseEntity.ok(filteredResults);
            }

            // Convert all entities to snake_case
            List<Map<String, Object>> snakeCaseResults = results.stream()
                .map(this::toSnakeCaseMap)
                .collect(Collectors.toList());

            return ResponseEntity.ok(snakeCaseResults);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", service.count());
        return ResponseEntity.ok(stats);
    }

    /**
     * Get statistics on NULL values for various fields
     * GET /api/character_anime_works/stats/null_counts
     */
    @Operation(
            summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field (character_name, role)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "NULL statistics retrieved successfully"
            )
    })
    @GetMapping("/stats/null_counts")
    public ResponseEntity<Map<String, Object>> getNullCounts() {
        Map<String, Long> nullCounts = service.getNullCounts();

        Map<String, Object> response = new HashMap<>();
        response.put("null_counts", nullCounts);
        response.put("total_records", service.count());

        return ResponseEntity.ok(response);
    }

    /**
     * Convert entity to Map with snake_case field names
     */
    private Map<String, Object> toSnakeCaseMap(CharacterAnimeWorks entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("character_mal_id", entity.getCharacterMalId());
        result.put("anime_mal_id", entity.getAnimeMalId());
        result.put("character_name", entity.getCharacterName());
        result.put("role", entity.getRole());
        return result;
    }

    /**
     * Filter fields based on comma-separated field list
     * Field names use snake_case
     */
    private Map<String, Object> filterFields(CharacterAnimeWorks entity, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");

        for (String field : requestedFields) {
            field = field.trim();
            switch (field) {
                case "character_mal_id":
                    result.put("character_mal_id", entity.getCharacterMalId());
                    break;
                case "anime_mal_id":
                    result.put("anime_mal_id", entity.getAnimeMalId());
                    break;
                case "character_name":
                    result.put("character_name", entity.getCharacterName());
                    break;
                case "role":
                    result.put("role", entity.getRole());
                    break;
            }
        }

        return result;
    }

    private Sort parseSortParameter(String sort) {
        List<Sort.Order> orders = new ArrayList<>();

        if (sort != null && !sort.isEmpty()) {
            String[] sortFields = sort.split(",");

            for (String field : sortFields) {
                field = field.trim();
                Sort.Direction direction;
                String actualField;

                if (field.startsWith("-")) {
                    direction = Sort.Direction.DESC;
                    actualField = field.substring(1);
                } else {
                    direction = Sort.Direction.ASC;
                    actualField = field;
                }

                // ✅ FIX: NULL values always sorted LAST
                orders.add(Sort.Order.by(actualField)
                        .with(direction)
                        .nullsLast());
            }
        }

        // Add primary keys as tiebreaker
        orders.add(Sort.Order.asc("characterMalId"));
        orders.add(Sort.Order.asc("animeMalId"));

        return Sort.by(orders);
    }
}