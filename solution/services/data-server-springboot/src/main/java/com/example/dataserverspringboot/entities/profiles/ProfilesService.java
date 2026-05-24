package com.example.dataserverspringboot.entities.profiles;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for the {@link Profiles} module.
 *
 * <p>Contains all business logic for querying user profile records.
 * All public methods return {@link ProfilesDTO} or
 * {@code Page<ProfilesDTO>} — the raw {@link Profiles} entity never
 * leaves this layer.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Pre-building LIKE patterns via {@link #likePattern(String)} to avoid
 *       the {@code lower(bytea)} PostgreSQL type inference bug.</li>
 *   <li>Routing filter requests to the most specific repository method.</li>
 *   <li>Converting {@code Page<Profiles>} to {@code Page<ProfilesDTO>}
 *       via {@code .map(ProfilesDTO::fromEntity)} on every branch.</li>
 * </ul>
 */
@Hidden
@Service
public class ProfilesService {

    @Autowired
    private ProfilesRepository repository;

    /**
     * Fetches a single profile by its primary key (username).
     *
     * <p>Calls {@link ProfilesRepository#findById} and maps the result to a
     * {@link ProfilesDTO} via {@link ProfilesDTO#fromEntity}. Returns an
     * empty {@link Optional} if no record with the given username exists.</p>
     *
     * @param username the primary key to look up
     * @return {@link Optional} containing the {@link ProfilesDTO} if found,
     *         empty otherwise
     */
    public Optional<ProfilesDTO> getById(String username) {
        return repository.findById(username)
                         .map(ProfilesDTO::fromEntity);
    }

    /**
     * Checks whether a profile with the given username exists.
     *
     * <p>Delegates to {@link ProfilesRepository#existsById}. Used by the
     * controller to decide between 200 and 404 without fetching the full entity.</p>
     *
     * @param username the primary key to check
     * @return {@code true} if a record with this username exists
     */
    public boolean existsById(String username) {
        return repository.existsById(username);
    }

    /**
     * Returns the total number of profile records in the database.
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
     * {@code function lower(bytea) does not exist} PostgreSQL error that occurs
     * when a {@code null} value is passed to a {@code LOWER()} or
     * {@code LIKE} clause.</p>
     *
     * @param value the raw search string entered by the client
     * @return a lowercase wildcard pattern, or {@code null} if input is blank
     */
    private String likePattern(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.toLowerCase() + "%";
    }

    /**
     * Returns a paginated page of {@link ProfilesDTO} matching the given filters.
     *
     * <p>Filter routing logic (single active filter at a time):</p>
     * <ol>
     *   <li>If {@code search} is provided, it is converted to a LIKE pattern
     *       and passed to {@link ProfilesRepository#searchByUsername}.</li>
     *   <li>If {@code gender} is non-null, {@link ProfilesRepository#findByGender}
     *       is called.</li>
     *   <li>If {@code location} is non-null,
     *       {@link ProfilesRepository#findByLocation} is called.</li>
     *   <li>If no filters are active, {@code findAll(pageable)} is called.</li>
     * </ol>
     *
     * <p>Every repository call is followed by
     * {@code .map(ProfilesDTO::fromEntity)} so the raw entity never reaches
     * the controller.</p>
     *
     * @param search   case-insensitive partial match on username, or {@code null}
     * @param gender   exact gender filter, or {@code null}
     * @param location exact location filter, or {@code null}
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link ProfilesDTO} matching all active filters
     */
    public Page<ProfilesDTO> findWithFilters(
            String search, String gender, String location, Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByUsername(searchPattern, pageable)
                    .map(ProfilesDTO::fromEntity);
        }
        if (gender != null) {
            return repository.findByGender(gender, pageable)
                    .map(ProfilesDTO::fromEntity);
        }
        if (location != null) {
            return repository.findByLocation(location, pageable)
                    .map(ProfilesDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(ProfilesDTO::fromEntity);
    }

    /**
     * Overload of {@link #findWithFilters(String, String, String, Pageable)}
     * that also handles IS NULL and IS NOT NULL filters.
     *
     * <p>The null/not-null filters take precedence over the regular filters.
     * If {@code nullFilter} is non-empty, it is handled by
     * {@link #handleNullFilter(String, Pageable)} and the other parameters
     * are ignored. Likewise for {@code notNullFilter}. If neither is set,
     * the call delegates to the three-parameter overload.</p>
     *
     * @param search      case-insensitive partial match on username, or {@code null}
     * @param gender      exact gender filter, or {@code null}
     * @param location    exact location filter, or {@code null}
     * @param nullFilter  name of the field to filter with IS NULL, or {@code null}
     * @param notNullFilter name of the field to filter with IS NOT NULL, or {@code null}
     * @param pageable    pagination and sorting parameters
     * @return paginated page of {@link ProfilesDTO} matching all active filters
     */
    public Page<ProfilesDTO> findWithFilters(
            String search, String gender, String location,
            String nullFilter, String notNullFilter,
            Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, gender, location, pageable);
    }

    /**
     * Routes an IS NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code gender}, {@code birthday}, {@code location}.
     * An unrecognised field name falls back to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link ProfilesDTO} where the specified field is NULL
     */
    private Page<ProfilesDTO> handleNullFilter(String field, Pageable pageable) {
        Page<Profiles> result = switch (field.toLowerCase()) {
            case "gender"   -> repository.findByGenderIsNull(pageable);
            case "birthday" -> repository.findByBirthdayIsNull(pageable);
            case "location" -> repository.findByLocationIsNull(pageable);
            default         -> repository.findAll(pageable);
        };
        return result.map(ProfilesDTO::fromEntity);
    }

    /**
     * Routes an IS NOT NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code gender}, {@code birthday}, {@code location}.
     * An unrecognised field name falls back to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NOT NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link ProfilesDTO} where the specified field is not NULL
     */
    private Page<ProfilesDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<Profiles> result = switch (field.toLowerCase()) {
            case "gender"   -> repository.findByGenderIsNotNull(pageable);
            case "birthday" -> repository.findByBirthdayIsNotNull(pageable);
            case "location" -> repository.findByLocationIsNotNull(pageable);
            default         -> repository.findAll(pageable);
        };
        return result.map(ProfilesDTO::fromEntity);
    }

    /**
     * Returns a map of nullable field names to the count of records where
     * that field is {@code NULL}.
     *
     * <p>Used by the {@code GET /api/profiles/stats/null_counts} endpoint
     * to provide a data-quality overview. Covered fields:
     * {@code gender}, {@code birthday}, {@code location}.</p>
     *
     * @return map of field name to null count
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("gender",   repository.countByGenderIsNull());
        counts.put("birthday", repository.countByBirthdayIsNull());
        counts.put("location", repository.countByLocationIsNull());
        return counts;
    }
}
