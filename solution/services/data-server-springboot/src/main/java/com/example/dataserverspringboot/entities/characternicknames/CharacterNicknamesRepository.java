package com.example.dataserverspringboot.entities.characternicknames;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterNicknamesRepository extends JpaRepository<CharacterNicknames, CharacterNicknames.CharacterNicknamesId> {

    /**
     * Search by nickname (case-insensitive, partial match)
     */
    @Query("SELECT e FROM CharacterNicknames e WHERE LOWER(CAST(e.nickname AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<CharacterNicknames> searchByNickname(@Param("search") String search, Pageable pageable);

    /**
     * Find by character_mal_id
     */
    Page<CharacterNicknames> findByCharacterMalId(Integer character_mal_id, Pageable pageable);

}
