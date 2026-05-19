package com.example.dataserverspringboot.entities.characternicknames;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Hidden
@Repository
public interface CharacterNicknamesRepository extends JpaRepository<CharacterNicknames, CharacterNicknames.CharacterNicknamesId> {

    /**
     * Search by nickname — case-insensitive partial match.
     * Accepts a pre-built lowercase wildcard pattern from the service layer
     * (e.g. "%spike%") to avoid the lower(bytea) PostgreSQL type inference bug.
     */
    @Query("SELECT e FROM CharacterNicknames e WHERE LOWER(e.nickname) LIKE :searchPattern")
    Page<CharacterNicknames> searchByNickname(@Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Find by character_mal_id
     */
    Page<CharacterNicknames> findByCharacterMalId(Integer characterMalId, Pageable pageable);

}
