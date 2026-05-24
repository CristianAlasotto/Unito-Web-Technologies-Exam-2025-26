package com.example.dataserverspringboot.entities.characternicknames;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link CharacterNicknames} entity.
 *
 * <p>Extends {@link JpaRepository} with composite key type
 * {@link CharacterNicknames.CharacterNicknamesId}. Spring Data provides all
 * standard CRUD operations automatically — no implementation class is needed.</p>
 *
 * <p>Custom query methods use two mechanisms:</p>
 * <ul>
 *   <li><b>@Query JPQL</b> — used for {@link #searchByNickname} where a LIKE
 *       pattern is required. Pure JPQL (no {@code nativeQuery=true}) so Hibernate
 *       handles camelCase-to-snake_case column mapping automatically.</li>
 *   <li><b>Derived method</b> — Spring Data derives SQL from the method name
 *       ({@code findByCharacterMalId} → {@code WHERE character_mal_id = ?}).</li>
 * </ul>
 */
@Hidden
@Repository
public interface CharacterNicknamesRepository
        extends JpaRepository<CharacterNicknames, CharacterNicknames.CharacterNicknamesId> {

    /**
     * Searches nickname records with a case-insensitive partial match on the nickname.
     *
     * <p>The caller ({@link CharacterNicknamesService}) pre-builds the wildcard
     * pattern via {@code likePattern()} before calling this method,
     * e.g. {@code "%spike%"}. Passing a concrete non-null {@link String}
     * prevents Hibernate from inferring {@code bytea} for a {@code null} parameter,
     * which would cause PostgreSQL to throw
     * {@code function lower(bytea) does not exist}.</p>
     *
     * @param searchPattern pre-built lowercase LIKE pattern, e.g. {@code "%spike%"}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of matching records
     */
    @Query("SELECT e FROM CharacterNicknames e WHERE LOWER(e.nickname) LIKE :searchPattern")
    Page<CharacterNicknames> searchByNickname(
            @Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Returns all nicknames for a specific character.
     *
     * <p>Spring Data derives:
     * {@code SELECT * FROM character_nicknames WHERE character_mal_id = ?}.</p>
     *
     * @param characterMalId character MAL ID to match
     * @param pageable       pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<CharacterNicknames> findByCharacterMalId(Integer characterMalId, Pageable pageable);
}
