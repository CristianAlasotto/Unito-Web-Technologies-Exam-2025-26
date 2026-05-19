package com.example.dataserverspringboot.entities.details;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dataserverspringboot.entities.characters.Characters;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Details.
 *
 * DESIGN:
 *   - Controller only receives requests, delegates to service, and returns responses.
 *   - No repository is injected here — all data access goes through DetailsService.
 *   - DTOs are returned directly to ResponseEntity — Spring (Jackson) converts them
 *     to JSON automatically, using @JsonProperty on DTO getters for snake_case names.
 *   - Input validation is handled declaratively via @Valid + UpdateScoreRequestDTO.
 */
@Tag(name = "Details", description = "Anime details and relationships API")
@RestController
@RequestMapping("/api/details")
@CrossOrigin(origins = "*")
public class DetailsController {

    @Autowired
    private DetailsService service;

    // ── GET /api/details/{mal_id} ─────────────────────────────────────────────

    @Operation(summary = "Get anime by ID",
            description = "Retrieve detailed information about a specific anime by its MyAnimeList ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime found successfully",
                    content = @Content(schema = @Schema(implementation = DetailsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Anime not found", content = @Content)
    })
    @GetMapping("/{mal_id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Anime MAL ID", example = "1", required = true)
            @PathVariable("mal_id") Integer malId) {

        Optional<DetailsDTO> dto = service.getById(malId);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",  "Anime not found",
                    "mal_id", malId));
        }

        // Jackson serialises DetailsDTO to JSON automatically via @JsonProperty
        return ResponseEntity.ok(dto.get());
    }

    // ── GET /api/details ──────────────────────────────────────────────────────

    @Operation(summary = "Get all anime",
            description = "Retrieve paginated anime with optional filters and sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search anime title (case-insensitive)", example = "Cowboy Bebop")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-score")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by anime type", example = "TV")
            @RequestParam(required = false) String type,

            @Parameter(description = "Filter by release year", example = "1998")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "Filter by airing status", example = "Finished Airing")
            @RequestParam(required = false) String status,

            @Parameter(description = "Filter by age rating", example = "PG-13")
            @RequestParam(required = false) String rating,

            @Parameter(description = "Filter by source material", example = "Manga")
            @RequestParam(required = false) String source,

            @Parameter(description = "Filter by genre (partial match)", example = "Action")
            @RequestParam(required = false) String genres,

            @Parameter(description = "Filter by exact episode count", example = "26")
            @RequestParam(required = false) Integer episodes,

            @Parameter(description = "Filter records where field IS NULL", example = "synopsis")
            @RequestParam(required = false) String nullFilter,

            @Parameter(description = "Filter records where field IS NOT NULL", example = "score")
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
            Page<DetailsDTO> pageResult = service.findWithFilters(
                    search, type, year, status, rating, source, genres, episodes,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  pageResult.getTotalElements(),
                    "items",  pageResult.getContent()));

        } else if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<DetailsDTO> pageResult = service.findWithFilters(
                    search, type, year, status, rating, source, genres, episodes,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<DetailsDTO> pageResult = service.findWithFilters(
                    search, type, year, status, rating, source, genres, episodes,
                    nullFilter, notNullFilter, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    // ── GET /api/details/stats ────────────────────────────────────────────────

    @Operation(summary = "Get statistics", description = "Get total count of anime in the database")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    // ── GET /api/details/stats/null_counts ────────────────────────────────────

    @Operation(summary = "Get NULL value statistics",
            description = "Count of NULL values for nullable fields: synopsis, score, end_date, title_japanese, season, favorites")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "NULL statistics retrieved successfully"))
    @GetMapping("/stats/null_counts")
    public ResponseEntity<Map<String, Object>> getNullCounts() {
        return ResponseEntity.ok(Map.of(
                "null_counts",   service.getNullCounts(),
                "total_records", service.count()));
    }

    // ── GET /api/details/{mal_id}/characters ──────────────────────────────────

    @Operation(summary = "Get characters in anime",
            description = "Retrieve all characters that appear in this anime, sorted by favorites")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Characters retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Anime not found", content = @Content)
    })
    @GetMapping("/{mal_id}/characters")
    public ResponseEntity<?> getCharacters(
            @Parameter(description = "Anime MAL ID", example = "1", required = true)
            @PathVariable("mal_id") Integer malId,
            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {

        if (!service.existsById(malId)) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",  "Anime not found",
                    "mal_id", malId));
        }

        int finalPage     = (page     != null) ? page     : 1;
        int finalPageSize = (pageSize != null) ? pageSize : 10;

        Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize);
        Page<Characters> charactersPage = service.getCharactersForAnime(malId, pageable);

        return ResponseEntity.ok(Map.of(
                "mal_id",     malId,
                "page",       finalPage,
                "pageSize",   finalPageSize,
                "totalPages", charactersPage.getTotalPages(),
                "totalItems", charactersPage.getTotalElements(),
                "items",      charactersPage.getContent()));
    }

    // ── GET /api/details/{mal_id}/recommendations ─────────────────────────────

    @Operation(summary = "Get anime recommendations",
            description = "Get recommended anime similar to the specified anime, sorted by score")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Anime not found", content = @Content)
    })
    @GetMapping("/{mal_id}/recommendations")
    public ResponseEntity<?> getRecommendations(
            @Parameter(description = "Anime MAL ID", example = "1", required = true)
            @PathVariable("mal_id") Integer malId,
            @Parameter(description = "Page number (1-indexed)", example = "1")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(required = false) Integer pageSize) {

        Optional<DetailsDTO> anime = service.getById(malId);
        if (anime.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",  "Anime not found",
                    "mal_id", malId));
        }

        int finalPage     = (page     != null) ? page     : 1;
        int finalPageSize = (pageSize != null) ? pageSize : 10;

        Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize,
                Sort.by("score").descending());

        Page<DetailsDTO> recommendationsPage =
                service.getRecommendationsForAnime(malId, pageable);

        return ResponseEntity.ok(Map.of(
                "anime",           anime.get(),
                "page",            finalPage,
                "pageSize",        finalPageSize,
                "totalPages",      recommendationsPage.getTotalPages(),
                "total",           recommendationsPage.getTotalElements(),
                "recommendations", recommendationsPage.getContent()));
    }

    // ── POST /api/details/update_score ────────────────────────────────────────

    @Operation(summary = "Update anime score",
            description = "Update the score for a specific anime. " +
                    "Body must contain mal_id (integer) and score (0.00 to 10.00).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Score updated successfully",
                    content = @Content(schema = @Schema(implementation = DetailsDTO.class))),
            @ApiResponse(responseCode = "404", description = "Anime not found",    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input — validation failed", content = @Content)
    })
    @PostMapping("/update_score")
    public ResponseEntity<?> updateScore(
            @Valid @RequestBody UpdateScoreRequestDTO request) {

        Optional<DetailsDTO> updated = service.updateScore(request.getMalId(), request.getScore());

        if (updated.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error",   "Anime not found",
                    "mal_id",  request.getMalId()));
        }

        // Jackson serialises DetailsDTO to JSON automatically via @JsonProperty
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Score updated successfully",
                "anime",   updated.get()));
    }

    // ── Private helper ────────────────────────────────────────────────────────

    /**
     * Parse the sort query parameter into a Spring Sort object.
     * Prefix a field name with "-" for descending order (e.g. "-score").
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
        orders.add(Sort.Order.asc("malId")); // tiebreaker
        return Sort.by(orders);
    }
}
