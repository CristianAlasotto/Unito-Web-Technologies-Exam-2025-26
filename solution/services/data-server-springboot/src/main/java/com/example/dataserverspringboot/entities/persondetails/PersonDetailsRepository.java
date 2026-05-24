package com.example.dataserverspringboot.entities.persondetails;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link PersonDetails} entity.
 *
 * <p>Extends {@link JpaRepository} with primary key type {@link Integer}
 * (the MAL person ID). Spring Data provides all standard CRUD operations
 * automatically — no implementation class is needed.</p>
 *
 * <p>Custom query methods use two mechanisms:</p>
 * <ul>
 *   <li><b>@Query JPQL</b> — used for {@link #searchByName},
 *       {@link #findByCityContaining}, and the cross-entity join query
 *       {@link #findAnimeWorks}. Pure JPQL (no {@code nativeQuery=true})
 *       so Hibernate handles camelCase-to-snake_case column mapping
 *       automatically.</li>
 *   <li><b>Derived methods</b> — Spring Data derives SQL from the method name.
 *       The {@code IsNull} / {@code IsNotNull} suffixes generate
 *       {@code WHERE field IS NULL} / {@code WHERE field IS NOT NULL}.</li>
 * </ul>
 */
@Hidden
@Repository
public interface PersonDetailsRepository extends JpaRepository<PersonDetails, Integer> {

    /**
     * Searches people by name with a case-insensitive partial match.
     *
     * <p>The caller ({@link PersonDetailsService}) pre-builds the wildcard
     * pattern via {@code likePattern()} before calling this method,
     * e.g. {@code "%miyazaki%"}. Passing a concrete non-null {@link String}
     * prevents Hibernate from inferring {@code bytea} for a {@code null}
     * parameter, which would cause PostgreSQL to throw
     * {@code function lower(bytea) does not exist}.</p>
     *
     * @param searchPattern pre-built lowercase LIKE pattern, e.g. {@code "%miyazaki%"}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of matching people
     */
    @Query("SELECT e FROM PersonDetails e WHERE LOWER(e.name) LIKE :searchPattern")
    Page<PersonDetails> searchByName(
            @Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Searches people by city or location with a case-insensitive partial match.
     *
     * <p>The caller ({@link PersonDetailsService}) pre-builds the wildcard
     * pattern via {@code likePattern()} before calling this method,
     * e.g. {@code "%tokyo%"}. Passing a concrete non-null {@link String}
     * prevents the {@code lower(bytea)} PostgreSQL error.</p>
     *
     * @param cityPattern pre-built lowercase LIKE pattern, e.g. {@code "%tokyo%"}
     * @param pageable    pagination and sorting parameters
     * @return paginated page of matching people
     */
    @Query("SELECT e FROM PersonDetails e WHERE LOWER(e.relevantLocation) LIKE :cityPattern")
    Page<PersonDetails> findByCityContaining(
            @Param("cityPattern") String cityPattern, Pageable pageable);

    /**
     * Returns all anime this person has worked on, ordered by score descending.
     *
     * <p>Implements a cross-entity JPQL join:
     * {@code person_details → person_voice_works → details}.
     * Uses fully qualified class names so that Spring Data can resolve
     * entities from different packages. {@code DISTINCT} prevents duplicate
     * anime rows when a person voiced multiple characters in the same anime.</p>
     *
     * @param personMalId the person MAL ID to look up works for
     * @param pageable    pagination parameters
     * @return paginated page of {@link com.example.dataserverspringboot.entities.details.Details}
     *         ordered by score descending
     */
    @Query("SELECT DISTINCT d FROM com.example.dataserverspringboot.entities.details.Details d " +
           "WHERE d.malId IN " +
           "(SELECT pvw.animeMalId FROM com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.personMalId = :personMalId) " +
           "ORDER BY d.score DESC")
    Page<com.example.dataserverspringboot.entities.details.Details> findAnimeWorks(
            @Param("personMalId") Integer personMalId, Pageable pageable);

    /**
     * Counts the number of distinct anime this person has worked on.
     *
     * @param personMalId the person MAL ID to count works for
     * @return count of distinct anime
     */
    @Query("SELECT COUNT(DISTINCT pvw.animeMalId) " +
           "FROM com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.personMalId = :personMalId")
    long countAnimeWorks(@Param("personMalId") Integer personMalId);

    /**
     * Returns all people where {@code website_url} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with null website URL
     */
    Page<PersonDetails> findByWebsiteUrlIsNull(Pageable pageable);

    /**
     * Returns all people where {@code website_url} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with a non-null website URL
     */
    Page<PersonDetails> findByWebsiteUrlIsNotNull(Pageable pageable);

    /**
     * Returns all people where {@code given_name} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with null given name
     */
    Page<PersonDetails> findByGivenNameIsNull(Pageable pageable);

    /**
     * Returns all people where {@code given_name} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with a non-null given name
     */
    Page<PersonDetails> findByGivenNameIsNotNull(Pageable pageable);

    /**
     * Returns all people where {@code family_name} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with null family name
     */
    Page<PersonDetails> findByFamilyNameIsNull(Pageable pageable);

    /**
     * Returns all people where {@code family_name} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with a non-null family name
     */
    Page<PersonDetails> findByFamilyNameIsNotNull(Pageable pageable);

    /**
     * Returns all people where {@code birthday} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with null birthday
     */
    Page<PersonDetails> findByBirthdayIsNull(Pageable pageable);

    /**
     * Returns all people where {@code birthday} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with a non-null birthday
     */
    Page<PersonDetails> findByBirthdayIsNotNull(Pageable pageable);

    /**
     * Returns all people where {@code relevant_location} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with null location
     */
    Page<PersonDetails> findByRelevantLocationIsNull(Pageable pageable);

    /**
     * Returns all people where {@code relevant_location} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of people with a non-null location
     */
    Page<PersonDetails> findByRelevantLocationIsNotNull(Pageable pageable);

    /**
     * Counts people where {@code website_url} is {@code NULL}.
     *
     * @return count of people with null website URL
     */
    long countByWebsiteUrlIsNull();

    /**
     * Counts people where {@code given_name} is {@code NULL}.
     *
     * @return count of people with null given name
     */
    long countByGivenNameIsNull();

    /**
     * Counts people where {@code family_name} is {@code NULL}.
     *
     * @return count of people with null family name
     */
    long countByFamilyNameIsNull();

    /**
     * Counts people where {@code birthday} is {@code NULL}.
     *
     * @return count of people with null birthday
     */
    long countByBirthdayIsNull();

    /**
     * Counts people where {@code relevant_location} is {@code NULL}.
     *
     * @return count of people with null location
     */
    long countByRelevantLocationIsNull();
}
