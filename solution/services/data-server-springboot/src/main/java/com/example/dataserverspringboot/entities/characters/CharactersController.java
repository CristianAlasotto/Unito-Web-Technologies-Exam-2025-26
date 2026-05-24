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
 * REST API controller for the {@link Characters} module.
 *
 * <p>Exposes six endpoints under {@code /api/characters}:</p>
 * <ul>
 *   <li>{@code GET /api/characters/{character_mal_id}} — single character by ID.</li>
 *   <li>{@code GET /api/characters/{character_mal_id}/details} — anime appearances.</li>
 *   <li>{@code GET /api/characters/{character_mal_id}/voice_actors} — voice actors.</li>
 *   <li>{@code GET /api/characters} — paginated list with optional filters.</li>
 *   <li>{@code GET /api/characters/stats} — total record count.</li>
 *   <li>{@code GET /api/characters/stats/null_counts} — NULL statistics.</li>
 * </ul>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Only {@link CharactersService} is injected — no repository access.</li>
 *   <li>{@link CharactersDTO}, {@link DetailsDTO}, and {@link PersonDetailsDTO}
 *       objects are returned directly to {@link ResponseEntity}; Spring (Jackson)
 *       serialises them automatically using {@code @JsonProperty} annotations.</li>
 *   <li>Every method returns {@link ResponseEntity} to allow full control over
 *       HTTP status codes.</li>
 * </ul>
 */
@Tag(name = "Characters", description = "Anime characters and relationships API")
@RestController
@RequestMapping("/api/characters")
@CrossOrigin(origins = "*")
public class CharactersController {

    @Autowired
    private CharactersService service;

    /**
     * Returns a single character looked up by their MAL ID.
     *
     * <p>Returns {@code 404 Not Found} if no character with that ID exists,
     * otherwise {@code 200 OK} with the {@link CharactersDTO} serialised by Jackson.</p>
     *
     * @param characterMalId character MAL ID (path variable, primary key)
     * @return {@link ResponseEntity} with the {@link CharactersDTO}, or {@code 404}
     */
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

        return ResponseEntity.ok(dto.get());
    }

    /**
     * Returns the paginated list of anime this character appears in.
     *
     * <p>First looks up the character to include in the response body and to
     * return a meaningful {@code 404} if it does not exist. Then delegates to
     * {@link CharactersService#getAnimeAppearancesForCharacter}, which returns
     * {@code Page<DetailsDTO>} — no raw entity reaches the controller.</p>
     *
     * <p>Supports two pagination modes (page-based takes priority):</p>
     * <ul>
     *   <li>{@code page} + {@code pageSize} — page-based.</li>
     *   <li>{@code limit} + {@code offset} — limit/offset.</li>
     * </ul>
     *
     * @param characterMalId character MAL ID
     * @param page           1-indexed page number (page-based mode)
     * @param pageSize       results per page (page-based mode)
     * @param limit          maximum results (limit/offset mode)
     * @param offset         records to skip (limit/offset mode)
     * @return {@link ResponseEntity} with paginated body of {@link DetailsDTO},
     *         or {@code 404} if the character does not exist
     */
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

    /**
     * Returns the paginated list of voice actors for this character.
     *
     * <p>First looks up the character to include in the response body and to
     * return a meaningful {@code 404} if it does not exist. Then delegates to
     * {@link CharactersService#getVoiceActorsForCharacter}, which returns
     * {@code Page<PersonDetailsDTO>} — no raw entity reaches the controller.</p>
     *
     * <p>Supports two pagination modes (page-based takes priority):</p>
     * <ul>
     *   <li>{@code page} + {@code pageSize} — page-based.</li>
     *   <li>{@code limit} + {@code offset} — limit/offset.</li>
     * </ul>
     *
     * @param characterMalId character MAL ID
     * @param page           1-indexed page number (page-based mode)
     * @param pageSize       results per page (page-based mode)
     * @param limit          maximum results (limit/offset mode)
     * @param offset         records to skip (limit/offset mode)
     * @return {@link ResponseEntity} with paginated body of {@link PersonDetailsDTO},
     *         or {@code 404} if the character does not exist
     */
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

    /**
     * Returns a paginated list of {@link CharactersDTO} matching optional filters.
     *
     * <p>Supports three pagination modes (page-based takes priority over
     * limit/offset when both sets of parameters are present):</p>
     * <ul>
     *   <li>{@code page} + {@code pageSize} — page-based.</li>
     *   <li>{@code limit} + {@code offset} — limit/offset.</li>
     *   <li>No pagination parameters — returns the first 10 records.</li>
     * </ul>
     *
     * <p>Optional filters (null/not-null take absolute priority):</p>
     * <ul>
     *   <li>{@code search} — case-insensitive partial match on name.</li>
     *   <li>{@code nullFilter} — field name for IS NULL filter.</li>
     *   <li>{@code notNullFilter} — field name for IS NOT NULL filter.</li>
     * </ul>
     *
     * @param search        partial match on character name
     * @param sort          sort expression, e.g. {@code "-favorites"}
     * @param nullFilter    field name for IS NULL filter
     * @param notNullFilter field name for IS NOT NULL filter
     * @param limit         maximum results (limit/offset mode)
     * @param offset        records to skip (limit/offset mode)
     * @param page          1-indexed page number (page-based mode)
     * @param pageSize      records per page (page-based mode)
     * @return {@link ResponseEntity} with paginated or plain-list body of
     *         {@link CharactersDTO}
     */
    @Operation(summary = "Get all characters",
            description = "Retrieve paginated list of characters with optional filters and sorting. "
                    + "NULL values are always sorted last.")
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

    /**
     * Returns the total number of character records in the database.
     *
     * @return {@link ResponseEntity} with body {@code {"total": N}}
     */
    @Operation(summary = "Get statistics",
            description = "Get total count of characters in the database")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    /**
     * Returns NULL value statistics for each nullable field.
     *
     * @return {@link ResponseEntity} with body
     *         {@code {"null_counts": {...}, "total_records": N}}
     */
    @Operation(summary = "Get NULL value statistics",
            description = "Get count of NULL values for each nullable field "
                    + "(name_kanji, image, about, favorites)")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "NULL statistics retrieved successfully"))
    @GetMapping("/stats/null_counts")
    public ResponseEntity<Map<String, Object>> getNullCounts() {
        return ResponseEntity.ok(Map.of(
                "null_counts",   service.getNullCounts(),
                "total_records", service.count()));
    }

    /**
     * Converts the {@code sort} query parameter string into a Spring
     * {@link Sort} object.
     *
     * <p>Each comma-separated token is a field name. A leading {@code -}
     * means descending; no prefix means ascending. {@code NULL} values are
     * always placed last via {@link Sort.Order#nullsLast()}. A stable
     * tiebreaker on {@code characterMalId} is always appended.</p>
     *
     * @param sort comma-separated sort expression, e.g. {@code "-favorites,name"}
     * @return a {@link Sort} object ready to pass to {@link PageRequest}
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
        orders.add(Sort.Order.asc("characterMalId"));
        return Sort.by(orders);
    }
}
