package com.example.dataserverspringboot.entities.profiles;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Hidden
@Repository
public interface ProfilesRepository extends JpaRepository<Profiles, String> {

    /**
     * Search by username — case-insensitive partial match.
     * Accepts a pre-built lowercase wildcard pattern from the service layer
     * (e.g. "%xinil%"), following the same likePattern() convention used in
     * DetailsService to avoid the lower(bytea) PostgreSQL type inference bug.
     */
    @Query("SELECT e FROM Profiles e WHERE LOWER(e.username) LIKE :searchPattern")
    Page<Profiles> searchByUsername(@Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Find by gender
     */
    Page<Profiles> findByGender(String gender, Pageable pageable);

    /**
     * Find by location
     */
    Page<Profiles> findByLocation(String location, Pageable pageable);

    // ============================================================
    // NULL FILTERING METHODS
    // ============================================================

    /**
     * Find all profiles where gender IS NULL
     */
    Page<Profiles> findByGenderIsNull(Pageable pageable);

    /**
     * Find all profiles where gender IS NOT NULL
     */
    Page<Profiles> findByGenderIsNotNull(Pageable pageable);

    /**
     * Find all profiles where birthday IS NULL
     */
    Page<Profiles> findByBirthdayIsNull(Pageable pageable);

    /**
     * Find all profiles where birthday IS NOT NULL
     */
    Page<Profiles> findByBirthdayIsNotNull(Pageable pageable);

    /**
     * Find all profiles where location IS NULL
     */
    Page<Profiles> findByLocationIsNull(Pageable pageable);

    /**
     * Find all profiles where location IS NOT NULL
     */
    Page<Profiles> findByLocationIsNotNull(Pageable pageable);

    // Count methods for statistics

    /**
     * Count profiles with null gender
     */
    long countByGenderIsNull();

    /**
     * Count profiles with null birthday
     */
    long countByBirthdayIsNull();

    /**
     * Count profiles with null location
     */
    long countByLocationIsNull();
}
