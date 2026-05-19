package com.example.dataserverspringboot.entities.characteranimeworks;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Hidden
@Repository
public interface CharacterAnimeWorksRepository extends JpaRepository<CharacterAnimeWorks, CharacterAnimeWorks.CharacterAnimeWorksId> {

    /**
     * Search by role — case-insensitive partial match.
     * Accepts a pre-built lowercase wildcard pattern from the service layer
     * (e.g. "%main%") to avoid the lower(bytea) PostgreSQL type inference bug.
     */
    @Query("SELECT e FROM CharacterAnimeWorks e WHERE LOWER(e.role) LIKE :searchPattern")
    Page<CharacterAnimeWorks> searchByRole(@Param("searchPattern") String searchPattern, Pageable pageable);

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
