package com.example.dataserverspringboot.entities.persondetails;

import com.example.dataserverspringboot.entities.details.Details;
import com.example.dataserverspringboot.entities.details.DetailsDTO;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for the {@link PersonDetails} module.
 *
 * <p>Contains all business logic for querying person records.
 * All public methods return {@link PersonDetailsDTO} or
 * {@code Page<PersonDetailsDTO>} — the raw {@link PersonDetails} entity
 * never leaves this layer.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Pre-building LIKE patterns via {@link #likePattern(String)} to avoid
 *       the {@code lower(bytea)} PostgreSQL type inference bug.</li>
 *   <li>Delegating the cross-entity anime works query to the repository and
 *       mapping the raw {@link Details} entity to {@link DetailsDTO} before
 *       returning — so no JPA entity ever reaches the controller.</li>
 *   <li>Routing filter requests to the most specific repository method.</li>
 *   <li>Converting {@code Page<PersonDetails>} to {@code Page<PersonDetailsDTO>}
 *       via {@code .map(PersonDetailsDTO::fromEntity)} on every branch.</li>
 * </ul>
 */
@Hidden
@Service
public class PersonDetailsService {

    @Autowired
    private PersonDetailsRepository repository;

    /**
     * Fetches a single person by their MAL ID.
     *
     * <p>Calls {@link PersonDetailsRepository#findById} and maps the result
     * to a {@link PersonDetailsDTO} via {@link PersonDetailsDTO#fromEntity}.
     * Returns an empty {@link Optional} if no record with that ID exists.</p>
     *
     * @param personMalId the primary key to look up
     * @return {@link Optional} containing the {@link PersonDetailsDTO} if found,
     *         empty otherwise
     */
    public Optional<PersonDetailsDTO> getById(Integer personMalId) {
        return repository.findById(personMalId)
                         .map(PersonDetailsDTO::fromEntity);
    }

    /**
     * Checks whether a person with the given MAL ID exists.
     *
     * <p>Used by {@link PersonDetailsController#getAnimeWorks} to decide
     * between a 404 response and the actual query, without fetching the
     * full entity unnecessarily.</p>
     *
     * @param personMalId the primary key to check
     * @return {@code true} if a record with this ID exists
     */
    public boolean existsById(Integer personMalId) {
        return repository.existsById(personMalId);
    }

    /**
     * Returns the total number of person records in the database.
     *
     * @return total record count
     */
    public long count() {
        return repository.count();
    }

    /**
     * Returns a paginated page of {@link DetailsDTO} for all anime this person
     * has worked on.
     *
     * <p>Delegates to {@link PersonDetailsRepository#findAnimeWorks}, which
     * executes a cross-entity JPQL join:
     * {@code person_details → person_voice_works → details}. The raw
     * {@link Details} entities returned by the repository are immediately
     * mapped to {@link DetailsDTO} via {@code .map(DetailsDTO::fromEntity)} —
     * so no JPA entity reaches the controller.</p>
     *
     * @param personMalId the person to look up anime works for
     * @param pageable    pagination parameters
     * @return paginated page of {@link DetailsDTO} ordered by score descending
     */
    public Page<DetailsDTO> getAnimeWorksForPerson(Integer personMalId, Pageable pageable) {
        return repository.findAnimeWorks(personMalId, pageable)
                         .map(DetailsDTO::fromEntity);
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
     * Returns a paginated page of {@link PersonDetailsDTO} matching the given filters.
     *
     * <p>Both {@code search} and {@code city} are converted to LIKE patterns
     * via {@link #likePattern(String)} before routing. Filter routing logic:</p>
     * <ol>
     *   <li>If {@code search} is provided, it is matched against the name field
     *       via {@link PersonDetailsRepository#searchByName}.</li>
     *   <li>If {@code city} is provided, it is matched against the
     *       {@code relevant_location} field via
     *       {@link PersonDetailsRepository#findByCityContaining}.</li>
     *   <li>If no filters are active, {@code findAll(pageable)} is called.</li>
     * </ol>
     *
     * <p>Every repository call is followed by
     * {@code .map(PersonDetailsDTO::fromEntity)} so the raw entity never
     * reaches the controller.</p>
     *
     * @param search   case-insensitive partial match on name, or {@code null}
     * @param city     case-insensitive partial match on location, or {@code null}
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link PersonDetailsDTO} matching all active filters
     */
    public Page<PersonDetailsDTO> findWithFilters(
            String search, String city, Pageable pageable) {

        String searchPattern = likePattern(search);
        String cityPattern   = likePattern(city);

        if (searchPattern != null) {
            return repository.searchByName(searchPattern, pageable)
                    .map(PersonDetailsDTO::fromEntity);
        }
        if (cityPattern != null) {
            return repository.findByCityContaining(cityPattern, pageable)
                    .map(PersonDetailsDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(PersonDetailsDTO::fromEntity);
    }

    /**
     * Overload of {@link #findWithFilters(String, String, Pageable)} that also
     * handles IS NULL and IS NOT NULL filters.
     *
     * <p>The null/not-null filters take precedence over {@code search} and
     * {@code city}. If {@code nullFilter} is non-empty,
     * {@link #handleNullFilter} is called and the other parameters are ignored.
     * Likewise for {@code notNullFilter}. If neither is set, the call delegates
     * to the three-parameter overload.</p>
     *
     * @param search        case-insensitive partial match on name, or {@code null}
     * @param city          case-insensitive partial match on location, or {@code null}
     * @param nullFilter    field name for IS NULL filter, or {@code null}
     * @param notNullFilter field name for IS NOT NULL filter, or {@code null}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of {@link PersonDetailsDTO} matching all active filters
     */
    public Page<PersonDetailsDTO> findWithFilters(
            String search, String city,
            String nullFilter, String notNullFilter,
            Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, city, pageable);
    }

    /**
     * Routes an IS NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code website_url}, {@code given_name},
     * {@code family_name}, {@code birthday}, {@code relevant_location}.
     * Both snake_case and camelCase variants are accepted. An unrecognised
     * field name falls back to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link PersonDetailsDTO} where the field is NULL
     */
    private Page<PersonDetailsDTO> handleNullFilter(String field, Pageable pageable) {
        Page<PersonDetails> result = switch (field.toLowerCase()) {
            case "website_url",       "websiteurl"       -> repository.findByWebsiteUrlIsNull(pageable);
            case "given_name",        "givenname"        -> repository.findByGivenNameIsNull(pageable);
            case "family_name",       "familyname"       -> repository.findByFamilyNameIsNull(pageable);
            case "birthday"                              -> repository.findByBirthdayIsNull(pageable);
            case "relevant_location", "relevantlocation" -> repository.findByRelevantLocationIsNull(pageable);
            default                                      -> repository.findAll(pageable);
        };
        return result.map(PersonDetailsDTO::fromEntity);
    }

    /**
     * Routes an IS NOT NULL filter to the correct derived repository method.
     *
     * <p>Supported field names: {@code website_url}, {@code given_name},
     * {@code family_name}, {@code birthday}, {@code relevant_location}.
     * Both snake_case and camelCase variants are accepted. An unrecognised
     * field name falls back to {@code findAll()}.</p>
     *
     * @param field    the field name to filter with IS NOT NULL (case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return paginated page of {@link PersonDetailsDTO} where the field is not NULL
     */
    private Page<PersonDetailsDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<PersonDetails> result = switch (field.toLowerCase()) {
            case "website_url",       "websiteurl"       -> repository.findByWebsiteUrlIsNotNull(pageable);
            case "given_name",        "givenname"        -> repository.findByGivenNameIsNotNull(pageable);
            case "family_name",       "familyname"       -> repository.findByFamilyNameIsNotNull(pageable);
            case "birthday"                              -> repository.findByBirthdayIsNotNull(pageable);
            case "relevant_location", "relevantlocation" -> repository.findByRelevantLocationIsNotNull(pageable);
            default                                      -> repository.findAll(pageable);
        };
        return result.map(PersonDetailsDTO::fromEntity);
    }

    /**
     * Returns a map of nullable field names to the count of records where
     * that field is {@code NULL}.
     *
     * <p>Used by {@code GET /api/person_details/stats/null_counts}.
     * Covered fields: {@code website_url}, {@code given_name},
     * {@code family_name}, {@code birthday}, {@code relevant_location}.</p>
     *
     * @return map of field name to null count
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("website_url",       repository.countByWebsiteUrlIsNull());
        counts.put("given_name",        repository.countByGivenNameIsNull());
        counts.put("family_name",       repository.countByFamilyNameIsNull());
        counts.put("birthday",          repository.countByBirthdayIsNull());
        counts.put("relevant_location", repository.countByRelevantLocationIsNull());
        return counts;
    }
}
