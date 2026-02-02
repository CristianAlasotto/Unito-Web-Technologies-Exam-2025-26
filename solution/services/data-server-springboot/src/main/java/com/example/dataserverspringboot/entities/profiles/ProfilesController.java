package com.example.dataserverspringboot.entities.profiles;

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
@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*")
public class ProfilesController {

    @Autowired
    private ProfilesService service;

    @GetMapping("/{username}")
    public ResponseEntity<?> getById(
            @PathVariable("username") String username,
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

    @GetMapping("/{username}/summary")
    public ResponseEntity<?> getSummary(@PathVariable("username") String username) {
        Optional<Profiles> entity = service.getById(username);
        
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        
        Profiles data = entity.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("username", data.getUsername());
        summary.put("gender", data.getGender());
        summary.put("joined", data.getJoined());
        summary.put("watching", data.getWatching());
        summary.put("completed", data.getCompleted());
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String nullFilter,      // NEW: Filter for NULL values
            @RequestParam(required = false) String notNullFilter,   // NEW: Filter for NOT NULL values
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer page,
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
                nullFilter,       // NEW
                notNullFilter,    // NEW
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
                nullFilter,       // NEW
                notNullFilter,    // NEW
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
                nullFilter,       // NEW
                notNullFilter,    // NEW
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
                        .nullsLast());  // ← ADD THIS
            }
        }

        // Add primary keys as tiebreaker
        orders.add(Sort.Order.asc("username"));

        return Sort.by(orders);
    }
}
