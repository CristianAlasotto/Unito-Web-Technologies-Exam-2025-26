package com.example.dataserverspringboot.entities.details;

import com.example.dataserverspringboot.entities.characters.CharactersDTO;
import com.example.dataserverspringboot.entities.personanimeworks.PersonAnimeWorks;
import com.example.dataserverspringboot.entities.personanimeworks.PersonAnimeWorksRepository;
import com.example.dataserverspringboot.entities.persondetails.PersonDetailsRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for the {@link Details} module.
 *
 * <p>Contains all business logic for querying and updating anime records.
 * All public methods return {@link DetailsDTO} or {@code Page<DetailsDTO>} —
 * the raw {@link Details} entity never leaves this layer.</p>
 *
 * <p>Three repositories are injected:</p>
 * <ul>
 *   <li>{@link DetailsRepository} — all anime queries, IS NULL filters,
 *       cross-entity joins for characters and recommendations.</li>
 *   <li>{@link PersonAnimeWorksRepository} — staff credit lookup used only
 *       inside {@link #getStaffForAnime(Integer)}.</li>
 *   <li>{@link PersonDetailsRepository} — person name resolution used only
 *       inside {@link #getStaffForAnime(Integer)}.</li>
 * </ul>
 */
@Hidden
@Service
public class DetailsService {

    @Autowired
    private DetailsRepository repository;

    @Autowired
    private PersonAnimeWorksRepository personAnimeWorksRepository;

    @Autowired
    private PersonDetailsRepository personDetailsRepository;

    /**
     * Fetches a single anime by its MAL ID.
     *
     * <p>Calls {@link DetailsRepository#findById} and maps the result to a
     * {@link DetailsDTO} via {@link DetailsDTO#fromEntity}. Returns an empty
     * {@link Optional} if no record with that ID exists.</p>
     *
     * @param malId the anime MAL ID (primary key)
     * @return {@link Optional} containing the {@link DetailsDTO} if found, empty otherwise
     */
    public Optional<DetailsDTO> getById(Integer malId) {
        return repository.findById(malId)
                         .map(DetailsDTO::fromEntity);
    }

    /**
     * Checks whether an anime with the given MAL ID exists.
     *
     * <p>Used by the controller to decide between 200 and 404 before executing
     * a join query, avoiding a costly query on a non-existent record.</p>
     *
     * @param malId the anime MAL ID to check
     * @return {@code true} if a record with this ID exists
     */
    public boolean existsById(Integer malId) {
        return repository.existsById(malId);
    }

    /**
     * Returns the total number of anime records in the database.
     *
     * @return total record count
     */
    public long count() {
        return repository.count();
    }

    /**
     * Returns a paginated page of {@link DetailsDTO} for anime recommended as
     * similar to the given anime.
     *
     * <p>Delegates to {@link DetailsRepository#findRecommendationsForAnime},
     * which executes a cross-entity JPQL subquery:
     * {@code details → recommendations → details}. Results are ordered by
     * score descending. The raw {@link Details} entities are immediately mapped
     * to {@link DetailsDTO} — none reach the controller.</p>
     *
     * @param malId    source anime MAL ID
     * @param pageable pagination parameters
     * @return paginated page of {@link DetailsDTO} ordered by score descending
     */
    public Page<DetailsDTO> getRecommendationsForAnime(Integer malId, Pageable pageable) {
        return repository.findRecommendationsForAnime(malId, pageable)
                         .map(DetailsDTO::fromEntity);
    }

    /**
     * Returns a paginated page of {@link CharactersDTO} for all characters
     * that appear in the given anime.
     *
     * <p>Delegates to {@link DetailsRepository#findCharactersInAnime}, which
     * executes a cross-entity JPQL join:
     * {@code details → character_anime_works → characters}. Results are ordered
     * by favourites descending. The raw {@code Characters} entities are
     * immediately mapped to {@link CharactersDTO} — none reach the controller.</p>
     *
     * @param malId    anime MAL ID
     * @param pageable pagination parameters
     * @return paginated page of {@link CharactersDTO} ordered by favourites descending
     */
    public Page<CharactersDTO> getCharactersForAnime(Integer malId, Pageable pageable) {
        return repository.findCharactersInAnime(malId, pageable)
                         .map(CharactersDTO::fromEntity);
    }

    /**
     * Returns a list of staff members (person details + position) for the given anime.
     *
     * <p>Processing steps:</p>
     * <ol>
     *   <li>Fetches all {@link PersonAnimeWorks} rows for this anime via
     *       {@link PersonAnimeWorksRepository#findByAnimeMalId}.</li>
     *   <li>For each credit row, resolves the person's name fields via
     *       {@link PersonDetailsRepository#findById}.</li>
     *   <li>Builds a plain {@link Map} with snake_case keys for each person
     *       ({@code person_mal_id}, {@code name}, {@code given_name},
     *       {@code family_name}, {@code position}).</li>
     * </ol>
     *
     * <p>Note: the map is built manually here because the staff response embeds
     * fields from two different entities ({@link PersonAnimeWorks} and
     * {@link com.example.dataserverspringboot.entities.persondetails.PersonDetails})
     * into one composite object, which does not correspond to a single DTO class.</p>
     *
     * @param malId anime MAL ID
     * @return list of staff maps, one entry per credit
     */
    public List<Map<String, Object>> getStaffForAnime(Integer malId) {
        List<Map<String, Object>> staff = new ArrayList<>();
        List<PersonAnimeWorks> works = personAnimeWorksRepository
                .findByAnimeMalId(malId, Pageable.unpaged()).getContent();
        for (PersonAnimeWorks work : works) {
            personDetailsRepository.findById(work.getPersonMalId()).ifPresent(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("person_mal_id", p.getPersonMalId());
                map.put("name",          p.getName());
                map.put("given_name",    p.getGivenName());
                map.put("family_name",   p.getFamilyName());
                map.put("position",      work.getPosition());
                staff.add(map);
            });
        }
        return staff;
    }

    /**
     * Converts a raw search string into a lowercase LIKE pattern
     * of the form {@code "%value%"}.
     *
     * <p>Returns {@code null} if the input is {@code null} or blank.
     * By pre-building the pattern in Java and passing a concrete non-null
     * {@link String} to the repository, Hibernate always infers the parameter
     * type as {@code VARCHAR} instead of {@code bytea}, avoiding the
     * {@code function lower(bytea) does not exist} PostgreSQL error
     * (SQLState 42883) that occurred with the original
     * {@code LOWER(CONCAT('%', :param, '%'))} approach.</p>
     *
     * @param value the raw search string entered by the client
     * @return a lowercase wildcard pattern, or {@code null} if input is blank
     */
    private String likePattern(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.toLowerCase() + "%";
    }

    /**
     * Returns a paginated page of {@link DetailsDTO} matching the given filters.
     *
     * <p>Processing steps:</p>
     * <ol>
     *   <li>{@link #likePattern(String)} pre-builds wildcard strings for
     *       {@code search} and {@code genres}. Passing concrete non-null
     *       {@link String} values avoids the {@code lower(bytea)} bug.</li>
     *   <li>The number of active filters is counted. If more than one is active,
     *       {@link DetailsRepository#findWithCombinedFilters} is called — a single
     *       JPQL {@code @Query} with AND logic and {@code :param IS NULL OR condition}
     *       to make every filter optional.</li>
     *   <li>If only one filter is active, the dedicated derived repository method
     *       is called (simpler and slightly faster than the combined query).</li>
     *   <li>If no filters are active, {@code findAll(pageable)} is called.</li>
     *   <li>Every repository call is followed by {@code .map(DetailsDTO::fromEntity)}
     *       so the raw {@link Details} entity never reaches the controller.</li>
     * </ol>
     *
     * @param search   case-insensitive partial match on title, or {@code null}
     * @param type     exact type filter, or {@code null}
     * @param year     exact year filter, or {@code null}
     * @param status   exact status filter, or {@code null}
     * @param rating   exact rating filter, or {@code null}
     * @param source   exact source filter, or {@code null}
     * @param genres   case-insensitive partial match on genres, or {@code null}
     * @param episodes exact episode count filter, or {@code null}
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link DetailsDTO} matching all active filters
     */
    public Page<DetailsDTO> findWithFilters(
            String search, String type, Integer year, String status,
            String rating, String source, String genres, Integer episodes,
            Pageable pageable) {

        String searchPattern = likePattern(search);
        String genresPattern = likePattern(genres);

        int filterCount = 0;
        if (searchPattern != null) filterCount++;
        if (type     != null)      filterCount++;
        if (year     != null)      filterCount++;
        if (status   != null)      filterCount++;
        if (rating   != null)      filterCount++;
        if (source   != null)      filterCount++;
        if (genresPattern != null) filterCount++;
        if (episodes != null)      filterCount++;

        if (filterCount > 1) {
            return repository.findWithCombinedFilters(
                    searchPattern, type, year, status,
                    rating, source, genresPattern, episodes, pageable)
                    .map(DetailsDTO::fromEntity);
        }

        if (searchPattern != null) return repository.searchByTitle(searchPattern, pageable).map(DetailsDTO::fromEntity);
        if (type     != null)      return repository.findByType(type, pageable).map(DetailsDTO::fromEntity);
        if (year     != null)      return repository.findByYear(year, pageable).map(DetailsDTO::fromEntity);
        if (status   != null)      return repository.findByStatus(status, pageable).map(DetailsDTO::fromEntity);
        if (rating   != null)      return repository.findByRating(rating, pageable).map(DetailsDTO::fromEntity);
        if (source   != null)      return repository.findBySource(source, pageable).map(DetailsDTO::fromEntity);
        if (genresPattern != null) return repository.findByGenresContaining(genresPattern, pageable).map(DetailsDTO::fromEntity);
        if (episodes != null)      return repository.findByEpisodes(episodes, pageable).map(DetailsDTO::fromEntity);

        return repository.findAll(pageable).map(DetailsDTO::fromEntity);
    }

    /**
     * Overload of
     * {@link #findWithFilters(String, String, Integer, String, String, String, String, Integer, Pageable)}
     * that also handles IS NULL and IS NOT NULL filters.
     *
     * <p>The null/not-null filters take absolute precedence over all other filters.
     * If {@code nullFilter} is non-empty, {@link #handleNullFilter} is called and
     * the remaining parameters are ignored. Likewise for {@code notNullFilter}.
     * If neither is set, the call delegates to the nine-parameter overload.</p>
     *
     * @param search        case-insensitive partial match on title, or {@code null}
     * @param type          exact type filter, or {@code null}
     * @param year          exact year filter, or {@code null}
     * @param status        exact status filter, or {@code null}
     * @param rating        exact rating filter, or {@code null}
     * @param source        exact source filter, or {@code null}
     * @param genres        case-insensitive partial match on genres, or {@code null}
     * @param episodes      exact episode count filter, or {@code null}
     * @param nullFilter    field name for IS NULL filter, or {@code null}
     * @param notNullFilter field name for IS NOT NULL filter, or {@code null}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of {@link DetailsDTO} matching all active filters
     */
    public Page<DetailsDTO> findWithFilters(
            String search, String type, Integer year, String status,
            String rating, String source, String genres, Integer episodes,
            String nullFilter, String notNullFilter,
            Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, type, year, status,
                rating, source, genres, episodes, pageable);
    }

    /**
     * Routes an IS NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code synopsis}, {@code score}, {@code end_date},
     * {@code title_japanese}, {@code season}, {@code favorites}. Both snake_case
     * and camelCase variants are accepted. An unrecognised field falls back
     * to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link DetailsDTO} where the field is NULL
     */
    private Page<DetailsDTO> handleNullFilter(String field, Pageable pageable) {
        Page<Details> result = switch (field.toLowerCase()) {
            case "synopsis"                        -> repository.findBySynopsisIsNull(pageable);
            case "score"                           -> repository.findByScoreIsNull(pageable);
            case "end_date",   "enddate"           -> repository.findByEndDateIsNull(pageable);
            case "title_japanese", "titlejapanese" -> repository.findByTitleJapaneseIsNull(pageable);
            case "season"                          -> repository.findBySeasonIsNull(pageable);
            case "favorites"                       -> repository.findByFavoritesIsNull(pageable);
            default                                -> repository.findAll(pageable);
        };
        return result.map(DetailsDTO::fromEntity);
    }

    /**
     * Routes an IS NOT NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code synopsis}, {@code score}, {@code end_date},
     * {@code title_japanese}, {@code season}, {@code favorites}. Both snake_case
     * and camelCase variants are accepted. An unrecognised field falls back
     * to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NOT NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link DetailsDTO} where the field is not NULL
     */
    private Page<DetailsDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<Details> result = switch (field.toLowerCase()) {
            case "synopsis"                        -> repository.findBySynopsisIsNotNull(pageable);
            case "score"                           -> repository.findByScoreIsNotNull(pageable);
            case "end_date",   "enddate"           -> repository.findByEndDateIsNotNull(pageable);
            case "title_japanese", "titlejapanese" -> repository.findByTitleJapaneseIsNotNull(pageable);
            case "season"                          -> repository.findBySeasonIsNotNull(pageable);
            case "favorites"                       -> repository.findByFavoritesIsNotNull(pageable);
            default                                -> repository.findAll(pageable);
        };
        return result.map(DetailsDTO::fromEntity);
    }

    /**
     * Returns a map of nullable field names to the count of records where
     * that field is {@code NULL}.
     *
     * <p>Used by {@code GET /api/details/stats/null_counts}.
     * Covered fields: {@code synopsis}, {@code score}, {@code end_date},
     * {@code title_japanese}, {@code season}, {@code favorites}.</p>
     *
     * @return map of field name to null count
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("synopsis",       repository.countBySynopsisIsNull());
        counts.put("score",          repository.countByScoreIsNull());
        counts.put("end_date",       repository.countByEndDateIsNull());
        counts.put("title_japanese", repository.countByTitleJapaneseIsNull());
        counts.put("season",         repository.countBySeasonIsNull());
        counts.put("favorites",      repository.countByFavoritesIsNull());
        return counts;
    }

    /**
     * Updates the score for the specified anime and returns the updated record as a DTO.
     *
     * <p>Fetches the {@link Details} entity by MAL ID, sets the new score,
     * saves it via {@link DetailsRepository#save}, and immediately maps the
     * result to a {@link DetailsDTO}. Returns an empty {@link Optional} if no
     * anime with that ID exists. The score has already been validated by
     * {@code @Valid} on {@link UpdateScoreRequestDTO} before this method is called.</p>
     *
     * @param malId    the MAL ID of the anime to update
     * @param newScore the new score value (0.00 to 10.00)
     * @return {@link Optional} containing the updated {@link DetailsDTO},
     *         or empty if the anime does not exist
     */
    public Optional<DetailsDTO> updateScore(Integer malId, BigDecimal newScore) {
        return repository.findById(malId)
                .map(details -> {
                    details.setScore(newScore);
                    return DetailsDTO.fromEntity(repository.save(details));
                });
    }
}
