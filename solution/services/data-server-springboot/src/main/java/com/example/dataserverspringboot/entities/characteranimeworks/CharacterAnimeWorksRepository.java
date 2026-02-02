package com.example.dataserverspringboot.entities.characteranimeworks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterAnimeWorksRepository extends JpaRepository<CharacterAnimeWorks, CharacterAnimeWorks.CharacterAnimeWorksId> {

    /**
     * Search by role (case-insensitive, partial match)
     */
    @Query("SELECT e FROM CharacterAnimeWorks e WHERE LOWER(CAST(e.role AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<CharacterAnimeWorks> searchByRole(@Param("search") String search, Pageable pageable);

    /**
     * Find by role
     */
    Page<CharacterAnimeWorks> findByRole(String role, Pageable pageable);

    /**
     * Find by character_mal_id
     */
    Page<CharacterAnimeWorks> findByCharacterMalId(Integer character_mal_id, Pageable pageable);

    /**
     * Find by anime_mal_id
     */
    Page<CharacterAnimeWorks> findByAnimeMalId(Integer anime_mal_id, Pageable pageable);

    // ============================================================
    // NULL FILTERING METHODS
    // ============================================================

    /**
     * Find all where character_name IS NULL
     */
    Page<CharacterAnimeWorks> findByCharacterNameIsNull(Pageable pageable);

    /**
     * Find all where character_name IS NOT NULL
     */
    Page<CharacterAnimeWorks> findByCharacterNameIsNotNull(Pageable pageable);

    /**
     * Find all where role IS NULL
     */
    Page<CharacterAnimeWorks> findByRoleIsNull(Pageable pageable);

    /**
     * Find all where role IS NOT NULL
     */
    Page<CharacterAnimeWorks> findByRoleIsNotNull(Pageable pageable);

    // Count methods for statistics

    /**
     * Count records with null character_name
     */
    long countByCharacterNameIsNull();

    /**
     * Count records with null role
     */
    long countByRoleIsNull();

}
