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
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for CharacterNicknames
 * Composite key table - supports list operations only
 */
@Tag(name = "Character Nicknames", description = "Anime character nicknames and relationships API")
@RestController
@RequestMapping("/api/character_nicknames")
@CrossOrigin(origins = "*")
public class CharacterNicknamesController {

    @Autowired
    private CharacterNicknamesService service;

    @Operation(
            summary = "Get all character nicknames",
            description = "Retrieve paginated list of nicknames with optional filters. Note: Null filters are not applicable as all fields are Primary Keys."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAll(
            @Parameter(description = "Comma-separated fields to return", example = "nickname")
            @RequestParam(required = false) String fields,

            @Parameter(description = "Search by nickname (case-insensitive)", example = "Spike")
            @RequestParam(required = false) String search,

            @Parameter(description = "Sort field (prefix with - for descending)", example = "-nickname")
            @RequestParam(required = false) String sort,

            @Parameter(description = "Filter by Character MAL ID", example = "1")
            @RequestParam(value = "character_mal_id", required = false) Integer characterMalId,

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
            Page<CharacterNicknames> pageResult = service.findWithFilters(
                search,
                characterMalId,
                pageable);

            List<CharacterNicknames> results = pageResult.getContent();
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
            Page<CharacterNicknames> pageResult = service.findWithFilters(
                search,
                characterMalId,
                pageable);

            List<CharacterNicknames> results = pageResult.getContent();
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
            Page<CharacterNicknames> pageResult = service.findWithFilters(
                search,
                characterMalId,
                pageable);

            List<CharacterNicknames> results = pageResult.getContent();

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
     * GET /api/character_nicknames/single?character_mal_id&nickname
     */
    @Operation(
            summary = "Get specific nickname entry",
            description = "Retrieve a single nickname record using the composite key (character_mal_id + nickname)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found successfully", content = @Content(schema = @Schema(implementation = CharacterNicknames.class))),
            @ApiResponse(responseCode = "400", description = "Missing key fields", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/single")
    public ResponseEntity<?> getSingle(
            @Parameter(description = "Character MAL ID (Required)", required = true)
            @RequestParam(value = "character_mal_id", required = false) Integer characterMalId,

            @Parameter(description = "Nickname (Required)", required = true)
            @RequestParam(required = false) String nickname,

            @Parameter(description = "Comma-separated fields to return")
            @RequestParam(required = false) String fields) {

        // Check if all key fields are provided
        if (characterMalId == null || nickname == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "All key fields required: characterMalId, nickname");
            error.put("usage", "GET /api/character_nicknames/single?character_mal_id&nickname");
            return ResponseEntity.status(400).body(error);
        }

        // Create composite key
        CharacterNicknames.CharacterNicknamesId id = new CharacterNicknames.CharacterNicknamesId(characterMalId, nickname);
        Optional<CharacterNicknames> entity = service.getById(id);

        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "CharacterNicknames not found");
            error.put("character_mal_id", characterMalId); error.put("nickname", nickname);
            return ResponseEntity.status(404).body(error);
        }

        CharacterNicknames data = entity.get();

        if (fields != null && !fields.isEmpty()) {
            Map<String, Object> filtered = filterFields(data, fields);
            return ResponseEntity.ok(filtered);
        }

        return ResponseEntity.ok(toSnakeCaseMap(data));
    }


    /**
     * Get summary by composite key (using query parameters)
     * GET /api/character_nicknames/summary?character_mal_id&nickname
     */
    @Operation(
            summary = "Get nickname summary",
            description = "Retrieve a brief summary of the nickname record"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found successfully"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
            @Parameter(description = "Character MAL ID (Required)", required = true)
            @RequestParam(value = "character_mal_id", required = false) Integer characterMalId,

            @Parameter(description = "Nickname (Required)", required = true)
            @RequestParam(required = false) String nickname) {

        // Check if all key fields are provided
        if (characterMalId == null || nickname == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "All key fields required: characterMalId, nickname");
            error.put("usage", "GET /api/character_nicknames/summary?character_mal_id&nickname");
            return ResponseEntity.status(400).body(error);
        }

        // Create composite key
        CharacterNicknames.CharacterNicknamesId id = new CharacterNicknames.CharacterNicknamesId(characterMalId, nickname);
        Optional<CharacterNicknames> entity = service.getById(id);

        if (entity.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "CharacterNicknames not found");
            error.put("character_mal_id", characterMalId); error.put("nickname", nickname);
            return ResponseEntity.status(404).body(error);
        }

        CharacterNicknames data = entity.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("character_mal_id", data.getCharacterMalId());
        summary.put("nickname", data.getNickname());

        return ResponseEntity.ok(summary);
    }

    private Map<String, Object> toSnakeCaseMap(CharacterNicknames entity) {
        Map<String, Object> result = new HashMap<>();
        result.put("character_mal_id", entity.getCharacterMalId());
        result.put("nickname", entity.getNickname());
        return result;
    }

    private Map<String, Object> filterFields(CharacterNicknames entity, String fields) {
        Map<String, Object> result = new HashMap<>();
        String[] requestedFields = fields.split(",");

        for (String field : requestedFields) {
            field = field.trim();
            switch (field) {
                case "character_mal_id":
                    result.put("character_mal_id", entity.getCharacterMalId());
                    break;
                case "nickname":
                    result.put("nickname", entity.getNickname());
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
                        .nullsLast());
            }
        }

        // Add primary keys as tiebreaker
        orders.add(Sort.Order.asc("characterMalId"));
        orders.add(Sort.Order.asc("nickname"));

        return Sort.by(orders);
    }
}