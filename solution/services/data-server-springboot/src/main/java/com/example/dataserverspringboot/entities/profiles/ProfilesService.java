package com.example.dataserverspringboot.entities.profiles;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Hidden
@Service
public class ProfilesService {

    @Autowired
    private ProfilesRepository repository;

    // ── Basic lookups ─────────────────────────────────────────────────────────

    /**
     * Fetch a single profile by username.
     * Returns Optional<ProfilesDTO> — raw entity never leaves the service layer.
     */
    public Optional<ProfilesDTO> getById(String username) {
        return repository.findById(username)
                         .map(ProfilesDTO::fromEntity);
    }

    public boolean existsById(String username) {
        return repository.existsById(username);
    }

    public long count() {
        return repository.count();
    }

    // ── Filter helpers ────────────────────────────────────────────────────────

    /**
     * Converts a raw search string into a lowercase LIKE pattern ("%value%").
     * Returns null if the input is null or blank.
     * Same fix as DetailsService.likePattern() — avoids lower(bytea) bug.
     */
    private String likePattern(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.toLowerCase() + "%";
    }

    /**
     * Find profiles with optional filters, returning a paginated page of DTOs.
     * The Page<Profiles> result is mapped to Page<ProfilesDTO> so the raw
     * JPA entity never leaves the service layer.
     */
    public Page<ProfilesDTO> findWithFilters(
            String search, String gender, String location, Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByUsername(searchPattern, pageable)
                    .map(ProfilesDTO::fromEntity);
        }
        if (gender != null) {
            return repository.findByGender(gender, pageable)
                    .map(ProfilesDTO::fromEntity);
        }
        if (location != null) {
            return repository.findByLocation(location, pageable)
                    .map(ProfilesDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(ProfilesDTO::fromEntity);
    }

    /**
     * Overload that also handles IS NULL / IS NOT NULL filters.
     */
    public Page<ProfilesDTO> findWithFilters(
            String search, String gender, String location,
            String nullFilter, String notNullFilter,
            Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, gender, location, pageable);
    }

    /**
     * Route IS NULL filter to the correct derived repository method.
     */
    private Page<ProfilesDTO> handleNullFilter(String field, Pageable pageable) {
        Page<Profiles> result = switch (field.toLowerCase()) {
            case "gender"   -> repository.findByGenderIsNull(pageable);
            case "birthday" -> repository.findByBirthdayIsNull(pageable);
            case "location" -> repository.findByLocationIsNull(pageable);
            default         -> repository.findAll(pageable);
        };
        return result.map(ProfilesDTO::fromEntity);
    }

    /**
     * Route IS NOT NULL filter to the correct derived repository method.
     */
    private Page<ProfilesDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<Profiles> result = switch (field.toLowerCase()) {
            case "gender"   -> repository.findByGenderIsNotNull(pageable);
            case "birthday" -> repository.findByBirthdayIsNotNull(pageable);
            case "location" -> repository.findByLocationIsNotNull(pageable);
            default         -> repository.findAll(pageable);
        };
        return result.map(ProfilesDTO::fromEntity);
    }

    /**
     * Returns a map of field names to the count of records where that field is NULL.
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("gender",   repository.countByGenderIsNull());
        counts.put("birthday", repository.countByBirthdayIsNull());
        counts.put("location", repository.countByLocationIsNull());
        return counts;
    }
}
