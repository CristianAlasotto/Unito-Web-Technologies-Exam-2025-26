package com.example.dataserverspringboot.entities.details;

import com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks;
import com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorksRepository;
import com.example.dataserverspringboot.entities.characters.Characters;
import com.example.dataserverspringboot.entities.characters.CharactersRepository;
import com.example.dataserverspringboot.entities.personanimeworks.PersonAnimeWorks;
import com.example.dataserverspringboot.entities.personanimeworks.PersonAnimeWorksRepository;
import com.example.dataserverspringboot.entities.persondetails.PersonDetails;
import com.example.dataserverspringboot.entities.persondetails.PersonDetailsRepository;
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
 * REST API Controller for Details
 * Returns field names in snake_case to match database columns
 * Supports relation expansion via ?include parameter
 */
@RestController
@RequestMapping("/api/details")
@CrossOrigin(origins = "*")
public class DetailsController {

    @Autowired
    private DetailsService service;

    @Autowired
    private DetailsRepository detailsRepository;
    
    @Autowired
    private CharacterAnimeWorksRepository characterAnimeWorksRepository;
    
    @Autowired
    private CharactersRepository charactersRepository;
    
    @Autowired
    private PersonAnimeWorksRepository personAnimeWorksRepository;
    
    @Autowired
    private PersonDetailsRepository personDetailsRepository;

    @GetMapping("/{mal_id}")
    public ResponseEntity<?> getById(
            @PathVariable("mal_id") Integer malId,
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String include) {
        
        Optional<Details> entity = service.getById(malId);
        
        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Details not found");
            error.put("mal_id", malId);
            return ResponseEntity.status(404).body(error);
        }
        
        Details data = entity.get();
        
