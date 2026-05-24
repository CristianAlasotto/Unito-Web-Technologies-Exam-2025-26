package com.example.dataserverspringboot.entities.personvoiceworks;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for the {@link PersonVoiceWorks} module.
 *
 * <p>Contains all business logic for querying voice work records.
 * All public methods return {@link PersonVoiceWorksDTO} or
 * {@code Page<PersonVoiceWorksDTO>} — the raw {@link PersonVoiceWorks}
 * entity never leaves this layer.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Pre-building LIKE patterns via {@link #likePattern(String)} to avoid
 *       the {@code lower(bytea)} PostgreSQL type inference bug.</li>
 *   <li>Routing filter requests to the most specific repository method
 *       via a priority-based if-else chain.</li>
 *   <li>Converting {@code Page<PersonVoiceWorks>} to
 *       {@code Page<PersonVoiceWorksDTO>} via
 *       {@code .map(PersonVoiceWorksDTO::fromEntity)} on every branch.</li>
 * </ul>
 */
@Hidden
@Service
public class PersonVoiceWorksService {

    @Autowired
    private PersonVoiceWorksRepository repository;

    /**
     * Fetches a single voice work record by its composite key.
     *
     * <p>Calls {@link PersonVoiceWorksRepository#findById} and maps the result
     * to a {@link PersonVoiceWorksDTO} via {@link PersonVoiceWorksDTO#fromEntity}.
     * Returns an empty {@link Optional} if no record with the given key exists.</p>
     *
     * @param id composite key ({@code personMalId + characterMalId + animeMalId})
     * @return {@link Optional} containing the {@link PersonVoiceWorksDTO} if found,
     *         empty otherwise
     */
    public Optional<PersonVoiceWorksDTO> getById(PersonVoiceWorks.PersonVoiceWorksId id) {
        return repository.findById(id)
                         .map(PersonVoiceWorksDTO::fromEntity);
    }

    /**
     * Returns the total number of voice work records in the database.
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
     * Returns a paginated page of {@link PersonVoiceWorksDTO} matching the given filters.
     *
     * <p>Filter routing logic (single active filter at a time, in priority order):</p>
     * <ol>
     *   <li>If {@code search} is provided, it is converted to a LIKE pattern
     *       and passed to {@link PersonVoiceWorksRepository#searchByLanguage}.</li>
     *   <li>If {@code language} is non-null,
     *       {@link PersonVoiceWorksRepository#findByLanguage} is called.</li>
     *   <li>If {@code role} is non-null,
     *       {@link PersonVoiceWorksRepository#findByRole} is called.</li>
     *   <li>If {@code personMalId} is non-null,
     *       {@link PersonVoiceWorksRepository#findByPersonMalId} is called.</li>
     *   <li>If {@code characterMalId} is non-null,
     *       {@link PersonVoiceWorksRepository#findByCharacterMalId} is called.</li>
     *   <li>If {@code animeMalId} is non-null,
     *       {@link PersonVoiceWorksRepository#findByAnimeMalId} is called.</li>
     *   <li>If no filters are active, {@code findAll(pageable)} is called.</li>
     * </ol>
     *
     * <p>Every repository call is followed by
     * {@code .map(PersonVoiceWorksDTO::fromEntity)} so the raw entity
     * never reaches the controller.</p>
     *
     * @param search         case-insensitive partial match on language, or {@code null}
     * @param language       exact language filter, or {@code null}
     * @param role           exact role filter, or {@code null}
     * @param personMalId    exact person ID filter, or {@code null}
     * @param characterMalId exact character ID filter, or {@code null}
     * @param animeMalId     exact anime ID filter, or {@code null}
     * @param pageable       pagination and sorting parameters
     * @return paginated page of {@link PersonVoiceWorksDTO} matching all active filters
     */
    public Page<PersonVoiceWorksDTO> findWithFilters(
            String search, String language, String role,
            Integer personMalId, Integer characterMalId, Integer animeMalId,
            Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByLanguage(searchPattern, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (language != null) {
            return repository.findByLanguage(language, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (role != null) {
            return repository.findByRole(role, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (personMalId != null) {
            return repository.findByPersonMalId(personMalId, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (characterMalId != null) {
            return repository.findByCharacterMalId(characterMalId, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (animeMalId != null) {
            return repository.findByAnimeMalId(animeMalId, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(PersonVoiceWorksDTO::fromEntity);
    }

    /**
     * Overload of
     * {@link #findWithFilters(String, String, String, Integer, Integer, Integer, Pageable)}
     * that also handles IS NULL and IS NOT NULL filters.
     *
     * <p>The null/not-null filters take precedence over all other filters.
     * If {@code nullFilter} is non-empty, {@link #handleNullFilter} is called
     * and the remaining parameters are ignored. Likewise for {@code notNullFilter}.
     * If neither is set, the call delegates to the seven-parameter overload.</p>
     *
     * @param search         case-insensitive partial match on language, or {@code null}
     * @param language       exact language filter, or {@code null}
     * @param role           exact role filter, or {@code null}
     * @param personMalId    exact person ID filter, or {@code null}
     * @param characterMalId exact character ID filter, or {@code null}
     * @param animeMalId     exact anime ID filter, or {@code null}
     * @param nullFilter     field name for IS NULL filter, or {@code null}
     * @param notNullFilter  field name for IS NOT NULL filter, or {@code null}
     * @param pageable       pagination and sorting parameters
     * @return paginated page of {@link PersonVoiceWorksDTO} matching all active filters
     */
    public Page<PersonVoiceWorksDTO> findWithFilters(
            String search, String language, String role,
            Integer personMalId, Integer characterMalId, Integer animeMalId,
            String nullFilter, String notNullFilter,
            Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, language, role,
                personMalId, characterMalId, animeMalId, pageable);
    }

    /**
     * Routes an IS NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code role}, {@code language}.
     * An unrecognised field name falls back to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link PersonVoiceWorksDTO} where the field is NULL
     */
    private Page<PersonVoiceWorksDTO> handleNullFilter(String field, Pageable pageable) {
        Page<PersonVoiceWorks> result = switch (field.toLowerCase()) {
            case "role"     -> repository.findByRoleIsNull(pageable);
            case "language" -> repository.findByLanguageIsNull(pageable);
            default         -> repository.findAll(pageable);
        };
        return result.map(PersonVoiceWorksDTO::fromEntity);
    }

    /**
     * Routes an IS NOT NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code role}, {@code language}.
     * An unrecognised field name falls back to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NOT NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link PersonVoiceWorksDTO} where the field is not NULL
     */
    private Page<PersonVoiceWorksDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<PersonVoiceWorks> result = switch (field.toLowerCase()) {
            case "role"     -> repository.findByRoleIsNotNull(pageable);
            case "language" -> repository.findByLanguageIsNotNull(pageable);
            default         -> repository.findAll(pageable);
        };
        return result.map(PersonVoiceWorksDTO::fromEntity);
    }

    /**
     * Returns a map of nullable field names to the count of records where
     * that field is {@code NULL}.
     *
     * <p>Used by {@code GET /api/person_voice_works/stats/null_counts}.
     * Covered fields: {@code role}, {@code language}.</p>
     *
     * @return map of field name to null count
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("role",     repository.countByRoleIsNull());
        counts.put("language", repository.countByLanguageIsNull());
        return counts;
    }
}
