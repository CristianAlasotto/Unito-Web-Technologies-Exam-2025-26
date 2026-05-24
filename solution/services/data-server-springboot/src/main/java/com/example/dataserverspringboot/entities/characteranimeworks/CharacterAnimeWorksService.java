package com.example.dataserverspringboot.entities.characteranimeworks;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for the {@link CharacterAnimeWorks} module.
 *
 * <p>Contains all business logic for querying character-anime relationship records.
 * All public methods return {@link CharacterAnimeWorksDTO} or
 * {@code Page<CharacterAnimeWorksDTO>} — the raw {@link CharacterAnimeWorks}
 * entity never leaves this layer.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Pre-building LIKE patterns via {@link #likePattern(String)} to avoid
 *       the {@code lower(bytea)} PostgreSQL type inference bug.</li>
 *   <li>Routing filter requests to the most specific repository method
 *       via a priority-based if-else chain.</li>
 *   <li>Routing IS NULL / IS NOT NULL filters to the correct derived
 *       repository methods via two private switch-based helpers.</li>
 *   <li>Converting {@code Page<CharacterAnimeWorks>} to
 *       {@code Page<CharacterAnimeWorksDTO>} via
 *       {@code .map(CharacterAnimeWorksDTO::fromEntity)} on every branch.</li>
 * </ul>
 */
@Hidden
@Service
public class CharacterAnimeWorksService {

    @Autowired
    private CharacterAnimeWorksRepository repository;

    /**
     * Fetches a single record by its composite key.
     *
     * <p>Calls {@link CharacterAnimeWorksRepository#findById} and maps the result
     * to a {@link CharacterAnimeWorksDTO} via
     * {@link CharacterAnimeWorksDTO#fromEntity}. Returns an empty
     * {@link Optional} if no record with the given key exists.</p>
     *
     * @param id composite key ({@code characterMalId + animeMalId})
     * @return {@link Optional} containing the {@link CharacterAnimeWorksDTO} if found,
     *         empty otherwise
     */
    public Optional<CharacterAnimeWorksDTO> getById(
            CharacterAnimeWorks.CharacterAnimeWorksId id) {
        return repository.findById(id)
                         .map(CharacterAnimeWorksDTO::fromEntity);
    }

    /**
     * Returns the total number of character-anime records in the database.
     *
     * @return total record count
     */
    public long count() {
        return repository.count();
    }

    /**
     * Converts a raw search string into a lowercase LIKE pattern
     * of the form {@code "%value%"}.
     *
     * <p>Returns {@code null} if the input is {@code null} or blank.
     * By pre-building the pattern in Java and passing a concrete non-null
     * {@link String} to the repository, Hibernate always infers the parameter
     * type as {@code VARCHAR} instead of {@code bytea}, avoiding the
     * {@code function lower(bytea) does not exist} PostgreSQL error.</p>
     *
     * @param value the raw search string entered by the client
     * @return a lowercase wildcard pattern, or {@code null} if input is blank
     */
    private String likePattern(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.toLowerCase() + "%";
    }

    /**
     * Returns a paginated page of {@link CharacterAnimeWorksDTO} matching the given filters.
     *
     * <p>Filter routing logic (single active filter at a time, in priority order):</p>
     * <ol>
     *   <li>If {@code search} is provided, it is converted to a LIKE pattern
     *       and passed to {@link CharacterAnimeWorksRepository#searchByRole}.</li>
     *   <li>If {@code role} is non-null,
     *       {@link CharacterAnimeWorksRepository#findByRole} is called.</li>
     *   <li>If {@code characterMalId} is non-null,
     *       {@link CharacterAnimeWorksRepository#findByCharacterMalId} is called.</li>
     *   <li>If {@code animeMalId} is non-null,
     *       {@link CharacterAnimeWorksRepository#findByAnimeMalId} is called.</li>
     *   <li>If no filters are active, {@code findAll(pageable)} is called.</li>
     * </ol>
     *
     * <p>Every repository call is followed by
     * {@code .map(CharacterAnimeWorksDTO::fromEntity)} so the raw entity
     * never reaches the controller.</p>
     *
     * @param search         case-insensitive partial match on role, or {@code null}
     * @param role           exact role filter, or {@code null}
     * @param characterMalId exact character ID filter, or {@code null}
     * @param animeMalId     exact anime ID filter, or {@code null}
     * @param pageable       pagination and sorting parameters
     * @return paginated page of {@link CharacterAnimeWorksDTO} matching all active filters
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
     * Overload of
     * {@link #findWithFilters(String, String, Integer, Integer, Pageable)}
     * that also handles IS NULL and IS NOT NULL filters.
     *
     * <p>The null/not-null filters take absolute precedence over all other filters.
     * If {@code nullFilter} is non-empty, {@link #handleNullFilter} is called and
     * the remaining parameters are ignored. Likewise for {@code notNullFilter}.
     * If neither is set, the call delegates to the five-parameter overload.</p>
     *
     * @param search         case-insensitive partial match on role, or {@code null}
     * @param role           exact role filter, or {@code null}
     * @param characterMalId exact character ID filter, or {@code null}
     * @param animeMalId     exact anime ID filter, or {@code null}
     * @param nullFilter     field name for IS NULL filter, or {@code null}
     * @param notNullFilter  field name for IS NOT NULL filter, or {@code null}
     * @param pageable       pagination and sorting parameters
     * @return paginated page of {@link CharacterAnimeWorksDTO} matching all active filters
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
     * Routes an IS NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code character_name}, {@code role}.
     * Both snake_case and camelCase variants are accepted for
     * {@code character_name}. An unrecognised field name falls back
     * to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link CharacterAnimeWorksDTO} where the field is NULL
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
     * Routes an IS NOT NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code character_name}, {@code role}.
     * Both snake_case and camelCase variants are accepted for
     * {@code character_name}. An unrecognised field name falls back
     * to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NOT NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link CharacterAnimeWorksDTO} where the field is not NULL
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
     * Returns a map of nullable field names to the count of records where
     * that field is {@code NULL}.
     *
     * <p>Used by {@code GET /api/character_anime_works/stats/null_counts}.
     * Covered fields: {@code character_name}, {@code role}.</p>
     *
     * @return map of field name to null count
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("character_name", repository.countByCharacterNameIsNull());
        counts.put("role",           repository.countByRoleIsNull());
        return counts;
    }
}
