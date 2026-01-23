package com.example.dataserverspringboot.entities.personalternatenames;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonAlternateNamesRepository extends JpaRepository<PersonAlternateNames, PersonAlternateNames.PersonAlternateNamesId> {

    /**
     * Search by alt_name (case-insensitive, partial match)
     */
    @Query("SELECT e FROM PersonAlternateNames e WHERE LOWER(CAST(e.altName AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PersonAlternateNames> searchByAltName(@Param("search") String search, Pageable pageable);

    /**
     * Find by person_mal_id
     */
    Page<PersonAlternateNames> findByPersonMalId(Integer person_mal_id, Pageable pageable);

}
