package com.example.dataserverspringboot.entities.characters;

import com.example.dataserverspringboot.entities.details.Details;
import com.example.dataserverspringboot.entities.persondetails.PersonDetails;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Characters} entity.
 *
 * <p>Extends {@link JpaRepository} with primary key type {@link Integer}
 * (the character MAL ID). Spring Data provides all standard CRUD operations
 * automatically — no implementation class is needed.</p>
 *
 * <p>Custom query methods use two mechanisms:</p>
 * <ul>
 *   <li><b>@Query JPQL</b> — used for text search and cross-entity joins
 *       ({@link #searchByName}, {@link #findAnimeAppearances},
 *       {@link #findVoiceActors} and their count variants).</li>
 *   <li><b>Derived methods</b> — Spring Data derives SQL from the method name.
 *       The {@code IsNull} / {@code IsNotNull} suffixes generate
 *       {@code WHERE field IS NULL} / {@code WHERE field IS NOT NULL}.</li>
 * </ul>
 */
@Hidden
@Repository
public interface CharactersRepository extends JpaRepository<Characters, Integer> {

    /**
     * Searches characters by name with a case-insensitive partial match.
     *
     * <p>The caller ({@link CharactersService}) pre-builds the wildcard pattern
     * via {@code likePattern()} before calling this method,
     * e.g. {@code "%spike%"}. Passing a concrete non-null {@link String}
     * prevents Hibernate from inferring {@code bytea} for a {@code null}
     * parameter, which would cause PostgreSQL to throw
     * {@code function lower(bytea) does not exist}.</p>
     *
     * @param searchPattern pre-built lowercase LIKE pattern, e.g. {@code "%spike%"}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of matching characters
     */
    @Query("SELECT e FROM Characters e WHERE LOWER(e.name) LIKE :searchPattern")
    Page<Characters> searchByName(
            @Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Returns all anime where this character appears, ordered by score descending.
     *
     * <p>Cross-entity JPQL join:
     * {@code characters → character_anime_works → details}.
     * Uses fully qualified class names to resolve entities from different packages.</p>
     *
     * @param characterMalId the character MAL ID to look up appearances for
     * @param pageable       pagination parameters
     * @return paginated page of {@link Details} ordered by score descending
     */
    @Query("SELECT d FROM Details d WHERE d.malId IN " +
           "(SELECT caw.animeMalId FROM " +
           "com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
           "WHERE caw.characterMalId = :characterMalId) " +
           "ORDER BY d.score DESC")
    Page<Details> findAnimeAppearances(
            @Param("characterMalId") Integer characterMalId, Pageable pageable);

    /**
     * Counts the number of anime this character appears in.
     *
     * @param characterMalId the character MAL ID
     * @return count of anime appearances
     */
    @Query("SELECT COUNT(caw) FROM " +
           "com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
           "WHERE caw.characterMalId = :characterMalId")
    long countAnimeAppearances(@Param("characterMalId") Integer characterMalId);

    /**
     * Returns all voice actors for this character, ordered by favourites descending.
     *
     * <p>Cross-entity JPQL join:
     * {@code characters → person_voice_works → person_details}.
     * Uses fully qualified class names to resolve entities from different packages.</p>
     *
     * @param characterMalId the character MAL ID to look up voice actors for
     * @param pageable       pagination parameters
     * @return paginated page of {@link PersonDetails} ordered by favourites descending
     */
    @Query("SELECT pd FROM PersonDetails pd WHERE pd.personMalId IN " +
           "(SELECT pvw.personMalId FROM " +
           "com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.characterMalId = :characterMalId) " +
           "ORDER BY pd.favorites DESC")
    Page<PersonDetails> findVoiceActors(
            @Param("characterMalId") Integer characterMalId, Pageable pageable);

    /**
     * Counts the number of distinct voice actors for this character.
     *
     * @param characterMalId the character MAL ID
     * @return count of distinct voice actors
     */
    @Query("SELECT COUNT(DISTINCT pvw.personMalId) FROM " +
           "com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.characterMalId = :characterMalId")
    long countVoiceActors(@Param("characterMalId") Integer characterMalId);

    /**
     * Returns all characters where {@code name_kanji} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of characters with null kanji name
     */
    Page<Characters> findByNameKanjiIsNull(Pageable pageable);

    /**
     * Returns all characters where {@code name_kanji} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of characters with a non-null kanji name
     */
    Page<Characters> findByNameKanjiIsNotNull(Pageable pageable);

    /**
     * Returns all characters where {@code image} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of characters with null image
     */
    Page<Characters> findByImageIsNull(Pageable pageable);

    /**
     * Returns all characters where {@code image} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of characters with a non-null image
     */
    Page<Characters> findByImageIsNotNull(Pageable pageable);

    /**
     * Returns all characters where {@code about} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of characters with null biography
     */
    Page<Characters> findByAboutIsNull(Pageable pageable);

    /**
     * Returns all characters where {@code about} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of characters with a non-null biography
     */
    Page<Characters> findByAboutIsNotNull(Pageable pageable);

    /**
     * Counts characters where {@code name_kanji} is {@code NULL}.
     *
     * @return count of characters with null kanji name
     */
    long countByNameKanjiIsNull();

    /**
     * Returns all characters where {@code favorites} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of characters with null favourites count
     */
    Page<Characters> findByFavoritesIsNull(Pageable pageable);

    /**
     * Returns all characters where {@code favorites} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of characters with a non-null favourites count
     */
    Page<Characters> findByFavoritesIsNotNull(Pageable pageable);

    /**
     * Counts characters where {@code favorites} is {@code NULL}.
     *
     * @return count of characters with null favourites count
     */
    long countByFavoritesIsNull();

    /**
     * Counts characters where {@code image} is {@code NULL}.
     *
     * @return count of characters with null image
     */
    long countByImageIsNull();

    /**
     * Counts characters where {@code about} is {@code NULL}.
     *
     * @return count of characters with null biography
     */
    long countByAboutIsNull();
}
