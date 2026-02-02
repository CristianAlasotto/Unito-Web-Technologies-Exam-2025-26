package com.example.dataserverspringboot.entities.profiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfilesService {

    @Autowired
    private ProfilesRepository repository;

    public Optional<Profiles> getById(String username) {
        return repository.findById(username);
    }

    public long count() {
        return repository.count();
    }

    public Page<Profiles> findWithFilters(String search, String gender, String location, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByUsername(search, pageable);
        }

        if (gender != null) {
            return repository.findByGender(gender, pageable);
        }

        if (location != null) {
            return repository.findByLocation(location, pageable);
        }

        return repository.findAll(pageable);
    }

    /**
     * Find records with filters including NULL/NOT NULL filters
     */
    public Page<Profiles> findWithFilters(String search, String gender, String location,
                                         String nullFilter, String notNullFilter,
                                         Pageable pageable) {
        
        // Handle NULL filter first (takes precedence)
        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        
        // Handle NOT NULL filter
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        
        // Fall back to regular filters
        return findWithFilters(search, gender, location, pageable);
    }

    /**
     * Handle NULL filtering for specific field
     */
    private Page<Profiles> handleNullFilter(String field, Pageable pageable) {
        switch (field.toLowerCase()) {
            case "gender":
                return repository.findByGenderIsNull(pageable);
            case "birthday":
                return repository.findByBirthdayIsNull(pageable);
            case "location":
                return repository.findByLocationIsNull(pageable);
            default:
                // Invalid field name, return all records
                return repository.findAll(pageable);
        }
    }

    /**
     * Handle NOT NULL filtering for specific field
     */
    private Page<Profiles> handleNotNullFilter(String field, Pageable pageable) {
        switch (field.toLowerCase()) {
            case "gender":
                return repository.findByGenderIsNotNull(pageable);
            case "birthday":
                return repository.findByBirthdayIsNotNull(pageable);
            case "location":
                return repository.findByLocationIsNotNull(pageable);
            default:
                // Invalid field name, return all records
                return repository.findAll(pageable);
        }
    }

    /**
     * Get statistics on NULL values
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("gender", repository.countByGenderIsNull());
        counts.put("birthday", repository.countByBirthdayIsNull());
        counts.put("location", repository.countByLocationIsNull());
        return counts;
    }
}
