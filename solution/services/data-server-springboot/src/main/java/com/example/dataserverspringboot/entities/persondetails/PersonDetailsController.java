package com.example.dataserverspringboot.entities.persondetails;

import com.example.dataserverspringboot.entities.details.Details;
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
 * REST API Controller for PersonDetails
 * Returns field names in snake_case to match database columns
 */
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
            @PathVariable("person_mal_id") Integer personMalId,
            @RequestParam(required = false) String fields) {
        
        Optional<PersonDetails> entity = service.getById(personMalId);
        
        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "PersonDetails not found");
            error.put("person_mal_id", personMalId);
            return ResponseEntity.status(404).body(error);
        }
        
        PersonDetails data = entity.get();
        
        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }
        
        return ResponseEntity.ok(toSnakeCaseMap(data));
    }

    @GetMapping("/{person_mal_id}/summary")
    public ResponseEntity<?> getSummary(@PathVariable("person_mal_id") Integer personMalId) {
        Optional<PersonDetails> entity = service.getById(personMalId);
        
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
     * JOIN 2: Get all anime this person worked on
     * GET /api/person_details/{person_mal_id}/anime
     */
    @GetMapping("/{person_mal_id}/details")
    public ResponseEntity<?> getAnimeWorks(
            @PathVariable("person_mal_id") Integer personMalId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        
        // Check if person exists
        Optional<PersonDetails> person = service.getById(personMalId);
        if (person.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Person not found");
            error.put("person_mal_id", personMalId);
            return ResponseEntity.status(404).body(error);
        }
        
        // Pagination setup
        boolean usePageBased = (page != null || pageSize != null);
        
        if (usePageBased) {
            int finalPage = (page != null) ? page : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;
            
            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);
            Page<Details> animePage = personDetailsRepository.findAnimeWorks(personMalId, pageable);
            
            List<Map<String, Object>> animeList = animePage.getContent().stream()
                .map(this::toDetailsSnakeCaseMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("person", toSnakeCaseMap(person.get()));
            response.put("page", finalPage);
            response.put("pageSize", finalPageSize);
            response.put("totalPages", animePage.getTotalPages());
            response.put("total", animePage.getTotalElements());
            response.put("anime", animeList);
            return ResponseEntity.ok(response);
            
        } else {
            int finalLimit = (limit != null) ? limit : 10;
            int finalOffset = (offset != null) ? offset : 0;
            int pageNum = finalOffset / finalLimit;
            
            Pageable pageable = PageRequest.of(pageNum, finalLimit);
            Page<Details> animePage = personDetailsRepository.findAnimeWorks(personMalId, pageable);
            
            List<Map<String, Object>> animeList = animePage.getContent().stream()
                .map(this::toDetailsSnakeCaseMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("person", toSnakeCaseMap(person.get()));
            
            if (limit != null || offset != null) {
                response.put("limit", finalLimit);
                response.put("offset", finalOffset);
            }
            
            response.put("total", animePage.getTotalElements());
            response.put("anime", animeList);
            return ResponseEntity.ok(response);
        }
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
        
        boolean usePageBased = (page != null || pageSize != null);
        boolean useLimitOffset = (limit != null || offset != null) && !usePageBased;
        
        Sort sortObj = parseSortParameter(sort);
        
        if (useLimitOffset) {
            int finalLimit = (limit != null) ? limit : 10;
            int finalOffset = (offset != null) ? offset : 0;
            
            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit, sortObj);
            Page<PersonDetails> pageResult = service.findWithFilters(search, pageable);
            
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
            Page<PersonDetails> pageResult = service.findWithFilters(search, pageable);
            
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
            Page<PersonDetails> pageResult = service.findWithFilters(search, pageable);
            
            List<PersonDetails> results = pageResult.getContent();
            
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
     * Convert PersonDetails entity to Map with snake_case field names
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
     * Convert Details entity to Map with snake_case field names
     */
    private Map<String, Object> toDetailsSnakeCaseMap(Details entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("mal_id", entity.getMalId());
        result.put("title", entity.getTitle());
        result.put("title_japanese", entity.getTitleJapanese());
        result.put("type", entity.getType());
        result.put("score", entity.getScore());
        result.put("image_url", entity.getImageUrl());
        result.put("episodes", entity.getEpisodes());
        result.put("year", entity.getYear());
        result.put("status", entity.getStatus());
        return result;
    }

    /**
     * Filter fields based on comma-separated field list
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
