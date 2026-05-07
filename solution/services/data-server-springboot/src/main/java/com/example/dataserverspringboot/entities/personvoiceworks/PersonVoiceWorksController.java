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
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for PersonVoiceWorks
 * Returns field names in snake_case to match database columns
 */
@Tag(name = "Person Voice Works", description = "Anime voice actors with their roles and languages and relationships API")
@RestController
@RequestMapping("/api/person_voice_works")
@CrossOrigin(origins = "*")
public class PersonVoiceWorksController {

    @Autowired
    private PersonVoiceWorksService service;

    @Operation(
            summary = "Get specific voice work entry",
            description = "Retrieve a single voice acting record using the composite key (person_mal_id + character_mal_id + anime_mal_id)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found successfully", content = @Content(schema = @Schema(implementation = PersonVoiceWorks.class))),
            @ApiResponse(responseCode = "400", description = "Missing key fields", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Composite ID (format depends on Spring conversion)", required = true)
            @PathVariable PersonVoiceWorks.PersonVoiceWorksId id,

            @Parameter(description = "Comma-separated fields to return")
            @RequestParam(required = false) String fields) {

        Optional<PersonVoiceWorks> entity = service.getById(id);

        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PersonVoiceWorks not found");
            error.put("id", id);
            return ResponseEntity.status(404).body(error);
        }

        PersonVoiceWorks data = entity.get();

        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }

        return ResponseEntity.ok(toSnakeCaseMap(data));
    }

    @Operation(
            summary = "Get all voice works",
            description = "Retrieve paginated list of voice works with optional filters, sorting, and field selection. NULL values are always sorted last."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Comma-separated fields to return", example = "role,language")
            @RequestParam(required = false) String fields,

            @Parameter(description = "Search by language (case-insensitive)", example = "Japanese")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-role")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by Language", example = "Japanese")
            @RequestParam(required = false) String language,

            @Parameter(description = "Filter by Role", example = "Main")
            @RequestParam(required = false) String role,

            @Parameter(description = "Filter by Person MAL ID", example = "1")
            @RequestParam(required = false) Integer person_mal_id,

            @Parameter(description = "Filter by Character MAL ID", example = "1")
            @RequestParam(required = false) Integer character_mal_id,

            @Parameter(description = "Filter by Anime MAL ID", example = "1")
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

        boolean useLimitOffset = (limit != null || offset != null);
        boolean usePageBased = (page != null || pageSize != null);

        Sort sortObj = parseSortParameter(sort);

        if (useLimitOffset) {
            int finalLimit = (limit != null) ? limit : 10;
            int finalOffset = (offset != null) ? offset : 0;

            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit, sortObj);
            Page<PersonVoiceWorks> pageResult = service.findWithFilters(
                search,
                language,
                role,
                person_mal_id,
                character_mal_id,
                anime_mal_id,
                nullFilter,
                notNullFilter,
                pageable);

            List<PersonVoiceWorks> results = pageResult.getContent();
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
            Page<PersonVoiceWorks> pageResult = service.findWithFilters(
                search,
                language,
                role,
                person_mal_id,
                character_mal_id,
                anime_mal_id,
                nullFilter,
                notNullFilter,
                pageable);

            List<PersonVoiceWorks> results = pageResult.getContent();
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
            Page<PersonVoiceWorks> pageResult = service.findWithFilters(
                search,
                language,
                role,
                person_mal_id,
                character_mal_id,
                anime_mal_id,
                nullFilter,
                notNullFilter,
                pageable);

            List<PersonVoiceWorks> results = pageResult.getContent();

            if (fields != null && !fields.isEmpty()) {
                List<Map<String, Object>> filteredResults = results.stream()
                    .map(entity -> filterFields(entity, fields))
                    .collect(Collectors.toList());
                return ResponseEntity.ok(filteredResults);
            }

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
     * GET /api/person_voice_works/stats/null_counts
     */
    @Operation(
            summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field (role, language)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "NULL statistics retrieved successfully")
    })
    @GetMapping("/stats/null_counts")
    public ResponseEntity<Map<String, Object>> getNullCounts() {
        Map<String, Long> nullCounts = service.getNullCounts();

        Map<String, Object> response = new HashMap<>();
        response.put("null_counts", nullCounts);
        response.put("total_records", service.count());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toSnakeCaseMap(PersonVoiceWorks entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("person_mal_id", entity.getPersonMalId());
        result.put("character_mal_id", entity.getCharacterMalId());
        result.put("anime_mal_id", entity.getAnimeMalId());
        result.put("role", entity.getRole());
        result.put("language", entity.getLanguage());
        return result;
    }

    private Map<String, Object> filterFields(PersonVoiceWorks entity, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");

        for (String field : requestedFields) {
            field = field.trim();
            switch (field) {
                case "person_mal_id":
                    result.put("person_mal_id", entity.getPersonMalId());
                    break;
                case "character_mal_id":
                    result.put("character_mal_id", entity.getCharacterMalId());
                    break;
                case "anime_mal_id":
                    result.put("anime_mal_id", entity.getAnimeMalId());
                    break;
                case "role":
                    result.put("role", entity.getRole());
                    break;
                case "language":
                    result.put("language", entity.getLanguage());
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
        orders.add(Sort.Order.asc("personMalId"));
        orders.add(Sort.Order.asc("characterMalId"));
        orders.add(Sort.Order.asc("animeMalId"));

        return Sort.by(orders);
    }
}