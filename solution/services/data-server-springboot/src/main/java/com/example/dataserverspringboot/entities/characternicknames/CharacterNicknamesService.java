package com.example.dataserverspringboot.entities.characternicknames;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for the {@link CharacterNicknames} module.
 *
 * <p>Contains all business logic for querying nickname records.
 * All public methods return {@link CharacterNicknamesDTO} or
 * {@code Page<CharacterNicknamesDTO>} — the raw {@link CharacterNicknames}
 * entity never leaves this layer.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Pre-building LIKE patterns via {@link #likePattern(String)} to avoid
 *       the {@code lower(bytea)} PostgreSQL type inference bug.</li>
 *   <li>Routing filter requests to the most specific repository method.</li>
 *   <li>Converting {@code Page<CharacterNicknames>} to
 *       {@code Page<CharacterNicknamesDTO>} via
 *       {@code .map(CharacterNicknamesDTO::fromEntity)} on every branch.</li>
 * </ul>
 */
@Hidden
@Service
public class CharacterNicknamesService {

    @Autowired
    private CharacterNicknamesRepository repository;

    /**
     * Fetches a single nickname record by its composite key.
     *
     * <p>Calls {@link CharacterNicknamesRepository#findById} and maps the result
     * to a {@link CharacterNicknamesDTO} via
     * {@link CharacterNicknamesDTO#fromEntity}. Returns an empty
     * {@link Optional} if no record with the given key exists.</p>
     *
     * @param id composite key ({@code characterMalId + nickname})
     * @return {@link Optional} containing the {@link CharacterNicknamesDTO} if found,
     *         empty otherwise
     */
    public Optional<CharacterNicknamesDTO> getById(
            CharacterNicknames.CharacterNicknamesId id) {
        return repository.findById(id)
                         .map(CharacterNicknamesDTO::fromEntity);
    }

    /**
     * Returns the total number of nickname records in the database.
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
     * Returns a paginated page of {@link CharacterNicknamesDTO} matching the given filters.
     *
     * <p>Filter routing logic (single active filter at a time, in priority order):</p>
     * <ol>
     *   <li>If {@code search} is provided, it is converted to a LIKE pattern
     *       and passed to {@link CharacterNicknamesRepository#searchByNickname}.</li>
     *   <li>If {@code characterMalId} is non-null,
     *       {@link CharacterNicknamesRepository#findByCharacterMalId} is called.</li>
     *   <li>If no filters are active, {@code findAll(pageable)} is called.</li>
     * </ol>
     *
     * <p>Every repository call is followed by
     * {@code .map(CharacterNicknamesDTO::fromEntity)} so the raw entity
     * never reaches the controller.</p>
     *
     * @param search         case-insensitive partial match on the nickname, or {@code null}
     * @param characterMalId exact character ID filter, or {@code null}
     * @param pageable       pagination and sorting parameters
     * @return paginated page of {@link CharacterNicknamesDTO} matching all active filters
     */
    public Page<CharacterNicknamesDTO> findWithFilters(
            String search, Integer characterMalId, Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByNickname(searchPattern, pageable)
                    .map(CharacterNicknamesDTO::fromEntity);
        }
        if (characterMalId != null) {
            return repository.findByCharacterMalId(characterMalId, pageable)
                    .map(CharacterNicknamesDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(CharacterNicknamesDTO::fromEntity);
    }
}
