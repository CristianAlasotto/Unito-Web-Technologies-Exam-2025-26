package com.example.dataserverspringboot.entities.characters;

import com.example.dataserverspringboot.entities.details.Details;
import com.example.dataserverspringboot.entities.persondetails.PersonDetails;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * REST API Controller for Characters
 * Returns field names in snake_case to match database columns
 */
@Tag(name = "Characters", description = "Anime characters and relationships API")
@RestController
@RequestMapping("/api/characters")
@CrossOrigin(origins = "*")
public class CharactersController {

    @Autowired
    private CharactersService service;

    @Autowired
    private CharactersRepository charactersRepository;

    @Operation(
            summary = "Get character by ID",
            description = "Retrieve a single anime character by their MyAnimeList character ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Character found successfully",
                    content = @Content(schema = @Schema(implementation = Characters.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Character not found",
                    content = @Content
            )
    })
    @GetMapping("/{character_mal_id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Character MAL ID", example = "1", required = true)
            @PathVariable("character_mal_id") Integer characterMalId,

            @Parameter(description = "Comma-separated list of fields to return", example = "name,favorites")
            @RequestParam(required = false) String fields) {
        
        Optional<Characters> entity = service.getById(characterMalId);
        
        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Characters not found");
            error.put("character_mal_id", characterMalId);
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

    @Operation(
            summary = "Get character summary",
            description = "Retrieve a brief summary of character information"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Summary retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Character not found",
                    content = @Content
            )
    })
    @GetMapping("/{character_mal_id}/summary")
    public ResponseEntity<?> getSummary(
            @Parameter(description = "Character MAL ID", example = "1", required = true)
            @PathVariable("character_mal_id") Integer characterMalId) {
        Optional<Characters> entity = service.getById(characterMalId);
        
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

    /**
     * JOIN 3: Get all anime where this character appears
     * GET /api/characters/{character_mal_id}/anime
     */
    @Operation(
            summary = "Get anime appearances",
            description = "Retrieve all anime where this character appears, sorted by score (highest first)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Anime list retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Character not found",
                    content = @Content
            )
    })
    @GetMapping("/{character_mal_id}/details")
    public ResponseEntity<?> getAnimeAppearances(
            @Parameter(description = "Character MAL ID", example = "1", required = true)
            @PathVariable("character_mal_id") Integer characterMalId,
            @Parameter(description = "Number of returned records", example = "10")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Number of deviation from the first record", example = "10")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {
        
        // Check if character exists
        Optional<Characters> character = service.getById(characterMalId);
        if (character.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Character not found");
            error.put("character_mal_id", characterMalId);
            return ResponseEntity.status(404).body(error);
        }
        
        // Pagination setup
        boolean usePageBased = (page != null || pageSize != null);
        
        if (usePageBased) {
            int finalPage = (page != null) ? page : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;
            
            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);
            Page<Details> animePage = charactersRepository.findAnimeAppearances(characterMalId, pageable);
            
            List<Map<String, Object>> animeList = animePage.getContent().stream()
                .map(this::toDetailsSnakeCaseMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("character", toSnakeCaseMap(character.get()));
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
            Page<Details> animePage = charactersRepository.findAnimeAppearances(characterMalId, pageable);
            
            List<Map<String, Object>> animeList = animePage.getContent().stream()
                .map(this::toDetailsSnakeCaseMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("character", toSnakeCaseMap(character.get()));
            
            if (limit != null || offset != null) {
                response.put("limit", finalLimit);
                response.put("offset", finalOffset);
            }
            
            response.put("total", animePage.getTotalElements());
            response.put("anime", animeList);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * JOIN 4: Get all voice actors for this character
     * GET /api/characters/{character_mal_id}/voice_actors
     */
    @GetMapping("/{character_mal_id}/voice_actors")
    public ResponseEntity<?> getVoiceActors(
            @PathVariable("character_mal_id") Integer characterMalId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        
        // Check if character exists
        Optional<Characters> character = service.getById(characterMalId);
        if (character.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Character not found");
            error.put("character_mal_id", characterMalId);
            return ResponseEntity.status(404).body(error);
        }
        
        // Pagination setup
        boolean usePageBased = (page != null || pageSize != null);
        
        if (usePageBased) {
            int finalPage = (page != null) ? page : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;
            
            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);
            Page<PersonDetails> voiceActorsPage = charactersRepository.findVoiceActors(characterMalId, pageable);
            
            List<Map<String, Object>> voiceActors = voiceActorsPage.getContent().stream()
                .map(this::toPersonDetailsSnakeCaseMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("character", toSnakeCaseMap(character.get()));
            response.put("page", finalPage);
            response.put("pageSize", finalPageSize);
            response.put("totalPages", voiceActorsPage.getTotalPages());
            response.put("total", voiceActorsPage.getTotalElements());
            response.put("voice_actors", voiceActors);
            return ResponseEntity.ok(response);
            
        } else {
            int finalLimit = (limit != null) ? limit : 10;
            int finalOffset = (offset != null) ? offset : 0;
            int pageNum = finalOffset / finalLimit;
            
            Pageable pageable = PageRequest.of(pageNum, finalLimit);
            Page<PersonDetails> voiceActorsPage = charactersRepository.findVoiceActors(characterMalId, pageable);
            
            List<Map<String, Object>> voiceActors = voiceActorsPage.getContent().stream()
                .map(this::toPersonDetailsSnakeCaseMap)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("character", toSnakeCaseMap(character.get()));
            
            if (limit != null || offset != null) {
                response.put("limit", finalLimit);
                response.put("offset", finalOffset);
            }
            
            response.put("total", voiceActorsPage.getTotalElements());
            response.put("voice_actors", voiceActors);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(
            summary = "Get all characters",
            description = "Retrieve paginated list of characters with optional filters, sorting, and field selection. NULL values are always sorted last."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Characters retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameters",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Comma-separated fields to return", example = "name,favorites")
            @RequestParam(required = false) String fields,
            @Parameter(description = "Search character name (case-insensitive)", example = "Spike")
            @RequestParam(required = false) String search,
            @Parameter(description = "Sort field (prefix with - for descending)", example = "-favorites")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Filter records where field IS NULL", example = "about")
            @RequestParam(required = false) String nullFilter,
            @Parameter(description = "Filter records where field IS NOT NULL", example = "favorites")
            @RequestParam(required = false) String notNullFilter,
            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Offset for pagination", example = "0")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {
        
        boolean usePageBased = (page != null || pageSize != null);
        boolean useLimitOffset = (limit != null || offset != null) && !usePageBased;
        
        Sort sortObj = parseSortParameter(sort);
        
        if (useLimitOffset) {
            int finalLimit = (limit != null) ? limit : 10;
            int finalOffset = (offset != null) ? offset : 0;
            
            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit, sortObj);
            Page<Characters> pageResult = service.findWithFilters(search, nullFilter, notNullFilter, pageable);
            
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
            Page<Characters> pageResult = service.findWithFilters(search, nullFilter, notNullFilter, pageable);
            
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
            Page<Characters> pageResult = service.findWithFilters(search, nullFilter, notNullFilter, pageable);
            
            List<Characters> results = pageResult.getContent();
            
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
     * Get statistics on NULL values for various fields
     * GET /api/characters/stats/null_counts
     */
    @Operation(
            summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field (name_kanji, image, about, favorites)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "NULL statistics retrieved successfully"
            )
    })
    @GetMapping("/stats/null_counts")
    public ResponseEntity<Map<String, Object>> getNullCounts() {
        Map<String, Long> nullCounts = service.getNullCounts();
        
        Map<String, Object> response = new HashMap<>();
        response.put("null_counts", nullCounts);
        response.put("total_records", service.count());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Convert Characters entity to Map with snake_case field names
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
     * Convert PersonDetails entity to Map with snake_case field names
     */
    private Map<String, Object> toPersonDetailsSnakeCaseMap(PersonDetails entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("person_mal_id", entity.getPersonMalId());
        result.put("name", entity.getName());
        result.put("given_name", entity.getGivenName());
        result.put("family_name", entity.getFamilyName());
        result.put("image_url", entity.getImageUrl());
        result.put("favorites", entity.getFavorites());
        result.put("birthday", entity.getBirthday());
        return result;
    }

    /**
     * Filter fields based on comma-separated field list
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

        // Add characterMalId as tiebreaker
        orders.add(Sort.Order.asc("characterMalId"));

        return Sort.by(orders);
    }
}
