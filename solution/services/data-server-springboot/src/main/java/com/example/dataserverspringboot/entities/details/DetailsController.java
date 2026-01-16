package com.example.dataserverspringboot.entities.details;

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
 * REST API Controller for Details (Anime) following API conventions
 * Implements all patterns from Formati_API_REST_e_Formati_JSON.pdf
 */
@RestController
@RequestMapping("/api/anime")
@CrossOrigin(origins = "*")
public class DetailsController {

    @Autowired
    private DetailsService service;

    // ============================================
    // 1. Recupero risorsa singola
    // GET /api/anime/{id}
    // ============================================
    @GetMapping("/{mal_id}")
    public ResponseEntity<?> getAnimeById(
            @PathVariable Integer mal_id,
            @RequestParam(required = false) String fields,
            @RequestParam(required = false) String include) {

        Optional<Details> anime = service.getAnimeById(mal_id);

        if (anime.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Anime not found");
            error.put("anime_id", mal_id);
            return ResponseEntity.status(404).body(error);
        }

        Details animeData = anime.get();

        // 2. Field selection
        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(animeData, fields);
            return ResponseEntity.ok(filtered);
        }

        // 9. Espansione relazioni (placeholder for future implementation)
        if (include != null && !include.isEmpty()) {
            Map<String, Object> expanded = expandRelations(animeData, include);
            return ResponseEntity.ok(expanded);
        }

