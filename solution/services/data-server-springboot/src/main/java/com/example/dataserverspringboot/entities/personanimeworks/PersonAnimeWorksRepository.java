package com.example.dataserverspringboot.entities.personanimeworks;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Hidden
@Repository
public interface PersonAnimeWorksRepository extends JpaRepository<PersonAnimeWorks, PersonAnimeWorks.PersonAnimeWorksId> {

    /**
     * Search by position — case-insensitive partial match.
     * Accepts a pre-built lowercase wildcard pattern from the service layer
     * (e.g. "%director%") to avoid the lower(bytea) PostgreSQL type inference bug.
     */
    @Query("SELECT e FROM PersonAnimeWorks e WHERE LOWER(e.position) LIKE :searchPattern")
    Page<PersonAnimeWorks> searchByPosition(@Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Find by position
     */
    Page<PersonAnimeWorks> findByPosition(String position, Pageable pageable);

    /**
     * Find by person_mal_id
     */
    Page<PersonAnimeWorks> findByPersonMalId(Integer personMalId, Pageable pageable);

    /**
     * Find by anime_mal_id
     */
    Page<PersonAnimeWorks> findByAnimeMalId(Integer animeMalId, Pageable pageable);

}
