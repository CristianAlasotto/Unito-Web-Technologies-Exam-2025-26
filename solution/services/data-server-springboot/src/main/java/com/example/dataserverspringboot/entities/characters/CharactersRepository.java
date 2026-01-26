package com.example.dataserverspringboot.entities.characters;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CharactersRepository extends JpaRepository<Characters, Integer> {

    /**
     * Search by name (case-insensitive, partial match)
     */
    @Query("SELECT e FROM Characters e WHERE LOWER(CAST(e.name AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Characters> searchByName(@Param("search") String search, Pageable pageable);

}
