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
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for Profiles
 * Returns field names in snake_case to match database columns
 */

@Tag(name = "Profile", description = "Website users and relationships API")
@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*")
public class ProfilesController {

    @Autowired
    private ProfilesService service;

    @Operation(
            summary = "Get profile by Username",
            description = "Retrieve a single user profile using their username"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found successfully", content = @Content(schema = @Schema(implementation = Profiles.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    @GetMapping("/{username}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Username (Primary Key)", example = "Xinil", required = true)
            @PathVariable("username") String username,

            @Parameter(description = "Comma-separated fields to return", example = "username,joined")
            @RequestParam(required = false) String fields) {

        Optional<Profiles> entity = service.getById(username);

        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Profiles not found");
            error.put("username", username);
            return ResponseEntity.status(404).body(error);
        }

        Profiles data = entity.get();

        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }

        // Return all fields with snake_case names
        return ResponseEntity.ok(toSnakeCaseMap(data));
    }

    @Operation(
            summary = "Get all profiles",
            description = "Retrieve paginated list of profiles with optional filters, sorting, and field selection. NULL values are always sorted last."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Comma-separated fields to return", example = "username,gender")
            @RequestParam(required = false) String fields,

            @Parameter(description = "Search by username (case-insensitive)", example = "Xinil")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-joined")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by Gender", example = "Male")
            @RequestParam(required = false) String gender,

            @Parameter(description = "Filter by Location", example = "California")
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

        boolean usePageBased = (page != null || pageSize != null);
        boolean useLimitOffset = (limit != null || offset != null) && !usePageBased;

        Sort sortObj = parseSortParameter(sort);

        if (useLimitOffset) {
            int finalLimit = (limit != null) ? limit : 10;
            int finalOffset = (offset != null) ? offset : 0;

            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit, sortObj);
            Page<Profiles> pageResult = service.findWithFilters(
                search,
                gender,
                location,
                nullFilter,
                notNullFilter,
                pageable);

            List<Profiles> results = pageResult.getContent();
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
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<Profiles> pageResult = service.findWithFilters(
                search,
                gender,
                location,
                nullFilter,
                notNullFilter,
                pageable);

            List<Profiles> results = pageResult.getContent();
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
            Page<Profiles> pageResult = service.findWithFilters(
                search,
                gender,
                location,
                nullFilter,
                notNullFilter,
                pageable);

            List<Profiles> results = pageResult.getContent();

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
     * GET /api/profiles/stats/null_counts
     */
    @Operation(
            summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field (gender, birthday, location)"
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

    /**
     * Convert entity to Map with snake_case field names
     */
    private Map<String, Object> toSnakeCaseMap(Profiles entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("username", entity.getUsername());
        result.put("gender", entity.getGender());
        result.put("birthday", entity.getBirthday());
        result.put("location", entity.getLocation());
        result.put("joined", entity.getJoined());
        result.put("watching", entity.getWatching());
        result.put("completed", entity.getCompleted());
        result.put("on_hold", entity.getOnHold());
        result.put("dropped", entity.getDropped());
        result.put("plan_to_watch", entity.getPlanToWatch());
        return result;
    }

    /**
     * Filter fields based on comma-separated field list
     * Field names use snake_case
     */
    private Map<String, Object> filterFields(Profiles entity, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");

        for (String field : requestedFields) {
            field = field.trim();
            switch (field) {
                case "username":
                    result.put("username", entity.getUsername());
                    break;
                case "gender":
                    result.put("gender", entity.getGender());
                    break;
                case "birthday":
                    result.put("birthday", entity.getBirthday());
                    break;
                case "location":
                    result.put("location", entity.getLocation());
                    break;
                case "joined":
                    result.put("joined", entity.getJoined());
                    break;
                case "watching":
                    result.put("watching", entity.getWatching());
                    break;
                case "completed":
                    result.put("completed", entity.getCompleted());
                    break;
                case "on_hold":
                    result.put("on_hold", entity.getOnHold());
                    break;
                case "dropped":
                    result.put("dropped", entity.getDropped());
                    break;
                case "plan_to_watch":
                    result.put("plan_to_watch", entity.getPlanToWatch());
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
        orders.add(Sort.Order.asc("username"));

        return Sort.by(orders);
    }
}