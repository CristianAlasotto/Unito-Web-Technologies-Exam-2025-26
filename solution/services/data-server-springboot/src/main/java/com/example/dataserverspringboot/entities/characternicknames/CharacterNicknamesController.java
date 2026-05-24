package com.example.dataserverspringboot.entities.characternicknames;

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
 * REST API controller for the {@link CharacterNicknames} module.
 *
 * <p>Exposes three endpoints under {@code /api/character_nicknames}:</p>
 * <ul>
 *   <li>{@code GET /api/character_nicknames} — paginated list with optional filters.</li>
 *   <li>{@code GET /api/character_nicknames/stats} — total record count.</li>
 *   <li>{@code GET /api/character_nicknames/single} — single record by composite key.</li>
 * </ul>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Only {@link CharacterNicknamesService} is injected — no repository access.</li>
 *   <li>{@link CharacterNicknamesDTO} objects are returned directly to
 *       {@link ResponseEntity}; Spring (Jackson) serialises them automatically
 *       using the {@code @JsonProperty} annotation on the DTO getter for
 *       {@code character_mal_id}.</li>
 *   <li>Every method returns {@link ResponseEntity} to allow full control
 *       over HTTP status codes.</li>
 *   <li>Null filters are not applicable here because both fields are primary
 *       keys and cannot be {@code NULL} by definition.</li>
 * </ul>
 */
@Tag(name = "Character Nicknames", description = "Anime character nicknames and relationships API")
@RestController
@RequestMapping("/api/character_nicknames")
@CrossOrigin(origins = "*")
public class CharacterNicknamesController {

    @Autowired
    private CharacterNicknamesService service;

    /**
     * Returns a paginated list of {@link CharacterNicknamesDTO} matching optional filters.
     *
     * <p>Supports three pagination modes (page-based takes priority over
     * limit/offset when both sets of parameters are present):</p>
     * <ul>
     *   <li>{@code page} + {@code pageSize} — response includes
     *       {@code page}, {@code pageSize}, {@code totalPages}, {@code items}.</li>
     *   <li>{@code limit} + {@code offset} — response includes
     *       {@code limit}, {@code offset}, {@code total}, {@code items}.</li>
     *   <li>No pagination parameters — returns the first 10 records as a plain list.</li>
     * </ul>
     *
     * <p>Optional filters (applied in priority order by the service):</p>
     * <ul>
     *   <li>{@code search} — case-insensitive partial match on the nickname.</li>
     *   <li>{@code character_mal_id} — exact character ID match.</li>
     * </ul>
     *
     * @param search         partial match on nickname
     * @param sort           sort expression, e.g. {@code "-nickname"}
     * @param characterMalId exact character ID filter
     * @param limit          maximum results (limit/offset mode)
     * @param offset         records to skip (limit/offset mode)
     * @param page           1-indexed page number (page-based mode)
     * @param pageSize       records per page (page-based mode)
     * @return {@link ResponseEntity} with paginated or plain-list body of
     *         {@link CharacterNicknamesDTO}
     */
    @Operation(
            summary = "Get all character nicknames",
            description = "Retrieve paginated list of nicknames with optional filters and sorting. "
                    + "Note: null filters are not applicable as both fields are primary keys.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Search by nickname (case-insensitive)", example = "Spike")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-nickname")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by character MAL ID", example = "1")
            @RequestParam(value = "character_mal_id", required = false) Integer characterMalId,

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
            Page<CharacterNicknamesDTO> pageResult =
                    service.findWithFilters(search, characterMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "limit",  finalLimit,
                    "offset", finalOffset,
                    "total",  pageResult.getTotalElements(),
                    "items",  pageResult.getContent()));

        } else if (usePageBased) {
            int finalPage     = (page     != null) ? page     : 1;
            int finalPageSize = (pageSize != null) ? pageSize : (limit != null) ? limit : 10;

            Pageable pageable = PageRequest.of(finalPage - 1, finalPageSize, sortObj);
            Page<CharacterNicknamesDTO> pageResult =
                    service.findWithFilters(search, characterMalId, pageable);

            return ResponseEntity.ok(Map.of(
                    "page",       finalPage,
                    "pageSize",   finalPageSize,
                    "totalPages", pageResult.getTotalPages(),
                    "items",      pageResult.getContent()));

        } else {
            Pageable pageable = PageRequest.of(0, 10, sortObj);
            Page<CharacterNicknamesDTO> pageResult =
                    service.findWithFilters(search, characterMalId, pageable);

            return ResponseEntity.ok(pageResult.getContent());
        }
    }

    /**
     * Returns the total number of nickname records in the database.
     *
     * @return {@link ResponseEntity} with body {@code {"total": N}}
     */
    @Operation(summary = "Get statistics",
            description = "Get total count of nickname records")
    @ApiResponses(@ApiResponse(responseCode = "200",
            description = "Statistics retrieved successfully"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of("total", service.count()));
    }

    /**
     * Returns a single nickname record looked up by its composite key.
     *
     * <p>Both {@code character_mal_id} and {@code nickname} are required.
     * If either is missing, {@code 400 Bad Request} is returned with a usage hint.
     * If the composite key does not exist, {@code 404 Not Found} is returned.</p>
     *
     * @param characterMalId character MAL ID (required)
     * @param nickname       nickname string (required)
     * @return {@link ResponseEntity} containing the {@link CharacterNicknamesDTO},
     *         or an error body with status 400 or 404
     */
    @Operation(
            summary = "Get specific nickname entry",
            description = "Retrieve a single nickname record using the composite key "
                    + "(character_mal_id + nickname)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found successfully",
                    content = @Content(schema = @Schema(implementation = CharacterNicknamesDTO.class))),
            @ApiResponse(responseCode = "400", description = "Missing key fields",  content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found",           content = @Content)
    })
    @GetMapping("/single")
    public ResponseEntity<?> getSingle(
            @Parameter(description = "Character MAL ID", required = true)
            @RequestParam(value = "character_mal_id", required = false) Integer characterMalId,

            @Parameter(description = "Nickname", required = true)
            @RequestParam(required = false) String nickname) {

        if (characterMalId == null || nickname == null) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "All key fields required: character_mal_id, nickname",
                    "usage", "GET /api/character_nicknames/single?character_mal_id=1&nickname=Spike"));
        }

        CharacterNicknames.CharacterNicknamesId id =
                new CharacterNicknames.CharacterNicknamesId(characterMalId, nickname);

        Optional<CharacterNicknamesDTO> dto = service.getById(id);

        if (dto.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error",            "CharacterNicknames not found",
                    "character_mal_id", characterMalId,
                    "nickname",         nickname));
        }

        return ResponseEntity.ok(dto.get());
    }

    /**
     * Converts the {@code sort} query parameter string into a Spring
     * {@link Sort} object.
     *
     * <p>Each comma-separated token is a field name. A leading {@code -}
     * means descending; no prefix means ascending. {@code NULL} values are
     * always placed last via {@link Sort.Order#nullsLast()}. Stable tiebreakers
     * on both composite key fields ({@code characterMalId} and {@code nickname})
     * are always appended for deterministic pagination.</p>
     *
     * @param sort comma-separated sort expression, e.g. {@code "-nickname"}
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
        orders.add(Sort.Order.asc("nickname"));
        return Sort.by(orders);
    }
}
