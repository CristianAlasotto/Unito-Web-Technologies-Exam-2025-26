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
 * Composite key table - supports list operations only
 */
@RestController
@RequestMapping("/api/person_voice_works")
@CrossOrigin(origins = "*")
public class PersonVoiceWorksController {

    @Autowired
    private PersonVoiceWorksService service;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer personMalId,
            @RequestParam(required = false) Integer characterMalId,
            @RequestParam(required = false) Integer animeMalId,
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
            Page<PersonVoiceWorks> pageResult = service.findWithFilters(
                search,
                language, role, personMalId, characterMalId, animeMalId,
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
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;
            
            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<PersonVoiceWorks> pageResult = service.findWithFilters(
                search,
                language, role, personMalId, characterMalId, animeMalId,
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
                language, role, personMalId, characterMalId, animeMalId,
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
     * Get single resource by composite key (using query parameters)
     * GET /api/person_voice_works/single?person_mal_id&character_mal_id&anime_mal_id&language
     */
    @GetMapping("/single")
    public ResponseEntity<?> getSingle(
            @RequestParam(required = false) Integer personMalId,
            @RequestParam(required = false) Integer characterMalId,
            @RequestParam(required = false) Integer animeMalId,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String fields) {
        
        // Check if all key fields are provided
        if (personMalId == null || characterMalId == null || animeMalId == null || language == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "All key fields required: person_mal_id, character_mal_id, anime_mal_id, language");
            error.put("usage", "GET /api/person_voice_works/single?person_mal_id&character_mal_id&anime_mal_id&language");
            return ResponseEntity.status(400).body(error);
        }
        
        // Create composite key
        PersonVoiceWorks.PersonVoiceWorksId id = new PersonVoiceWorks.PersonVoiceWorksId(personMalId, characterMalId, animeMalId, language);
        Optional<PersonVoiceWorks> entity = service.getById(id);
        
        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PersonVoiceWorks not found");
            error.put("person_mal_id", personMalId); error.put("character_mal_id", characterMalId); error.put("anime_mal_id", animeMalId); error.put("language", language);
            return ResponseEntity.status(404).body(error);
        }
        
        PersonVoiceWorks data = entity.get();
        
        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }
        
        return ResponseEntity.ok(toSnakeCaseMap(data));
    }


    /**
     * Get summary by composite key (using query parameters)
     * GET /api/person_voice_works/summary?person_mal_id&character_mal_id&anime_mal_id&language
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam(required = false) Integer personMalId,
            @RequestParam(required = false) Integer characterMalId,
            @RequestParam(required = false) Integer animeMalId,
            @RequestParam(required = false) String language) {
        
        // Check if all key fields are provided
        if (personMalId == null || characterMalId == null || animeMalId == null || language == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "All key fields required: person_mal_id, character_mal_id, anime_mal_id, language");
            error.put("usage", "GET /api/person_voice_works/summary?person_mal_id&character_mal_id&anime_mal_id&language");
            return ResponseEntity.status(400).body(error);
        }
        
        // Create composite key
        PersonVoiceWorks.PersonVoiceWorksId id = new PersonVoiceWorks.PersonVoiceWorksId(personMalId, characterMalId, animeMalId, language);
        Optional<PersonVoiceWorks> entity = service.getById(id);
        
        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PersonVoiceWorks not found");
            error.put("person_mal_id", personMalId); error.put("character_mal_id", characterMalId); error.put("anime_mal_id", animeMalId); error.put("language", language);
            return ResponseEntity.status(404).body(error);
        }
        
        PersonVoiceWorks data = entity.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("person_mal_id", data.getPersonMalId());
        summary.put("character_mal_id", data.getCharacterMalId());
        summary.put("anime_mal_id", data.getAnimeMalId());
        summary.put("role", data.getRole());
        summary.put("language", data.getLanguage());
        
        return ResponseEntity.ok(summary);
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
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }
        
        String[] sortFields = sort.split(",");
        List<Sort.Order> orders = new ArrayList<>();
        
        for (String field : sortFields) {
            field = field.trim();
            if (field.startsWith("-")) {
                orders.add(Sort.Order.desc(field.substring(1)));
            } else {
                orders.add(Sort.Order.asc(field));
            }
        }
        
        return Sort.by(orders);
    }
}
