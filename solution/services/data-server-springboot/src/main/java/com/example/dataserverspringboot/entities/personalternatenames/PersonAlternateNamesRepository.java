package com.example.dataserverspringboot.entities.personalternatenames;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Hidden
@Repository
public interface PersonAlternateNamesRepository extends JpaRepository<PersonAlternateNames, PersonAlternateNames.PersonAlternateNamesId> {

    /**
     * Search by alt_name — case-insensitive partial match.
     * Accepts a pre-built lowercase wildcard pattern from the service layer
     * (e.g. "%miyazaki%") to avoid the lower(bytea) PostgreSQL type inference bug.
     */
    @Query("SELECT e FROM PersonAlternateNames e WHERE LOWER(e.altName) LIKE :searchPattern")
    Page<PersonAlternateNames> searchByAltName(@Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Find by person_mal_id
     */
    Page<PersonAlternateNames> findByPersonMalId(Integer personMalId, Pageable pageable);

}
