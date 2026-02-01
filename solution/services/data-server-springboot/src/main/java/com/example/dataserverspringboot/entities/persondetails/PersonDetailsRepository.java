package com.example.dataserverspringboot.entities.persondetails;

import com.example.dataserverspringboot.entities.details.Details;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonDetailsRepository extends JpaRepository<PersonDetails, Integer> {

    /**
     * Search by name (case-insensitive, partial match)
     */
    @Query("SELECT e FROM PersonDetails e WHERE LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PersonDetails> searchByName(@Param("search") String search, Pageable pageable);

    /**
     * JOIN 2: Find all anime this person worked on
     * person_details → person_voice_works → details
     */
    @Query("SELECT d FROM Details d WHERE d.malId IN " +
           "(SELECT DISTINCT pvw.animeMalId FROM com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.personMalId = :personMalId) " +
           "ORDER BY d.score DESC")
    Page<Details> findAnimeWorks(@Param("personMalId") Integer personMalId, Pageable pageable);

    /**
     * COUNT for JOIN 2
     */
    @Query("SELECT COUNT(DISTINCT pvw.animeMalId) FROM com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.personMalId = :personMalId")
    long countAnimeWorks(@Param("personMalId") Integer personMalId);
}
