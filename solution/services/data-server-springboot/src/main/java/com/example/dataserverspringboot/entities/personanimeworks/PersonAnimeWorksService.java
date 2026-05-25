package com.example.dataserverspringboot.entities.personanimeworks;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for the {@link PersonAnimeWorks} module.
 *
 * <p>Contains all business logic for querying staff credit records.
 * All public methods return {@link PersonAnimeWorksDTO} or
 * {@code Page<PersonAnimeWorksDTO>} — the raw {@link PersonAnimeWorks}
 * entity never leaves this layer.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Pre-building LIKE patterns via {@link #likePattern(String)} to avoid
 *       the {@code lower(bytea)} PostgreSQL type inference bug.</li>
 *   <li>Routing filter requests to the most specific repository method
 *       via a priority-based if-else chain.</li>
 *   <li>Converting {@code Page<PersonAnimeWorks>} to
 *       {@code Page<PersonAnimeWorksDTO>} via
 *       {@code .map(PersonAnimeWorksDTO::fromEntity)} on every branch.</li>
 * </ul>
 */
@Hidden
@Service
public class PersonAnimeWorksService {

    @Autowired
    private PersonAnimeWorksRepository repository;

    /**
     * Fetches a single staff credit record by its composite key.
     *
     * <p>Calls {@link PersonAnimeWorksRepository#findById} and maps the result
     * to a {@link PersonAnimeWorksDTO} via {@link PersonAnimeWorksDTO#fromEntity}.
     * Returns an empty {@link Optional} if no record with the given key exists.</p>
     *
     * @param id composite key ({@code personMalId + position + animeMalId})
     * @return {@link Optional} containing the {@link PersonAnimeWorksDTO} if found,
     *         empty otherwise
     */
    public Optional<PersonAnimeWorksDTO> getById(PersonAnimeWorks.PersonAnimeWorksId id) {
        return repository.findById(id)
                         .map(PersonAnimeWorksDTO::fromEntity);
    }

    /**
     * Returns the total number of staff credit records in the database.
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
     * Returns a paginated page of {@link PersonAnimeWorksDTO} matching the given filters.
     *
     * <p>Filter routing logic (single active filter at a time, in priority order):</p>
     * <ol>
     *   <li>If {@code search} is provided, it is converted to a LIKE pattern
     *       and passed to {@link PersonAnimeWorksRepository#searchByPosition}.</li>
     *   <li>If {@code position} is non-null,
     *       {@link PersonAnimeWorksRepository#findByPosition} is called.</li>
     *   <li>If {@code personMalId} is non-null,
     *       {@link PersonAnimeWorksRepository#findByPersonMalId} is called.</li>
     *   <li>If {@code animeMalId} is non-null,
     *       {@link PersonAnimeWorksRepository#findByAnimeMalId} is called.</li>
     *   <li>If no filters are active, {@code findAll(pageable)} is called.</li>
     * </ol>
     *
     * <p>Every repository call is followed by
     * {@code .map(PersonAnimeWorksDTO::fromEntity)} so the raw entity
     * never reaches the controller.</p>
     *
     * @param search      case-insensitive partial match on position, or {@code null}
     * @param position    exact position filter, or {@code null}
     * @param personMalId exact person ID filter, or {@code null}
     * @param animeMalId  exact anime ID filter, or {@code null}
     * @param pageable    pagination and sorting parameters
     * @return paginated page of {@link PersonAnimeWorksDTO} matching all active filters
     */
    public Page<PersonAnimeWorksDTO> findWithFilters(
            String search, String position,
            Integer personMalId, Integer animeMalId,
            Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByPosition(searchPattern, pageable)
                    .map(PersonAnimeWorksDTO::fromEntity);
        }
        if (position != null) {
            return repository.findByPosition(position, pageable)
                    .map(PersonAnimeWorksDTO::fromEntity);
        }
        if (personMalId != null) {
            return repository.findByPersonMalId(personMalId, pageable)
                    .map(PersonAnimeWorksDTO::fromEntity);
        }
        if (animeMalId != null) {
            return repository.findByAnimeMalId(animeMalId, pageable)
                    .map(PersonAnimeWorksDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(PersonAnimeWorksDTO::fromEntity);
    }
}