        return ResponseEntity.ok(animeData);
    }

    // ============================================
    // 10. Vista ridotta (summary)
    // GET /api/anime/{id}/summary
    // ============================================
    @GetMapping("/{mal_id}/summary")
    public ResponseEntity<?> getAnimeSummary(@PathVariable Integer mal_id) {
        Optional<Details> anime = service.getAnimeById(mal_id);

        if (anime.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        Details animeData = anime.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("anime_id", animeData.getMal_id());
        summary.put("title", animeData.getTitle());
        summary.put("score", animeData.getScore());
        summary.put("popularity", animeData.getPopularity());

        return ResponseEntity.ok(summary);
    }

    // ============================================
    // 3. Lista risorse
    // 4. Filtraggio
    // 5. Ricerca testuale
    // 6. Ordinamento
    // 7. Paginazione (limit/offset)
    // 8. Paginazione (page/pageSize)
    // GET /api/anime
    // ============================================
    @GetMapping
    public ResponseEntity<?> getAllAnime(
            // Field selection
            @RequestParam(required = false) String fields,

            // 4. Filtraggio
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String source,

            // 5. Ricerca testuale
            @RequestParam(required = false) String search,

            // 6. Ordinamento
            @RequestParam(required = false) String sort,

            // 7. Paginazione (limit/offset)
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,

            // 8. Paginazione (page/pageSize)
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {

        // Determine pagination strategy
        boolean useLimitOffset = (limit != null || offset != null);
        boolean usePageBased = (page != null || pageSize != null);

        // Default values
        int defaultLimit = 10;
        int defaultOffset = 0;
        int defaultPage = 1;
        int defaultPageSize = 10;

        // Build query with filters
        List<Details> results;
        long totalCount;

        // Create Sort object from sort parameter
        Sort sortObj = parseSortParameter(sort);

        if (useLimitOffset) {
            // 7. Paginazione limit/offset
            int finalLimit = (limit != null) ? limit : defaultLimit;
            int finalOffset = (offset != null) ? offset : defaultOffset;

            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit, sortObj);
            Page<Details> pageResult = service.findWithFilters(type, year, status, rating, source, search, pageable);

            results = pageResult.getContent();
            totalCount = pageResult.getTotalElements();

            // Apply field selection if requested
            if (fields != null && !fields.isEmpty()) {
                List<Map<String, Object>> filteredResults = results.stream()
                        .map(anime -> filterFields(anime, fields))
                        .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("limit", finalLimit);
                response.put("offset", finalOffset);
                response.put("total", totalCount);
                response.put("items", filteredResults);
                return ResponseEntity.ok(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("limit", finalLimit);
            response.put("offset", finalOffset);
            response.put("total", totalCount);
            response.put("items", results);

            return ResponseEntity.ok(response);

        } else if (usePageBased) {
            // 8. Paginazione page/pageSize
            int finalPage = (page != null) ? page : defaultPage;
            int finalPageSize = (pageSize != null) ? pageSize : defaultPageSize;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<Details> pageResult = service.findWithFilters(type, year, status, rating, source, search, pageable);

            results = pageResult.getContent();
            long totalPages = pageResult.getTotalPages();

            // Apply field selection if requested
            if (fields != null && !fields.isEmpty()) {
                List<Map<String, Object>> filteredResults = results.stream()
                        .map(anime -> filterFields(anime, fields))
                        .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("page", finalPage);
                response.put("pageSize", finalPageSize);
                response.put("totalPages", totalPages);
                response.put("items", filteredResults);
                return ResponseEntity.ok(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("page", finalPage);
            response.put("pageSize", finalPageSize);
            response.put("totalPages", totalPages);
            response.put("items", results);

            return ResponseEntity.ok(response);

        } else {
            // 3. Simple list (default: first 10 results)
            Pageable pageable = PageRequest.of(0, defaultLimit, sortObj);
            Page<Details> pageResult = service.findWithFilters(type, year, status, rating, source, search, pageable);

            results = pageResult.getContent();

            // Apply field selection if requested
            if (fields != null && !fields.isEmpty()) {
                List<Map<String, Object>> filteredResults = results.stream()
                        .map(anime -> filterFields(anime, fields))
                        .collect(Collectors.toList());
                return ResponseEntity.ok(filteredResults);
            }

            return ResponseEntity.ok(results);
        }
    }

    // ============================================
    // Statistics endpoint
    // ============================================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAnime", service.countAllAnime());
        stats.put("totalCharacters", 0);
        stats.put("totalUsers", 0);
        return ResponseEntity.ok(stats);
    }

    // ============================================
    // Helper Methods
    // ============================================

    /**
     * 2. Field selection - Filter object to only include requested fields
     */
    private Map<String, Object> filterFields(Details anime, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");

        for (String field : requestedFields) {
            field = field.trim();
            switch (field) {
                case "anime_id":
                case "id":
                case "mal_id":
                    result.put("anime_id", anime.getMal_id());
                    break;
                case "title":
                    result.put("title", anime.getTitle());
                    break;
                case "title_japanese":
                    result.put("title_japanese", anime.getTitle_japanese());
                    break;
                case "type":
                    result.put("type", anime.getType());
                    break;
                case "source":
                    result.put("source", anime.getSource());
                    break;
                case "episodes":
                    result.put("episodes", anime.getEpisodes());
                    break;
                case "status":
                    result.put("status", anime.getStatus());
                    break;
                case "start_date":
                    result.put("start_date", anime.getStart_date());
                    break;
                case "end_date":
                    result.put("end_date", anime.getEnd_date());
                    break;
                case "score":
                    result.put("score", anime.getScore());
                    break;
                case "scored_by":
                    result.put("scored_by", anime.getScored_by());
                    break;
                case "rank":
                    result.put("rank", anime.getRank());
                    break;
                case "popularity":
                    result.put("popularity", anime.getPopularity());
                    break;
                case "members":
                    result.put("members", anime.getMembers());
                    break;
                case "favorites":
                    result.put("favorites", anime.getFavorites());
                    break;
                case "synopsis":
                    result.put("synopsis", anime.getSynopsis());
                    break;
                case "year":
                    result.put("year", anime.getYear());
                    break;
                case "rating":
                    result.put("rating", anime.getRating());
                    break;
                case "studios":
                    result.put("studios", anime.getStudios());
                    break;
                case "genres":
                    result.put("genres", anime.getGenres());
                    break;
                case "themes":
                    result.put("themes", anime.getThemes());
                    break;
                case "demographics":
                    result.put("demographics", anime.getDemographics());
                    break;
                case "producers":
                    result.put("producers", anime.getProducers());
                    break;
                case "licensors":
                    result.put("licensors", anime.getLicensors());
                    break;
                case "streaming":
                    result.put("streaming", anime.getStreaming());
                    break;
                case "image_url":
                    result.put("image_url", anime.getImage_url());
                    break;
                case "url":
                    result.put("url", anime.getUrl());
                    break;
            }
        }

        return result;
    }

    /**
     * 6. Parse sort parameter
     * Format: "-score" (descending) or "score" (ascending)
     */
    private Sort parseSortParameter(String sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }

        String[] sortFields = sort.split(",");
        List<Sort.Order> orders = new ArrayList<>();

        for (String field : sortFields) {
            field = field.trim();
            if (field.startsWith("-")) {
                // Descending
                String fieldName = field.substring(1);
                orders.add(Sort.Order.desc(mapSortField(fieldName)));
            } else {
                // Ascending
                orders.add(Sort.Order.asc(mapSortField(field)));
            }
        }

        return Sort.by(orders);
    }

    /**
     * Map API field names to database column names
     */
    private String mapSortField(String field) {
        switch (field) {
            case "anime_id":
            case "id":
                return "mal_id";
            case "title":
                return "title";
            case "score":
                return "score";
            case "year":
                return "year";
            case "popularity":
                return "popularity";
            case "rank":
                return "rank";
            case "members":
                return "members";
            case "favorites":
                return "favorites";
            default:
                return field;
        }
    }

    /**
     * 9. Espansione relazioni (placeholder)
     * Future: expand to include characters, staff, etc.
     */
    private Map<String, Object> expandRelations(Details anime, String include) {
        Map<String, Object> result = new HashMap<>();
        result.put("anime_id", anime.getMal_id());
        result.put("title", anime.getTitle());
        result.put("score", anime.getScore());
        result.put("type", anime.getType());

        String[] relations = include.split(",");
        for (String relation : relations) {
            relation = relation.trim();
            switch (relation) {
                case "characters":
                    // TODO: Implement characters expansion
                    result.put("characters", new ArrayList<>());
                    break;
                case "staff":
                    // TODO: Implement staff expansion
                    result.put("staff", new ArrayList<>());
                    break;
                case "reviews":
                    // TODO: Implement reviews expansion
                    result.put("reviews", new ArrayList<>());
                    break;
            }
        }

        return result;
    }
}