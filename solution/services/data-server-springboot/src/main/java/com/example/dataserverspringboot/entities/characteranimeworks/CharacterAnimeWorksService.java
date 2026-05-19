package com.example.dataserverspringboot.entities.characteranimeworks;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Hidden
@Service
public class CharacterAnimeWorksService {

    @Autowired
    private CharacterAnimeWorksRepository repository;

    // ── Basic lookups ─────────────────────────────────────────────────────────

    /**
     * Fetch a single record by composite key.
     * Returns Optional<CharacterAnimeWorksDTO> — raw entity never leaves the service layer.
     */
    public Optional<CharacterAnimeWorksDTO> getById(CharacterAnimeWorks.CharacterAnimeWorksId id) {
        return repository.findById(id)
                         .map(CharacterAnimeWorksDTO::fromEntity);
    }

    public long count() {
        return repository.count();
    }

    // ── Filter helpers ────────────────────────────────────────────────────────

    /**
     * Converts a raw search string into a lowercase LIKE pattern ("%value%").
     * Returns null if the input is null or blank.
     * Same fix as DetailsService.likePattern() — avoids lower(bytea) bug.
     */
    private String likePattern(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.toLowerCase() + "%";
    }

    /**
     * Find records with optional filters, returning a paginated page of DTOs.
     * The Page<CharacterAnimeWorks> result is mapped to Page<CharacterAnimeWorksDTO>
     * so the raw JPA entity never leaves the service layer.
     */
    public Page<CharacterAnimeWorksDTO> findWithFilters(
            String search, String role,
            Integer characterMalId, Integer animeMalId,
            Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByRole(searchPattern, pageable)
                    .map(CharacterAnimeWorksDTO::fromEntity);
        }
        if (role != null) {
            return repository.findByRole(role, pageable)
                    .map(CharacterAnimeWorksDTO::fromEntity);
        }
        if (characterMalId != null) {
            return repository.findByCharacterMalId(characterMalId, pageable)
                    .map(CharacterAnimeWorksDTO::fromEntity);
        }
        if (animeMalId != null) {
            return repository.findByAnimeMalId(animeMalId, pageable)
                    .map(CharacterAnimeWorksDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(CharacterAnimeWorksDTO::fromEntity);
    }

    /**
     * Overload that also handles IS NULL / IS NOT NULL filters.
     */
    public Page<CharacterAnimeWorksDTO> findWithFilters(
            String search, String role,
            Integer characterMalId, Integer animeMalId,
            String nullFilter, String notNullFilter,
            Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, role, characterMalId, animeMalId, pageable);
    }

    /**
     * Route IS NULL filter to the correct derived repository method.
     */
    private Page<CharacterAnimeWorksDTO> handleNullFilter(String field, Pageable pageable) {
        Page<CharacterAnimeWorks> result = switch (field.toLowerCase()) {
            case "character_name", "charactername" -> repository.findByCharacterNameIsNull(pageable);
            case "role"                            -> repository.findByRoleIsNull(pageable);
            default                                -> repository.findAll(pageable);
        };
        return result.map(CharacterAnimeWorksDTO::fromEntity);
    }

    /**
     * Route IS NOT NULL filter to the correct derived repository method.
     */
    private Page<CharacterAnimeWorksDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<CharacterAnimeWorks> result = switch (field.toLowerCase()) {
            case "character_name", "charactername" -> repository.findByCharacterNameIsNotNull(pageable);
            case "role"                            -> repository.findByRoleIsNotNull(pageable);
            default                                -> repository.findAll(pageable);
        };
        return result.map(CharacterAnimeWorksDTO::fromEntity);
    }

    /**
     * Returns a map of field names to the count of records where that field is NULL.
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("character_name", repository.countByCharacterNameIsNull());
        counts.put("role",           repository.countByRoleIsNull());
        return counts;
    }
}
