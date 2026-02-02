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

    @Autowired
    private RecommendationsRepository recommendationsRepository;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(value = "mal_id", required = false) Integer malId,
            @RequestParam(value = "recommendation_mal_id", required = false) Integer recommendationMalId,
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
            Page<Recommendations> pageResult = service.findWithFilters(
                search,
                malId, recommendationMalId,
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
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;
            
            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<Recommendations> pageResult = service.findWithFilters(
                search,
                malId, recommendationMalId,
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
                malId, recommendationMalId,
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


    /**
     * Get single resource by composite key (using query parameters)
     * GET /api/recommendations/single?mal_id&recommendation_mal_id
     */
    @GetMapping("/single")
    public ResponseEntity<?> getSingle(
            @RequestParam(value = "mal_id", required = false) Integer malId,
            @RequestParam(value = "recommendation_mal_id", required = false) Integer recommendationMalId,
            @RequestParam(required = false) String fields) {
        
        // Check if all key fields are provided
        if (malId == null || recommendationMalId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "All key fields required: malId, recommendationMalId");
            error.put("usage", "GET /api/recommendations/single?mal_id&recommendation_mal_id");
            return ResponseEntity.status(400).body(error);
        }
        
        // Create composite key
        Recommendations.RecommendationsId id = new Recommendations.RecommendationsId(malId, recommendationMalId);
        Optional<Recommendations> entity = service.getById(id);
        
        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Recommendations not found");
            error.put("mal_id", malId); error.put("recommendation_mal_id", recommendationMalId);
            return ResponseEntity.status(404).body(error);
        }
        
        Recommendations data = entity.get();
        
        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }
        
        return ResponseEntity.ok(toSnakeCaseMap(data));
    }


    /**
     * Get summary by composite key (using query parameters)
     * GET /api/recommendations/summary?mal_id&recommendation_mal_id
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @RequestParam(value = "mal_id", required = false) Integer malId,
            @RequestParam(value = "recommendation_mal_id", required = false) Integer recommendationMalId) {
        
        // Check if all key fields are provided
        if (malId == null || recommendationMalId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "All key fields required: malId, recommendationMalId");
            error.put("usage", "GET /api/recommendations/summary?mal_id&recommendation_mal_id");
            return ResponseEntity.status(400).body(error);
        }
        
        // Create composite key
        Recommendations.RecommendationsId id = new Recommendations.RecommendationsId(malId, recommendationMalId);
        Optional<Recommendations> entity = service.getById(id);
        
        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Recommendations not found");
            error.put("mal_id", malId); error.put("recommendation_mal_id", recommendationMalId);
            return ResponseEntity.status(404).body(error);
        }
        
        Recommendations data = entity.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("mal_id", data.getMalId());
        summary.put("recommendation_mal_id", data.getRecommendationMalId());
        
        return ResponseEntity.ok(summary);
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

        // CRITICAL FIX: Always add primaryKeys as tiebreaker
        orders.add(Sort.Order.asc("malId"));
        orders.add(Sort.Order.asc("reccommendationMalId"));

        return Sort.by(orders);  // ← Now ALWAYS consistent!
    }

    /**
     * Get all recommendations with full anime details (JPA version)
     * GET /api/recommendations/details
     */
    @GetMapping("/details")
    public ResponseEntity<?> getAllWithDetails(
            @RequestParam(value = "mal_id", required = false) Integer malId,
            @RequestParam(value = "recommendation_mal_id", required = false) Integer recommendationMalId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        
        boolean usePageBased = (page != null || pageSize != null);
        
        if (usePageBased) {
            int finalPage = (page != null) ? page : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;
            
            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);
            Page<RecommendationWithDetails> results = service.findAllWithDetails(malId, recommendationMalId, pageable);
            
            // Convert to response format
            List<Map<String, Object>> items = results.getContent().stream()
                .map(this::convertToDetailsMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("page", finalPage);
            response.put("pageSize", finalPageSize);
            response.put("totalPages", results.getTotalPages());
            response.put("total", results.getTotalElements());
            response.put("items", items);
            return ResponseEntity.ok(response);
            
        } else {
            int finalLimit = (limit != null) ? limit : 10;
            int finalOffset = (offset != null) ? offset : 0;
            int pageNum = finalOffset / finalLimit;
            
            Pageable pageable = PageRequest.of(pageNum, finalLimit);
            Page<RecommendationWithDetails> results = service.findAllWithDetails(malId, recommendationMalId, pageable);
            
            List<Map<String, Object>> items = results.getContent().stream()
                .map(this::convertToDetailsMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            
            if (limit != null || offset != null) {
                response.put("limit", finalLimit);
                response.put("offset", finalOffset);
            }
            
            response.put("total", results.getTotalElements());
            response.put("items", items);
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Helper method to convert RecommendationWithDetails to Map with snake_case and flattened structure
     */
    private Map<String, Object> convertToDetailsMap(RecommendationWithDetails dto) {
        Map<String, Object> result = new HashMap<>();
        result.put("mal_id", dto.getMalId());
        result.put("recommendation_mal_id", dto.getRecommendationMalId());
        
        // Add source anime details with "source_" prefix
        if (dto.getSourceAnime() != null) {
            com.example.dataserverspringboot.entities.details.Details source = dto.getSourceAnime();
            result.put("source_title", source.getTitle());
            result.put("source_title_japanese", source.getTitleJapanese());
            result.put("source_type", source.getType());
            result.put("source_score", source.getScore());
            result.put("source_image_url", source.getImageUrl());
            result.put("source_episodes", source.getEpisodes());
            result.put("source_year", source.getYear());
            result.put("source_status", source.getStatus());
        }
        
        // Add recommended anime details with "recommendation_" prefix
        if (dto.getRecommendedAnime() != null) {
            com.example.dataserverspringboot.entities.details.Details recommended = dto.getRecommendedAnime();
            result.put("recommendation_title", recommended.getTitle());
            result.put("recommendation_title_japanese", recommended.getTitleJapanese());
            result.put("recommendation_type", recommended.getType());
            result.put("recommendation_score", recommended.getScore());
            result.put("recommendation_image_url", recommended.getImageUrl());
            result.put("recommendation_episodes", recommended.getEpisodes());
            result.put("recommendation_year", recommended.getYear());
            result.put("recommendation_status", recommended.getStatus());
        }
        
        return result;
    }

}
