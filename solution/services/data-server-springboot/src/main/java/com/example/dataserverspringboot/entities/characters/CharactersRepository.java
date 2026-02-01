package com.example.dataserverspringboot.entities.characters;

import com.example.dataserverspringboot.entities.details.Details;
import com.example.dataserverspringboot.entities.persondetails.PersonDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CharactersRepository extends JpaRepository<Characters, Integer> {

    /**
     * Search by name (case-insensitive, partial match)
     */
    @Query("SELECT e FROM Characters e WHERE LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Characters> searchByName(@Param("search") String search, Pageable pageable);

    /**
     * JOIN 3: Find all anime where this character appears
     * characters → character_anime_works → details
     */
    @Query("SELECT d FROM Details d WHERE d.malId IN " +
           "(SELECT caw.animeMalId FROM com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
           "WHERE caw.characterMalId = :characterMalId) " +
           "ORDER BY d.score DESC")
    Page<Details> findAnimeAppearances(@Param("characterMalId") Integer characterMalId, Pageable pageable);

    /**
     * COUNT for JOIN 3
     */
    @Query("SELECT COUNT(caw) FROM com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
           "WHERE caw.characterMalId = :characterMalId")
    long countAnimeAppearances(@Param("characterMalId") Integer characterMalId);

    /**
     * JOIN 4: Find all voice actors for this character
     * characters → person_voice_works → person_details
     */
    @Query("SELECT pd FROM PersonDetails pd WHERE pd.personMalId IN " +
           "(SELECT pvw.personMalId FROM com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.characterMalId = :characterMalId) " +
           "ORDER BY pd.favorites DESC")
    Page<PersonDetails> findVoiceActors(@Param("characterMalId") Integer characterMalId, Pageable pageable);

    /**
     * COUNT for JOIN 4
     */
    @Query("SELECT COUNT(DISTINCT pvw.personMalId) FROM com.example.dataserverspringboot.entities.personvoiceworks.PersonVoiceWorks pvw " +
           "WHERE pvw.characterMalId = :characterMalId")
    long countVoiceActors(@Param("characterMalId") Integer characterMalId);
}
