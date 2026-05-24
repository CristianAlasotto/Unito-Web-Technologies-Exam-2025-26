package com.example.dataserverspringboot.entities.personalternatenames;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link PersonAlternateNames} entity.
 *
 * <p>Extends {@link JpaRepository} with composite key type
 * {@link PersonAlternateNames.PersonAlternateNamesId}. Spring Data provides
 * all standard CRUD operations automatically — no implementation class is needed.</p>
 *
 * <p>Custom query methods use two mechanisms:</p>
 * <ul>
 *   <li><b>@Query JPQL</b> — used for {@link #searchByAltName} where a LIKE
 *       pattern is required. Pure JPQL (no {@code nativeQuery=true}) so Hibernate
 *       handles camelCase-to-snake_case column mapping automatically.</li>
 *   <li><b>Derived method</b> — Spring Data derives SQL from the method name
 *       ({@code findByPersonMalId} → {@code WHERE person_mal_id = ?}).</li>
 * </ul>
 */
@Hidden
@Repository
public interface PersonAlternateNamesRepository
        extends JpaRepository<PersonAlternateNames, PersonAlternateNames.PersonAlternateNamesId> {

    /**
     * Searches alternate name records with a case-insensitive partial match on the name.
     *
     * <p>The caller ({@link PersonAlternateNamesService}) pre-builds the wildcard
     * pattern via {@code likePattern()} before calling this method,
     * e.g. {@code "%miyazaki%"}. Passing a concrete non-null {@link String}
     * prevents Hibernate from inferring {@code bytea} for a {@code null} parameter,
     * which would cause PostgreSQL to throw
     * {@code function lower(bytea) does not exist}.</p>
     *
     * @param searchPattern pre-built lowercase LIKE pattern, e.g. {@code "%miyazaki%"}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of matching records
     */
    @Query("SELECT e FROM PersonAlternateNames e WHERE LOWER(e.altName) LIKE :searchPattern")
    Page<PersonAlternateNames> searchByAltName(
            @Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Returns all alternate names for a specific person.
     *
     * <p>Spring Data derives:
     * {@code SELECT * FROM person_alternate_names WHERE person_mal_id = ?}.</p>
     *
     * @param personMalId person MAL ID to match
     * @param pageable    pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<PersonAlternateNames> findByPersonMalId(Integer personMalId, Pageable pageable);
}
