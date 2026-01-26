package com.example.dataserverspringboot.entities.details;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailsRepository extends JpaRepository<Details, Integer> {

    /**
     * Search by title (case-insensitive, partial match)
     */
    @Query("SELECT e FROM Details e WHERE LOWER(CAST(e.title AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Details> searchByTitle(@Param("search") String search, Pageable pageable);

    /**
     * Find by type
     */
    Page<Details> findByType(String type, Pageable pageable);

    /**
     * Find by year
     */
    Page<Details> findByYear(Integer year, Pageable pageable);

    /**
     * Find by status
     */
    Page<Details> findByStatus(String status, Pageable pageable);

    /**
     * Find by rating
     */
    Page<Details> findByRating(String rating, Pageable pageable);

    /**
     * Find by source
     */
    Page<Details> findBySource(String source, Pageable pageable);

}
