package com.example.dataserverspringboot.entities.personalternatenames;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for the {@link PersonAlternateNames} module.
 *
 * <p>Contains all business logic for querying alternate name records.
 * All public methods return {@link PersonAlternateNamesDTO} or
 * {@code Page<PersonAlternateNamesDTO>} — the raw {@link PersonAlternateNames}
 * entity never leaves this layer.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Pre-building LIKE patterns via {@link #likePattern(String)} to avoid
 *       the {@code lower(bytea)} PostgreSQL type inference bug.</li>
 *   <li>Routing filter requests to the most specific repository method.</li>
 *   <li>Converting {@code Page<PersonAlternateNames>} to
 *       {@code Page<PersonAlternateNamesDTO>} via
 *       {@code .map(PersonAlternateNamesDTO::fromEntity)} on every branch.</li>
 * </ul>
 */
@Hidden
@Service
public class PersonAlternateNamesService {

    @Autowired
    private PersonAlternateNamesRepository repository;

    /**
     * Fetches a single alternate name record by its composite key.
     *
     * <p>Calls {@link PersonAlternateNamesRepository#findById} and maps the result
     * to a {@link PersonAlternateNamesDTO} via
     * {@link PersonAlternateNamesDTO#fromEntity}. Returns an empty
     * {@link Optional} if no record with the given key exists.</p>
     *
     * @param id composite key ({@code personMalId + altName})
     * @return {@link Optional} containing the {@link PersonAlternateNamesDTO} if found,
     *         empty otherwise
     */
    public Optional<PersonAlternateNamesDTO> getById(
            PersonAlternateNames.PersonAlternateNamesId id) {
        return repository.findById(id)
                         .map(PersonAlternateNamesDTO::fromEntity);
    }

    /**
     * Returns the total number of alternate name records in the database.
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
     * Returns a paginated page of {@link PersonAlternateNamesDTO} matching the given filters.
     *
     * <p>Filter routing logic (single active filter at a time, in priority order):</p>
     * <ol>
     *   <li>If {@code search} is provided, it is converted to a LIKE pattern
     *       and passed to {@link PersonAlternateNamesRepository#searchByAltName}.</li>
     *   <li>If {@code personMalId} is non-null,
     *       {@link PersonAlternateNamesRepository#findByPersonMalId} is called.</li>
     *   <li>If no filters are active, {@code findAll(pageable)} is called.</li>
     * </ol>
     *
     * <p>Every repository call is followed by
     * {@code .map(PersonAlternateNamesDTO::fromEntity)} so the raw entity
     * never reaches the controller.</p>
     *
     * @param search      case-insensitive partial match on the alternate name, or {@code null}
     * @param personMalId exact person ID filter, or {@code null}
     * @param pageable    pagination and sorting parameters
     * @return paginated page of {@link PersonAlternateNamesDTO} matching all active filters
     */
    public Page<PersonAlternateNamesDTO> findWithFilters(
            String search, Integer personMalId, Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByAltName(searchPattern, pageable)
                    .map(PersonAlternateNamesDTO::fromEntity);
        }
        if (personMalId != null) {
            return repository.findByPersonMalId(personMalId, pageable)
                    .map(PersonAlternateNamesDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(PersonAlternateNamesDTO::fromEntity);
    }
}
