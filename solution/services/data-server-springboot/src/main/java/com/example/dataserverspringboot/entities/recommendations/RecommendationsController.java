package com.example.dataserverspringboot.entities.recommendations;

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
 * REST API Controller for Recommendations
 * Composite key table - supports list operations only
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationsController {

    @Autowired
    private RecommendationsService service;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer mal_id,
            @RequestParam(required = false) Integer recommendation_mal_id,
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
            Page<Recommendations> pageResult = service.findWithFilters(
                search,
                mal_id, recommendation_mal_id,
                pageable);
            
            List<Recommendations> results = pageResult.getContent();
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
            Page<Recommendations> pageResult = service.findWithFilters(
                search,
                mal_id, recommendation_mal_id,
                pageable);
            
            List<Recommendations> results = pageResult.getContent();
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
            Page<Recommendations> pageResult = service.findWithFilters(
                search,
                mal_id, recommendation_mal_id,
                pageable);
            
            List<Recommendations> results = pageResult.getContent();
            
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

    private Map<String, Object> toSnakeCaseMap(Recommendations entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("mal_id", entity.getMalId());
        result.put("recommendation_mal_id", entity.getRecommendationMalId());
        return result;
    }

    private Map<String, Object> filterFields(Recommendations entity, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");
        
        for (String field : requestedFields) {
            field = field.trim();
            switch (field) {
                case "mal_id":
                    result.put("mal_id", entity.getMalId());
                    break;
                case "recommendation_mal_id":
                    result.put("recommendation_mal_id", entity.getRecommendationMalId());
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
