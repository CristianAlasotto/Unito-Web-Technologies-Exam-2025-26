package com.example.dataserverspringboot.entities.persondetails;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Hidden
@Repository
public interface PersonDetailsRepository extends JpaRepository<PersonDetails, Integer> {

    /**
     * Search by name (case-insensitive, partial match)
     */
    /**
     * Search by name — case-insensitive partial match.
     * Accepts a pre-built lowercase wildcard pattern from the service layer
     * (e.g. "%miyazaki%") to avoid the lower(bytea) PostgreSQL type inference bug.
     */
    @Query("SELECT e FROM PersonDetails e WHERE LOWER(e.name) LIKE :searchPattern")
    Page<PersonDetails> searchByName(@Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Find by city/location (case-insensitive, partial match)
     */
    /**
     * Find by city/location — case-insensitive partial match.
     * Accepts a pre-built lowercase wildcard pattern from the service layer
     * (e.g. "%tokyo%") to avoid the lower(bytea) PostgreSQL type inference bug.
     */
    @Query("SELECT e FROM PersonDetails e WHERE LOWER(e.relevantLocation) LIKE :cityPattern")
    Page<PersonDetails> findByCityContaining(@Param("cityPattern") String cityPattern, Pageable pageable);

    /**
     * JOIN 2: Find all anime works for this person
     * person_details → person_voice_works → details
     */
    @Query("SELECT DISTINCT d FROM com.example.dataserverspringboot.entities.details.Details d WHERE d.malId IN " +
           "(SELECT pvw.animeMalId FROM com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.personMalId = :personMalId) " +
           "ORDER BY d.score DESC")
    Page<com.example.dataserverspringboot.entities.details.Details> findAnimeWorks(@Param("personMalId") Integer personMalId, Pageable pageable);

    /**
     * COUNT for JOIN 2
     */
    @Query("SELECT COUNT(DISTINCT pvw.animeMalId) FROM com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.personMalId = :personMalId")
    long countAnimeWorks(@Param("personMalId") Integer personMalId);

    // ============================================================
    // NULL FILTERING METHODS
    // ============================================================

    /**
     * Find all where website_url IS NULL
     */
    Page<PersonDetails> findByWebsiteUrlIsNull(Pageable pageable);

    /**
     * Find all where website_url IS NOT NULL
     */
    Page<PersonDetails> findByWebsiteUrlIsNotNull(Pageable pageable);

    /**
     * Find all where given_name IS NULL
     */
    Page<PersonDetails> findByGivenNameIsNull(Pageable pageable);

    /**
     * Find all where given_name IS NOT NULL
     */
    Page<PersonDetails> findByGivenNameIsNotNull(Pageable pageable);

    /**
     * Find all where family_name IS NULL
     */
    Page<PersonDetails> findByFamilyNameIsNull(Pageable pageable);

    /**
     * Find all where family_name IS NOT NULL
     */
    Page<PersonDetails> findByFamilyNameIsNotNull(Pageable pageable);

    /**
     * Find all where birthday IS NULL
     */
    Page<PersonDetails> findByBirthdayIsNull(Pageable pageable);

    /**
     * Find all where birthday IS NOT NULL
     */
    Page<PersonDetails> findByBirthdayIsNotNull(Pageable pageable);

    /**
     * Find all where relevant_location IS NULL
     */
    Page<PersonDetails> findByRelevantLocationIsNull(Pageable pageable);

    /**
     * Find all where relevant_location IS NOT NULL
     */
    Page<PersonDetails> findByRelevantLocationIsNotNull(Pageable pageable);

    // Count methods for statistics

    /**
     * Count records with null website_url
     */
    long countByWebsiteUrlIsNull();

    /**
     * Count records with null given_name
     */
    long countByGivenNameIsNull();

    /**
     * Count records with null family_name
     */
    long countByFamilyNameIsNull();

    /**
     * Count records with null birthday
     */
    long countByBirthdayIsNull();

    /**
     * Count records with null relevant_location
     */
    long countByRelevantLocationIsNull();

}
