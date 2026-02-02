package com.example.dataserverspringboot.entities.persondetails;

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
import com.example.dataserverspringboot.entities.details.Details;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * REST API Controller for PersonDetails
 * Returns field names in snake_case to match database columns
 */
@Tag(name = "Person Details", description = "Voice actor and staff information API")
@RestController
@RequestMapping("/api/person_details")
@CrossOrigin(origins = "*")
public class PersonDetailsController {

    @Autowired
    private PersonDetailsService service;

    @Autowired
    private PersonDetailsRepository personDetailsRepository;

    @GetMapping("/{person_mal_id}")
    public ResponseEntity<?> getById(
            @PathVariable Integer person_mal_id,
            @RequestParam(required = false) String fields) {
        
        Optional<PersonDetails> entity = service.getById(person_mal_id);
        
        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PersonDetails not found");
            error.put("person_mal_id", person_mal_id);
            return ResponseEntity.status(404).body(error);
        }
        
        PersonDetails data = entity.get();
        
        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }
        
        // Return all fields with snake_case names
        return ResponseEntity.ok(toSnakeCaseMap(data));
    }

    @GetMapping("/{person_mal_id}/summary")
    public ResponseEntity<?> getSummary(@PathVariable Integer person_mal_id) {
        Optional<PersonDetails> entity = service.getById(person_mal_id);
        
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        
        PersonDetails data = entity.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("person_mal_id", data.getPersonMalId());
        summary.put("name", data.getName());
        summary.put("favorites", data.getFavorites());
        summary.put("image_url", data.getImageUrl());
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Convert Details entity to Map with snake_case field names
     */
    private Map<String, Object> detailsToSnakeCaseMap(com.example.dataserverspringboot.entities.details.Details entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("mal_id", entity.getMalId());
        result.put("title", entity.getTitle());
        result.put("title_japanese", entity.getTitleJapanese());
        result.put("type", entity.getType());
        result.put("score", entity.getScore());
        result.put("image_url", entity.getImageUrl());
        result.put("url", entity.getUrl());
        return result;
    }

    @GetMapping("/{person_mal_id}/details")
    @Operation(
            summary = "Get anime works",
            description = "Retrieve all anime this person has worked on (voice acting roles)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime list retrieved"),
            @ApiResponse(responseCode = "404", description = "Person not found", content = @Content)
    })
    public ResponseEntity<?> getAnimeWorks(
            @Parameter(description = "Person MAL ID", example = "1", required = true)
            @PathVariable Integer person_mal_id,

            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "Results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {

        // Check if person exists
        Optional<PersonDetails> personOpt = service.getById(person_mal_id);
        if (personOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Person not found");
            error.put("person_mal_id", person_mal_id);
            return ResponseEntity.status(404).body(error);
        }

        int finalPage = (page != null) ? page : 1;
        int finalPageSize = (pageSize != null) ? pageSize : 10;

        Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);
        Page<com.example.dataserverspringboot.entities.details.Details> animePage =
                personDetailsRepository.findAnimeWorks(person_mal_id, pageable);

        List<Map<String, Object>> animeList = animePage.getContent().stream()
                .map(this::detailsToSnakeCaseMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("person_mal_id", person_mal_id);
        response.put("page", finalPage);
        response.put("pageSize", finalPageSize);
        response.put("totalPages", animePage.getTotalPages());
        response.put("totalItems", animePage.getTotalElements());
        response.put("items", animeList);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
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
            Page<PersonDetails> pageResult = service.findWithFilters(
                search,
                nullFilter,
                notNullFilter,
                pageable);
            
            List<PersonDetails> results = pageResult.getContent();
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
            Page<PersonDetails> pageResult = service.findWithFilters(
                search,
                nullFilter,
                notNullFilter,
                pageable);
            
            List<PersonDetails> results = pageResult.getContent();
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
            Page<PersonDetails> pageResult = service.findWithFilters(
                search,
                nullFilter,
                notNullFilter,
                pageable);
            
            List<PersonDetails> results = pageResult.getContent();
            
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
     * GET /api/person_details/stats/null_counts
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
    private Map<String, Object> toSnakeCaseMap(PersonDetails entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("person_mal_id", entity.getPersonMalId());
        result.put("url", entity.getUrl());
        result.put("website_url", entity.getWebsiteUrl());
        result.put("image_url", entity.getImageUrl());
        result.put("name", entity.getName());
        result.put("given_name", entity.getGivenName());
        result.put("family_name", entity.getFamilyName());
        result.put("birthday", entity.getBirthday());
        result.put("favorites", entity.getFavorites());
        result.put("relevant_location", entity.getRelevantLocation());
        return result;
    }

    /**
     * Filter fields based on comma-separated field list
     * Field names use snake_case
     */
    private Map<String, Object> filterFields(PersonDetails entity, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");
        
        for (String field : requestedFields) {
            field = field.trim();
            switch (field) {
                case "person_mal_id":
                    result.put("person_mal_id", entity.getPersonMalId());
                    break;
                case "url":
                    result.put("url", entity.getUrl());
                    break;
                case "website_url":
                    result.put("website_url", entity.getWebsiteUrl());
                    break;
                case "image_url":
                    result.put("image_url", entity.getImageUrl());
                    break;
                case "name":
                    result.put("name", entity.getName());
                    break;
                case "given_name":
                    result.put("given_name", entity.getGivenName());
                    break;
                case "family_name":
                    result.put("family_name", entity.getFamilyName());
                    break;
                case "birthday":
                    result.put("birthday", entity.getBirthday());
                    break;
                case "favorites":
                    result.put("favorites", entity.getFavorites());
                    break;
                case "relevant_location":
                    result.put("relevant_location", entity.getRelevantLocation());
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
        orders.add(Sort.Order.asc("personMalId"));

        return Sort.by(orders);
    }
}
