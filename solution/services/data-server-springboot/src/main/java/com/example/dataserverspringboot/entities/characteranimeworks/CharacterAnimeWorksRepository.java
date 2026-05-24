package com.example.dataserverspringboot.entities.characteranimeworks;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link CharacterAnimeWorks} entity.
 *
 * <p>Extends {@link JpaRepository} with composite key type
 * {@link CharacterAnimeWorks.CharacterAnimeWorksId}. Spring Data provides all
 * standard CRUD operations automatically — no implementation class is needed.</p>
 *
 * <p>Custom query methods use two mechanisms:</p>
 * <ul>
 *   <li><b>@Query JPQL</b> — used for {@link #searchByRole} where a LIKE
 *       pattern is required. Pure JPQL (no {@code nativeQuery=true}) so Hibernate
 *       handles camelCase-to-snake_case column mapping automatically.</li>
 *   <li><b>Derived methods</b> — Spring Data derives SQL from the method name.
 *       The {@code IsNull} / {@code IsNotNull} suffixes generate
 *       {@code WHERE field IS NULL} / {@code WHERE field IS NOT NULL}.</li>
 * </ul>
 */
@Hidden
@Repository
public interface CharacterAnimeWorksRepository
        extends JpaRepository<CharacterAnimeWorks, CharacterAnimeWorks.CharacterAnimeWorksId> {

    /**
     * Searches character work records by role with a case-insensitive partial match.
     *
     * <p>The caller ({@link CharacterAnimeWorksService}) pre-builds the wildcard
     * pattern via {@code likePattern()} before calling this method,
     * e.g. {@code "%main%"}. Passing a concrete non-null {@link String}
     * prevents Hibernate from inferring {@code bytea} for a {@code null}
     * parameter, which would cause PostgreSQL to throw
     * {@code function lower(bytea) does not exist}.</p>
     *
     * @param searchPattern pre-built lowercase LIKE pattern, e.g. {@code "%main%"}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of matching records
     */
    @Query("SELECT e FROM CharacterAnimeWorks e WHERE LOWER(e.role) LIKE :searchPattern")
    Page<CharacterAnimeWorks> searchByRole(
            @Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Returns all records with an exact role match.
     *
     * <p>Spring Data derives:
     * {@code SELECT * FROM character_anime_works WHERE role = ?}.</p>
     *
     * @param role     exact role value to match
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<CharacterAnimeWorks> findByRole(String role, Pageable pageable);

    /**
     * Returns all records for a specific character.
     *
     * <p>Spring Data derives:
     * {@code SELECT * FROM character_anime_works WHERE character_mal_id = ?}.</p>
     *
     * @param character_mal_id character MAL ID to match
     * @param pageable         pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<CharacterAnimeWorks> findByCharacterMalId(Integer character_mal_id, Pageable pageable);

    /**
     * Returns all records for a specific anime.
     *
     * <p>Spring Data derives:
     * {@code SELECT * FROM character_anime_works WHERE anime_mal_id = ?}.</p>
     *
     * @param anime_mal_id anime MAL ID to match
     * @param pageable     pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<CharacterAnimeWorks> findByAnimeMalId(Integer anime_mal_id, Pageable pageable);

    /**
     * Returns all records where {@code character_name} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of records with null character name
     */
    Page<CharacterAnimeWorks> findByCharacterNameIsNull(Pageable pageable);

    /**
     * Returns all records where {@code character_name} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of records with a non-null character name
     */
    Page<CharacterAnimeWorks> findByCharacterNameIsNotNull(Pageable pageable);

    /**
     * Returns all records where {@code role} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of records with null role
     */
    Page<CharacterAnimeWorks> findByRoleIsNull(Pageable pageable);

    /**
     * Returns all records where {@code role} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of records with a non-null role
     */
    Page<CharacterAnimeWorks> findByRoleIsNotNull(Pageable pageable);

    /**
     * Counts records where {@code character_name} is {@code NULL}.
     *
     * @return count of records with null character name
     */
    long countByCharacterNameIsNull();

    /**
     * Counts records where {@code role} is {@code NULL}.
     *
     * @return count of records with null role
     */
    long countByRoleIsNull();
}
