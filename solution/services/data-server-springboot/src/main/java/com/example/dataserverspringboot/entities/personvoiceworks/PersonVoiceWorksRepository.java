package com.example.dataserverspringboot.entities.personvoiceworks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonVoiceWorksRepository extends JpaRepository<PersonVoiceWorks, PersonVoiceWorks.PersonVoiceWorksId> {

    /**
     * Search by language (case-insensitive, partial match)
     */
    @Query("SELECT e FROM PersonVoiceWorks e WHERE LOWER(CAST(e.language AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PersonVoiceWorks> searchByLanguage(@Param("search") String search, Pageable pageable);

    /**
     * Find by language
     */
    Page<PersonVoiceWorks> findByLanguage(String language, Pageable pageable);

    /**
     * Find by role
     */
    Page<PersonVoiceWorks> findByRole(String role, Pageable pageable);

    /**
     * Find by person_mal_id
     */
    Page<PersonVoiceWorks> findByPersonMalId(Integer person_mal_id, Pageable pageable);

    /**
     * Find by character_mal_id
     */
    Page<PersonVoiceWorks> findByCharacterMalId(Integer character_mal_id, Pageable pageable);

    /**
     * Find by anime_mal_id
     */
    Page<PersonVoiceWorks> findByAnimeMalId(Integer anime_mal_id, Pageable pageable);

    // ============================================================
    // NULL FILTERING METHODS
    // ============================================================

    /**
     * Find all where role IS NULL
     */
    Page<PersonVoiceWorks> findByRoleIsNull(Pageable pageable);

    /**
     * Find all where role IS NOT NULL
     */
    Page<PersonVoiceWorks> findByRoleIsNotNull(Pageable pageable);

    /**
     * Find all where language IS NULL
     */
    Page<PersonVoiceWorks> findByLanguageIsNull(Pageable pageable);

    /**
     * Find all where language IS NOT NULL
     */
    Page<PersonVoiceWorks> findByLanguageIsNotNull(Pageable pageable);

    // Count methods for statistics

    /**
     * Count records with null role
     */
    long countByRoleIsNull();

    /**
     * Count records with null language
     */
    long countByLanguageIsNull();

}
