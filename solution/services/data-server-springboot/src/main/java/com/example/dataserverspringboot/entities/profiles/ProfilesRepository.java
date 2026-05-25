package com.example.dataserverspringboot.entities.profiles;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Profiles} entity.
 *
 * <p>Extends {@link JpaRepository} with primary key type {@link String}
 * (the username). Spring Data provides all standard CRUD operations
 * automatically — no implementation class is needed.</p>
 *
 * <p>Custom query methods use two mechanisms:</p>
 * <ul>
 *   <li><b>@Query JPQL</b> — used for {@link #searchByUsername} where a LIKE
 *       pattern is required. Pure JPQL (no {@code nativeQuery=true}) so Hibernate
 *       handles camelCase-to-snake_case column mapping automatically.</li>
 *   <li><b>Derived methods</b> — Spring Data derives the SQL from the method
 *       name. The {@code IsNull} / {@code IsNotNull} suffixes generate
 *       {@code WHERE field IS NULL} / {@code WHERE field IS NOT NULL}.</li>
 * </ul>
 */
@Hidden
@Repository
public interface ProfilesRepository extends JpaRepository<Profiles, String> {

    /**
     * Searches profiles by username with a case-insensitive partial match.
     *
     * <p>The caller (in {@link ProfilesService}) pre-builds the wildcard pattern
     * via {@code likePattern()} before calling this method, e.g. {@code "%xinil%"}.
     * Passing a concrete non-null {@link String} prevents Hibernate from
     * inferring {@code bytea} for a {@code null} parameter, which would cause
     * PostgreSQL to throw {@code function lower(bytea) does not exist}.</p>
     *
     * @param searchPattern pre-built lowercase LIKE pattern, e.g. {@code "%xinil%"}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of matching profiles
     */
    @Query("SELECT e FROM Profiles e WHERE LOWER(e.username) LIKE :searchPattern")
    Page<Profiles> searchByUsername(
            @Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Returns all profiles with an exact gender match.
     *
     * <p>Spring Data derives: {@code SELECT * FROM profiles WHERE gender = ?}.</p>
     *
     * @param gender   exact gender value to match
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching profiles
     */
    Page<Profiles> findByGender(String gender, Pageable pageable);

    /**
     * Returns all profiles with an exact location match.
     *
     * <p>Spring Data derives: {@code SELECT * FROM profiles WHERE location = ?}.</p>
     *
     * @param location exact location value to match
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching profiles
     */
    Page<Profiles> findByLocation(String location, Pageable pageable);

    /**
     * Returns all profiles where {@code gender} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of profiles with null gender
     */
    Page<Profiles> findByGenderIsNull(Pageable pageable);

    /**
     * Returns all profiles where {@code gender} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of profiles with a non-null gender
     */
    Page<Profiles> findByGenderIsNotNull(Pageable pageable);

    /**
     * Returns all profiles where {@code birthday} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of profiles with null birthday
     */
    Page<Profiles> findByBirthdayIsNull(Pageable pageable);

    /**
     * Returns all profiles where {@code birthday} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of profiles with a non-null birthday
     */
    Page<Profiles> findByBirthdayIsNotNull(Pageable pageable);

    /**
     * Returns all profiles where {@code location} is {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of profiles with null location
     */
    Page<Profiles> findByLocationIsNull(Pageable pageable);

    /**
     * Returns all profiles where {@code location} is not {@code NULL}.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated page of profiles with a non-null location
     */
    Page<Profiles> findByLocationIsNotNull(Pageable pageable);

    /**
     * Counts profiles where {@code gender} is {@code NULL}.
     *
     * @return count of profiles with null gender
     */
    long countByGenderIsNull();

    /**
     * Counts profiles where {@code birthday} is {@code NULL}.
     *
     * @return count of profiles with null birthday
     */
    long countByBirthdayIsNull();

    /**
     * Counts profiles where {@code location} is {@code NULL}.
     *
     * @return count of profiles with null location
     */
    long countByLocationIsNull();
}
