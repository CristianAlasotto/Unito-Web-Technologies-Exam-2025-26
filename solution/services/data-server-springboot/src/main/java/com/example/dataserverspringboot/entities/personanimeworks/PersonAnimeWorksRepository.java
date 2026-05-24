package com.example.dataserverspringboot.entities.personanimeworks;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link PersonAnimeWorks} entity.
 *
 * <p>Extends {@link JpaRepository} with composite key type
 * {@link PersonAnimeWorks.PersonAnimeWorksId}. Spring Data provides all
 * standard CRUD operations automatically — no implementation class is needed.</p>
 *
 * <p>Custom query methods use two mechanisms:</p>
 * <ul>
 *   <li><b>@Query JPQL</b> — used for {@link #searchByPosition} where a LIKE
 *       pattern is required. Pure JPQL (no {@code nativeQuery=true}) so Hibernate
 *       handles camelCase-to-snake_case column mapping automatically.</li>
 *   <li><b>Derived methods</b> — Spring Data derives SQL from the method name
 *       (e.g. {@code findByPosition} → {@code WHERE position = ?}).</li>
 * </ul>
 */
@Hidden
@Repository
public interface PersonAnimeWorksRepository
        extends JpaRepository<PersonAnimeWorks, PersonAnimeWorks.PersonAnimeWorksId> {

    /**
     * Searches staff credit records by position with a case-insensitive partial match.
     *
     * <p>The caller ({@link PersonAnimeWorksService}) pre-builds the wildcard
     * pattern via {@code likePattern()} before calling this method,
     * e.g. {@code "%director%"}. Passing a concrete non-null {@link String}
     * prevents Hibernate from inferring {@code bytea} for a {@code null}
     * parameter, which would cause PostgreSQL to throw
     * {@code function lower(bytea) does not exist}.</p>
     *
     * @param searchPattern pre-built lowercase LIKE pattern, e.g. {@code "%director%"}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of matching records
     */
    @Query("SELECT e FROM PersonAnimeWorks e WHERE LOWER(e.position) LIKE :searchPattern")
    Page<PersonAnimeWorks> searchByPosition(
            @Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Returns all records with an exact position match.
     *
     * <p>Spring Data derives: {@code SELECT * FROM person_anime_works WHERE position = ?}.</p>
     *
     * @param position exact position value to match
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<PersonAnimeWorks> findByPosition(String position, Pageable pageable);

    /**
     * Returns all records for a specific person.
     *
     * <p>Spring Data derives:
     * {@code SELECT * FROM person_anime_works WHERE person_mal_id = ?}.</p>
     *
     * @param personMalId person MAL ID to match
     * @param pageable    pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<PersonAnimeWorks> findByPersonMalId(Integer personMalId, Pageable pageable);

    /**
     * Returns all records for a specific anime.
     *
     * <p>Spring Data derives:
     * {@code SELECT * FROM person_anime_works WHERE anime_mal_id = ?}.</p>
     *
     * @param animeMalId anime MAL ID to match
     * @param pageable   pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<PersonAnimeWorks> findByAnimeMalId(Integer animeMalId, Pageable pageable);
}
