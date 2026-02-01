package com.example.dataserverspringboot.entities.personvoiceworks;

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
@RestController
@RequestMapping("/api/person_voice_works")
@CrossOrigin(origins = "*")
public class PersonVoiceWorksController {

    @Autowired
    private PersonVoiceWorksService service;

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable PersonVoiceWorks.PersonVoiceWorksId id,
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
        
        // Return all fields with snake_case names
        return ResponseEntity.ok(toSnakeCaseMap(data));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getSummary(@PathVariable PersonVoiceWorks.PersonVoiceWorksId id) {
        Optional<PersonVoiceWorks> entity = service.getById(id);
        
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        
        PersonVoiceWorks data = entity.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("person_mal_id", data.getPersonMalId());
        summary.put("character_mal_id", data.getCharacterMalId());
        summary.put("anime_mal_id", data.getAnimeMalId());
        summary.put("language", data.getLanguage());
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer person_mal_id,
            @RequestParam(required = false) Integer character_mal_id,
            @RequestParam(required = false) Integer anime_mal_id,
            @RequestParam(required = false) String nullFilter,
            @RequestParam(required = false) String notNullFilter,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer page,
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
     * GET /api/person_voice_works/stats/null_counts
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
    private Map<String, Object> toSnakeCaseMap(PersonVoiceWorks entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("person_mal_id", entity.getPersonMalId());
        result.put("character_mal_id", entity.getCharacterMalId());
        result.put("anime_mal_id", entity.getAnimeMalId());
        result.put("role", entity.getRole());
        result.put("language", entity.getLanguage());
        return result;
    }

    /**
     * Filter fields based on comma-separated field list
     * Field names use snake_case
     */
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
                if (field.startsWith("-")) {
                    orders.add(Sort.Order.desc(field.substring(1)));
                } else {
                    orders.add(Sort.Order.asc(field));
                }
            }
        }

        // CRITICAL FIX: Always add characterMalId as tiebreaker
        orders.add(Sort.Order.asc("personMalId"));
        orders.add(Sort.Order.asc("characterMalId"));
        orders.add(Sort.Order.asc("animeMalId"));


        return Sort.by(orders);  // ← Now ALWAYS consistent!
    }
}
