package com.example.dataserverspringboot.entities.persondetails;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonDetailsRepository extends JpaRepository<PersonDetails, Integer> {

    /**
     * Search by name (case-insensitive, partial match)
     */
    @Query("SELECT e FROM PersonDetails e WHERE LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PersonDetails> searchByName(@Param("search") String search, Pageable pageable);

}
