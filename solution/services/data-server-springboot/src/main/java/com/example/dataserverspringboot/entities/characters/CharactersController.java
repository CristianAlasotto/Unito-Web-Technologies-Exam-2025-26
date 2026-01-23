package com.example.dataserverspringboot.entities.characters;

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
 * REST API Controller for Characters
 * Returns field names in snake_case to match database columns
 */
@RestController
@RequestMapping("/api/characters")
@CrossOrigin(origins = "*")
public class CharactersController {

    @Autowired
    private CharactersService service;

    @GetMapping("/{character_mal_id}")
    public ResponseEntity<?> getById(
            @PathVariable Integer character_mal_id,
            @RequestParam(required = false) String fields) {
        
        Optional<Characters> entity = service.getById(character_mal_id);
        
        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Characters not found");
            error.put("character_mal_id", character_mal_id);
            return ResponseEntity.status(404).body(error);
        }
        
        Characters data = entity.get();
        
        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }
        
        // Return all fields with snake_case names
        return ResponseEntity.ok(toSnakeCaseMap(data));
    }

    @GetMapping("/{character_mal_id}/summary")
    public ResponseEntity<?> getSummary(@PathVariable Integer character_mal_id) {
        Optional<Characters> entity = service.getById(character_mal_id);
        
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        
        Characters data = entity.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("character_mal_id", data.getCharacterMalId());
        summary.put("name", data.getName());
        summary.put("favorites", data.getFavorites());
        summary.put("image", data.getImage());
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            
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
            Page<Characters> pageResult = service.findWithFilters(
                search,
                
                pageable);
            
            List<Characters> results = pageResult.getContent();
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
            Page<Characters> pageResult = service.findWithFilters(
                search,
                
                pageable);
            
            List<Characters> results = pageResult.getContent();
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
            Page<Characters> pageResult = service.findWithFilters(
                search,
                
                pageable);
            
            List<Characters> results = pageResult.getContent();
            
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
     * Convert entity to Map with snake_case field names
     */
    private Map<String, Object> toSnakeCaseMap(Characters entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("character_mal_id", entity.getCharacterMalId());
        result.put("url", entity.getUrl());
        result.put("name", entity.getName());
        result.put("name_kanji", entity.getNameKanji());
        result.put("image", entity.getImage());
        result.put("favorites", entity.getFavorites());
        result.put("about", entity.getAbout());
        return result;
    }

    /**
     * Filter fields based on comma-separated field list
     * Field names use snake_case
     */
    private Map<String, Object> filterFields(Characters entity, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");
        
        for (String field : requestedFields) {
            field = field.trim();
            switch (field) {
                case "character_mal_id":
                    result.put("character_mal_id", entity.getCharacterMalId());
                    break;
                case "url":
                    result.put("url", entity.getUrl());
                    break;
                case "name":
                    result.put("name", entity.getName());
                    break;
                case "name_kanji":
                    result.put("name_kanji", entity.getNameKanji());
                    break;
                case "image":
                    result.put("image", entity.getImage());
                    break;
                case "favorites":
                    result.put("favorites", entity.getFavorites());
                    break;
                case "about":
                    result.put("about", entity.getAbout());
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
