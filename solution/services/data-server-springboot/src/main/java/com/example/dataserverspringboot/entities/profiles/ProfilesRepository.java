package com.example.dataserverspringboot.entities.profiles;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfilesRepository extends JpaRepository<Profiles, String> {

    /**
     * Search by username (case-insensitive, partial match)
     */
    @Query("SELECT e FROM Profiles e WHERE LOWER(CAST(e.username AS string)) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Profiles> searchByUsername(@Param("search") String search, Pageable pageable);

    /**
     * Find by gender
     */
    Page<Profiles> findByGender(String gender, Pageable pageable);

    /**
     * Find by location
     */
    Page<Profiles> findByLocation(String location, Pageable pageable);

}
