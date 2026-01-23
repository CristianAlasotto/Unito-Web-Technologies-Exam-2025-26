package com.example.dataserverspringboot.entities.personanimeworks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonAnimeWorksRepository extends JpaRepository<PersonAnimeWorks, PersonAnimeWorks.PersonAnimeWorksId> {

    /**
     * Search by position (case-insensitive, partial match)
     */
    @Query("SELECT e FROM PersonAnimeWorks e WHERE LOWER(CAST(e.position AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PersonAnimeWorks> searchByPosition(@Param("search") String search, Pageable pageable);

    /**
     * Find by position
     */
    Page<PersonAnimeWorks> findByPosition(String position, Pageable pageable);

    /**
     * Find by person_mal_id
     */
    Page<PersonAnimeWorks> findByPersonMalId(Integer person_mal_id, Pageable pageable);

    /**
     * Find by anime_mal_id
     */
    Page<PersonAnimeWorks> findByAnimeMalId(Integer anime_mal_id, Pageable pageable);

}
