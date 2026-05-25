package com.example.dataserverspringboot.entities.characters;

import com.example.dataserverspringboot.entities.details.DetailsDTO;
import com.example.dataserverspringboot.entities.persondetails.PersonDetailsDTO;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for the {@link Characters} module.
 *
 * <p>Contains all business logic for querying character records.
 * All public methods return {@link CharactersDTO}, {@link DetailsDTO},
 * {@link PersonDetailsDTO}, or their paged equivalents — the raw
 * {@link Characters} entity never leaves this layer.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Pre-building LIKE patterns via {@link #likePattern(String)} to avoid
 *       the {@code lower(bytea)} PostgreSQL type inference bug.</li>
 *   <li>Delegating cross-entity join queries to the repository and mapping
 *       the raw {@link com.example.dataserverspringboot.entities.details.Details}
 *       and {@link com.example.dataserverspringboot.entities.persondetails.PersonDetails}
 *       entities to their DTOs before returning — no JPA entity reaches the controller.</li>
 *   <li>Routing null/not-null filter requests to the correct derived repository methods.</li>
 * </ul>
 */
@Hidden
@Service
public class CharactersService {

    @Autowired
    private CharactersRepository repository;

    /**
     * Fetches a single character by their MAL ID.
     *
     * <p>Calls {@link CharactersRepository#findById} and maps the result to a
     * {@link CharactersDTO} via {@link CharactersDTO#fromEntity}. Returns an
     * empty {@link Optional} if no record with that ID exists.</p>
     *
     * @param characterMalId the character MAL ID (primary key)
     * @return {@link Optional} containing the {@link CharactersDTO} if found,
     *         empty otherwise
     */
    public Optional<CharactersDTO> getById(Integer characterMalId) {
        return repository.findById(characterMalId)
                         .map(CharactersDTO::fromEntity);
    }

    /**
     * Checks whether a character with the given MAL ID exists.
     *
     * <p>Used by the controller to decide between 200 and 404 before executing
     * a join query, avoiding a costly query on a non-existent record.</p>
     *
     * @param characterMalId the character MAL ID to check
     * @return {@code true} if a record with this ID exists
     */
    public boolean existsById(Integer characterMalId) {
        return repository.existsById(characterMalId);
    }

    /**
     * Returns the total number of character records in the database.
     *
     * @return total record count
     */
    public long count() {
        return repository.count();
    }

    /**
     * Returns a paginated page of {@link DetailsDTO} for all anime this character
     * appears in, ordered by score descending.
     *
     * <p>Delegates to {@link CharactersRepository#findAnimeAppearances}, which
     * executes a cross-entity JPQL join:
     * {@code characters → character_anime_works → details}. The raw
     * {@link com.example.dataserverspringboot.entities.details.Details} entities
     * are immediately mapped to {@link DetailsDTO} via
     * {@code .map(DetailsDTO::fromEntity)} — none reach the controller.</p>
     *
     * @param characterMalId the character to look up appearances for
     * @param pageable       pagination parameters
     * @return paginated page of {@link DetailsDTO} ordered by score descending
     */
    public Page<DetailsDTO> getAnimeAppearancesForCharacter(
            Integer characterMalId, Pageable pageable) {
        return repository.findAnimeAppearances(characterMalId, pageable)
                         .map(DetailsDTO::fromEntity);
    }

    /**
     * Returns a paginated page of {@link PersonDetailsDTO} for all voice actors
     * of this character, ordered by favourites descending.
     *
     * <p>Delegates to {@link CharactersRepository#findVoiceActors}, which
     * executes a cross-entity JPQL join:
     * {@code characters → person_voice_works → person_details}. The raw
     * {@link com.example.dataserverspringboot.entities.persondetails.PersonDetails}
     * entities are immediately mapped to {@link PersonDetailsDTO} via
     * {@code .map(PersonDetailsDTO::fromEntity)} — none reach the controller.</p>
     *
     * @param characterMalId the character to look up voice actors for
     * @param pageable       pagination parameters
     * @return paginated page of {@link PersonDetailsDTO} ordered by favourites descending
     */
    public Page<PersonDetailsDTO> getVoiceActorsForCharacter(
            Integer characterMalId, Pageable pageable) {
        return repository.findVoiceActors(characterMalId, pageable)
                         .map(PersonDetailsDTO::fromEntity);
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
     * Returns a paginated page of {@link CharactersDTO} matching the given search filter.
     *
     * <p>If {@code search} is provided, it is converted to a LIKE pattern and
     * passed to {@link CharactersRepository#searchByName}. Otherwise
     * {@code findAll(pageable)} is called.</p>
     *
     * <p>Every repository call is followed by {@code .map(CharactersDTO::fromEntity)}
     * so the raw entity never reaches the controller.</p>
     *
     * @param search   case-insensitive partial match on name, or {@code null}
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link CharactersDTO}
     */
    public Page<CharactersDTO> findWithFilters(String search, Pageable pageable) {
        String searchPattern = likePattern(search);
        if (searchPattern != null) {
            return repository.searchByName(searchPattern, pageable)
                    .map(CharactersDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(CharactersDTO::fromEntity);
    }

    /**
     * Overload of {@link #findWithFilters(String, Pageable)} that also handles
     * IS NULL and IS NOT NULL filters.
     *
     * <p>The null/not-null filters take absolute precedence. If {@code nullFilter}
     * is non-empty, {@link #handleNullFilter} is called and the other parameters
     * are ignored. Likewise for {@code notNullFilter}. If neither is set, the
     * call delegates to the two-parameter overload.</p>
     *
     * @param search        case-insensitive partial match on name, or {@code null}
     * @param nullFilter    field name for IS NULL filter, or {@code null}
     * @param notNullFilter field name for IS NOT NULL filter, or {@code null}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of {@link CharactersDTO} matching all active filters
     */
    public Page<CharactersDTO> findWithFilters(
            String search, String nullFilter, String notNullFilter, Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, pageable);
    }

    /**
     * Routes an IS NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code name_kanji}, {@code image}, {@code about}.
     * Both snake_case and camelCase variants are accepted for {@code name_kanji}.
     * An unrecognised field name falls back to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link CharactersDTO} where the field is NULL
     */
    private Page<CharactersDTO> handleNullFilter(String field, Pageable pageable) {
        Page<Characters> result = switch (field.toLowerCase()) {
            case "name_kanji", "namekanji" -> repository.findByNameKanjiIsNull(pageable);
            case "image"                   -> repository.findByImageIsNull(pageable);
            case "about"                   -> repository.findByAboutIsNull(pageable);
            default                        -> repository.findAll(pageable);
        };
        return result.map(CharactersDTO::fromEntity);
    }

    /**
     * Routes an IS NOT NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code name_kanji}, {@code image}, {@code about},
     * {@code favorites}. Both snake_case and camelCase variants are accepted for
     * {@code name_kanji}. An unrecognised field name falls back to
     * {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NOT NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link CharactersDTO} where the field is not NULL
     */
    private Page<CharactersDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<Characters> result = switch (field.toLowerCase()) {
            case "name_kanji", "namekanji" -> repository.findByNameKanjiIsNotNull(pageable);
            case "image"                   -> repository.findByImageIsNotNull(pageable);
            case "about"                   -> repository.findByAboutIsNotNull(pageable);
            case "favorites"               -> repository.findByFavoritesIsNotNull(pageable);
            default                        -> repository.findAll(pageable);
        };
        return result.map(CharactersDTO::fromEntity);
    }

    /**
     * Returns a map of nullable field names to the count of records where
     * that field is {@code NULL}.
     *
     * <p>Used by {@code GET /api/characters/stats/null_counts}.
     * Covered fields: {@code name_kanji}, {@code image}, {@code about},
     * {@code favorites}.</p>
     *
     * @return map of field name to null count
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("name_kanji", repository.countByNameKanjiIsNull());
        counts.put("image",      repository.countByImageIsNull());
        counts.put("about",      repository.countByAboutIsNull());
        counts.put("favorites",  repository.countByFavoritesIsNull());
        return counts;
    }
}