        // Handle relation expansion
        if (include != null && !include.isEmpty()) {
            Map<String, Object> result = toSnakeCaseMap(data);
            expandRelations(malId, include, result);
            
            // Apply field selection if requested
            if (fields != null && !fields.isEmpty()) {
                return ResponseEntity.ok(filterMapFields(result, fields));
            }
            
            return ResponseEntity.ok(result);
        }
        
        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }
        
        // Return all fields with snake_case names
        return ResponseEntity.ok(toSnakeCaseMap(data));
    }

    @GetMapping("/{mal_id}/summary")
    public ResponseEntity<?> getSummary(@PathVariable("mal_id") Integer malId) {
        Optional<Details> entity = service.getById(malId);
        
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        
        Details data = entity.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("mal_id", data.getMalId());
        summary.put("title", data.getTitle());
        summary.put("score", data.getScore());
        summary.put("popularity", data.getPopularity());
        
        return ResponseEntity.ok(summary);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String source,
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
            Page<Details> pageResult = service.findWithFilters(
                search,
                type,
                year,
                status,
                rating,
                source,
                nullFilter,       // NEW
                notNullFilter,    // NEW
                pageable);
            
            List<Details> results = pageResult.getContent();
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
            Page<Details> pageResult = service.findWithFilters(
                search,
                type,
                year,
                status,
                rating,
                source,
                nullFilter,       // NEW
                notNullFilter,    // NEW
                pageable);
            
            List<Details> results = pageResult.getContent();
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
            Page<Details> pageResult = service.findWithFilters(
                search,
                type,
                year,
                status,
                rating,
                source,
                nullFilter,       // NEW
                notNullFilter,    // NEW
                pageable);
            
            List<Details> results = pageResult.getContent();
            
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
     * GET /api/details/stats/null_counts
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
     * Expand relations based on include parameter
     * Supports: characters, staff
     */
    private void expandRelations(Integer malId, String include, Map<String, Object> result) {
        String[] relations = include.split(",");
        
        for (String relation : relations) {
            relation = relation.trim();
            
            if ("characters".equals(relation)) {
                List<Map<String, Object>> characters = getCharactersForAnime(malId);
                result.put("characters", characters);
            }
            else if ("staff".equals(relation)) {
                List<Map<String, Object>> staff = getStaffForAnime(malId);
                result.put("staff", staff);
            }
        }
    }
    
    /**
     * Get all characters for an anime
     */
    private List<Map<String, Object>> getCharactersForAnime(Integer malId) {
        List<Map<String, Object>> characters = new ArrayList<>();
        
        // Find all character_anime_works entries for this anime
        List<CharacterAnimeWorks> works = characterAnimeWorksRepository
            .findByAnimeMalId(malId, Pageable.unpaged())
            .getContent();
        
        for (CharacterAnimeWorks work : works) {
            Optional<Characters> character = charactersRepository.findById(work.getCharacterMalId());
            
            if (character.isPresent()) {
                Characters c = character.get();
                Map<String, Object> charMap = new HashMap<>();
                charMap.put("character_mal_id", c.getCharacterMalId());
                charMap.put("name", c.getName());
                charMap.put("name_kanji", c.getNameKanji());
                charMap.put("image", c.getImage());
                charMap.put("role", work.getRole());
                characters.add(charMap);
            }
        }
        
        return characters;
    }
    
    /**
     * Get all staff for an anime
     */
    private List<Map<String, Object>> getStaffForAnime(Integer malId) {
        List<Map<String, Object>> staff = new ArrayList<>();
        
        // Find all person_anime_works entries for this anime
        List<PersonAnimeWorks> works = personAnimeWorksRepository
            .findByAnimeMalId(malId, Pageable.unpaged())
            .getContent();
        
        for (PersonAnimeWorks work : works) {
            Optional<PersonDetails> person = personDetailsRepository.findById(work.getPersonMalId());
            
            if (person.isPresent()) {
                PersonDetails p = person.get();
                Map<String, Object> staffMap = new HashMap<>();
                staffMap.put("person_mal_id", p.getPersonMalId());
                staffMap.put("name", p.getName());
                staffMap.put("given_name", p.getGivenName());
                staffMap.put("family_name", p.getFamilyName());
                staffMap.put("position", work.getPosition());
                staff.add(staffMap);
            }
        }
        
        return staff;
    }

    /**
     * Convert entity to Map with snake_case field names
     */
    private Map<String, Object> toSnakeCaseMap(Details entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("mal_id", entity.getMalId());
        result.put("title", entity.getTitle());
        result.put("title_japanese", entity.getTitleJapanese());
        result.put("url", entity.getUrl());
        result.put("image_url", entity.getImageUrl());
        result.put("type", entity.getType());
        result.put("status", entity.getStatus());
        result.put("score", entity.getScore());
        result.put("scored_by", entity.getScoredBy());
        result.put("start_date", entity.getStartDate());
        result.put("end_date", entity.getEndDate());
        result.put("synopsis", entity.getSynopsis());
        result.put("rank", entity.getRank());
        result.put("popularity", entity.getPopularity());
        result.put("members", entity.getMembers());
        result.put("favorites", entity.getFavorites());
        result.put("genres", entity.getGenres());
        result.put("studios", entity.getStudios());
        result.put("themes", entity.getThemes());
        result.put("demographics", entity.getDemographics());
        result.put("source", entity.getSource());
        result.put("rating", entity.getRating());
        result.put("episodes", entity.getEpisodes());
        result.put("season", entity.getSeason());
        result.put("year", entity.getYear());
        result.put("producers", entity.getProducers());
        result.put("explicit_genres", entity.getExplicitGenres());
        result.put("licensors", entity.getLicensors());
        result.put("streaming", entity.getStreaming());
        return result;
    }

    /**
     * Filter fields based on comma-separated field list
     * Field names use snake_case
     */
    private Map<String, Object> filterFields(Details entity, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");
        
        for (String field : requestedFields) {
            field = field.trim();
            switch (field) {
                case "mal_id":
                    result.put("mal_id", entity.getMalId());
                    break;
                case "title":
                    result.put("title", entity.getTitle());
                    break;
                case "title_japanese":
                    result.put("title_japanese", entity.getTitleJapanese());
                    break;
                case "url":
                    result.put("url", entity.getUrl());
                    break;
                case "image_url":
                    result.put("image_url", entity.getImageUrl());
                    break;
                case "type":
                    result.put("type", entity.getType());
                    break;
                case "status":
                    result.put("status", entity.getStatus());
                    break;
                case "score":
                    result.put("score", entity.getScore());
                    break;
                case "scored_by":
                    result.put("scored_by", entity.getScoredBy());
                    break;
                case "start_date":
                    result.put("start_date", entity.getStartDate());
                    break;
                case "end_date":
                    result.put("end_date", entity.getEndDate());
                    break;
                case "synopsis":
                    result.put("synopsis", entity.getSynopsis());
                    break;
                case "rank":
                    result.put("rank", entity.getRank());
                    break;
                case "popularity":
                    result.put("popularity", entity.getPopularity());
                    break;
                case "members":
                    result.put("members", entity.getMembers());
                    break;
                case "favorites":
                    result.put("favorites", entity.getFavorites());
                    break;
                case "genres":
                    result.put("genres", entity.getGenres());
                    break;
                case "studios":
                    result.put("studios", entity.getStudios());
                    break;
                case "themes":
                    result.put("themes", entity.getThemes());
                    break;
                case "demographics":
                    result.put("demographics", entity.getDemographics());
                    break;
                case "source":
                    result.put("source", entity.getSource());
                    break;
                case "rating":
                    result.put("rating", entity.getRating());
                    break;
                case "episodes":
                    result.put("episodes", entity.getEpisodes());
                    break;
                case "season":
                    result.put("season", entity.getSeason());
                    break;
                case "year":
                    result.put("year", entity.getYear());
                    break;
                case "producers":
                    result.put("producers", entity.getProducers());
                    break;
                case "explicit_genres":
                    result.put("explicit_genres", entity.getExplicitGenres());
                    break;
                case "licensors":
                    result.put("licensors", entity.getLicensors());
                    break;
                case "streaming":
                    result.put("streaming", entity.getStreaming());
                    break;
            }
        }
        
        return result;
    }
    
    /**
     * Filter fields from a Map (used after relation expansion)
     */
    private Map<String, Object> filterMapFields(Map<String, Object> data, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");
        
        for (String field : requestedFields) {
            field = field.trim();
            if (data.containsKey(field)) {
                result.put(field, data.get(field));
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

        return Sort.by(orders);  // ← Now ALWAYS consistent!
    }

    /**
     * Get recommendations for a specific anime (JPA version)
     * GET /api/details/{mal_id}/recommendations
     */
    @GetMapping("/{mal_id}/recommendations")
    public ResponseEntity<?> getRecommendations(
            @PathVariable("mal_id") Integer malId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        
        // Check if anime exists
        Optional<Details> anime = service.getById(malId);
        if (anime.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Anime not found");
            error.put("mal_id", malId);
            return ResponseEntity.status(404).body(error);
        }
        
        // Pagination setup
        boolean usePageBased = (page != null || pageSize != null);
        
        if (usePageBased) {
            int finalPage = (page != null) ? page : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;
            
            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, Sort.by("score").descending());
            Page<Details> recommendationsPage = detailsRepository.findRecommendationsForAnime(malId, pageable);
            
            List<Map<String, Object>> recommendations = recommendationsPage.getContent().stream()
                .map(this::toSnakeCaseMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("anime", toSnakeCaseMap(anime.get()));
            response.put("page", finalPage);
            response.put("pageSize", finalPageSize);
            response.put("totalPages", recommendationsPage.getTotalPages());
            response.put("total", recommendationsPage.getTotalElements());
            response.put("recommendations", recommendations);
            return ResponseEntity.ok(response);
            
        } else {
            // Offset-based or default
            int finalLimit = (limit != null) ? limit : 10;
            int finalOffset = (offset != null) ? offset : 0;
            int pageNum = finalOffset / finalLimit;
            
            Pageable pageable = PageRequest.of(pageNum, finalLimit, Sort.by("score").descending());
            Page<Details> recommendationsPage = detailsRepository.findRecommendationsForAnime(malId, pageable);
            
            List<Map<String, Object>> recommendations = recommendationsPage.getContent().stream()
                .map(this::toSnakeCaseMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("anime", toSnakeCaseMap(anime.get()));
            
            if (limit != null || offset != null) {
                response.put("limit", finalLimit);
                response.put("offset", finalOffset);
            }
            
            response.put("total", recommendationsPage.getTotalElements());
            response.put("recommendations", recommendations);
            return ResponseEntity.ok(response);
        }
    }

}
