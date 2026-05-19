package com.example.dataserverspringboot.entities.characters;

import com.example.dataserverspringboot.entities.details.DetailsDTO;
import com.example.dataserverspringboot.entities.persondetails.PersonDetailsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Characters.
 *
 * DESIGN:
 *   - Controller only receives requests, delegates to service, and returns responses.
 *   - No repository is injected here — all data access goes through CharactersService.
 *   - DTOs are returned directly — Spring (Jackson) converts them to JSON automatically,
 *     using @JsonProperty annotations on the DTO getters for snake_case field names.
 */
@Tag(name = "Characters", description = "Anime characters and relationships API")
@RestController
@RequestMapping("/api/characters")
@CrossOrigin(origins = "*")
public class CharactersController {

    @Autowired
    private CharactersService service;

    // ── GET /api/characters/{character_mal_id} ────────────────────────────────

    @Operation(summary = "Get character by ID",
            description = "Retrieve a single anime character by their MyAnimeList character ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Character found successfully",
                    content = @Content(schema = @Schema(implementation = CharactersDTO.class))),
            @ApiResponse(responseCode = "404", description = "Character not found", content = @Content)
    })
    @GetMapping("/{character_mal_id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Character MAL ID", example = "1", required = true)
            @PathVariable("character_mal_id") Integer characterMalId) {

        Optional<CharactersDTO> dto = service.getById(characterMalId);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",            "Character not found",
                    "character_mal_id", characterMalId));
        }

        // Jackson serialises CharactersDTO to JSON automatically via @JsonProperty
        return ResponseEntity.ok(dto.get());
    }

    // ── GET /api/characters/{character_mal_id}/details ────────────────────────

    @Operation(summary = "Get anime appearances",
            description = "Retrieve all anime where this character appears, sorted by score")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime list retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Character not found", content = @Content)
    })
    @GetMapping("/{character_mal_id}/details")
    public ResponseEntity<?> getAnimeAppearances(
            @Parameter(description = "Character MAL ID", example = "1", required = true)
            @PathVariable("character_mal_id") Integer characterMalId,

            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(required = false) Integer pageSize,

            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(required = false) Integer limit,

            @Parameter(description = "Offset for pagination", example = "0")
            @RequestParam(required = false) Integer offset) {

        Optional<CharactersDTO> character = service.getById(characterMalId);
        if (character.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",            "Character not found",
                    "character_mal_id", characterMalId));
        }

        boolean usePageBased = (page != null || pageSize != null);

        if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);

            // Relation query goes through service — no repository in controller
            // Returns Page<DetailsDTO> — Jackson serialises each DetailsDTO automatically
            Page<DetailsDTO> animePage =
                    service.getAnimeAppearancesForCharacter(characterMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "character",  character.get(),
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", animePage.getTotalPages(),
                    "total",      animePage.getTotalElements(),
                    "anime",      animePage.getContent()));

        } else {
            int finalLimit  = (limit  != null) ? limit  : 10;
            int finalOffset = (offset != null) ? offset : 0;

            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit);
            Page<DetailsDTO> animePage =
                    service.getAnimeAppearancesForCharacter(characterMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "character", character.get(),
                    "limit",     finalLimit,
                    "offset",    finalOffset,
                    "total",     animePage.getTotalElements(),
                    "anime",     animePage.getContent()));
        }
    }

    // ── GET /api/characters/{character_mal_id}/voice_actors ───────────────────

    @Operation(summary = "Get voice actors",
            description = "Retrieve all voice actors for this character, sorted by favorites")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Voice actors retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Character not found", content = @Content)
    })
    @GetMapping("/{character_mal_id}/voice_actors")
    public ResponseEntity<?> getVoiceActors(
            @Parameter(description = "Character MAL ID", example = "1", required = true)
            @PathVariable("character_mal_id") Integer characterMalId,

            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(required = false) Integer pageSize,

            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(required = false) Integer limit,

            @Parameter(description = "Offset for pagination", example = "0")
            @RequestParam(required = false) Integer offset) {

        Optional<CharactersDTO> character = service.getById(characterMalId);
        if (character.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",            "Character not found",
                    "character_mal_id", characterMalId));
        }

        boolean usePageBased = (page != null || pageSize != null);

        if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);

            // Relation query goes through service — no repository in controller
            // Returns Page<PersonDetailsDTO> — Jackson serialises each DTO automatically
            Page<PersonDetailsDTO> voiceActorsPage =
                    service.getVoiceActorsForCharacter(characterMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "character",    character.get(),
                    "page",         finalPage,
                    "pageSize",     finalPageSize,
                    "totalPages",   voiceActorsPage.getTotalPages(),
                    "total",        voiceActorsPage.getTotalElements(),
                    "voice_actors", voiceActorsPage.getContent()));

        } else {
            int finalLimit  = (limit  != null) ? limit  : 10;
            int finalOffset = (offset != null) ? offset : 0;

            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit);
            Page<PersonDetailsDTO> voiceActorsPage =
                    service.getVoiceActorsForCharacter(characterMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "character",    character.get(),
                    "limit",        finalLimit,
                    "offset",       finalOffset,
                    "total",        voiceActorsPage.getTotalElements(),
                    "voice_actors", voiceActorsPage.getContent()));
        }
    }

    // ── GET /api/characters ───────────────────────────────────────────────────

    @Operation(summary = "Get all characters",
            description = "Retrieve paginated list of characters with optional filters and sorting. " +
                    "NULL values are always sorted last.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Characters retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
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

        boolean usePageBased   = (page != null || pageSize != null);
        boolean useLimitOffset = (limit != null || offset != null) && !usePageBased;

        Sort sortObj = parseSortParameter(sort);

        if (useLimitOffset) {
            int finalLimit  = (limit  != null) ? limit  : 10;
            int finalOffset = (offset != null) ? offset : 0;

            Pageable pageable = PageRequest.of(finalOffset / finalLimit, finalLimit, sortObj);
            Page<CharactersDTO> pageResult =
                    service.findWithFilters(search, nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  pageResult.getTotalElements(),
                    "items",  pageResult.getContent()));

        } else if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<CharactersDTO> pageResult =
                    service.findWithFilters(search, nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<CharactersDTO> pageResult =
                    service.findWithFilters(search, nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    // ── GET /api/characters/stats ─────────────────────────────────────────────

    @Operation(summary = "Get statistics", description = "Get total count of characters in the database")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    // ── GET /api/characters/stats/null_counts ─────────────────────────────────

    @Operation(summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field (name_kanji, image, about, favorites)")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "NULL statistics retrieved successfully"))
    @GetMapping("/stats/null_counts")
    public ResponseEntity<Map<String, Object>> getNullCounts() {
        return ResponseEntity.ok(Map.of(
                "null_counts",   service.getNullCounts(),
                "total_records", service.count()));
    }

    // ── Private helper ────────────────────────────────────────────────────────

    /**
     * Parse the sort query parameter into a Spring Sort object.
     * Prefix a field name with "-" for descending order (e.g. "-favorites").
     * NULL values are always sorted last.
     */
    private Sort parseSortParameter(String sort) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null && !sort.isEmpty()) {
            for (String field : sort.split(",")) {
                field = field.trim();
                if (field.startsWith("-")) {
                    orders.add(Sort.Order.by(field.substring(1))
                            .with(Sort.Direction.DESC).nullsLast());
                } else {
                    orders.add(Sort.Order.by(field)
                            .with(Sort.Direction.ASC).nullsLast());
                }
            }
        }
        orders.add(Sort.Order.asc("characterMalId")); // tiebreaker
        return Sort.by(orders);
    }
}
