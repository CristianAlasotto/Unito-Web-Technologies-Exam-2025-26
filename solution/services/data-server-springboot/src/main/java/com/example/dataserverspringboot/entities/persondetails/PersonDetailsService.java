package com.example.dataserverspringboot.entities.persondetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PersonDetailsService {

    @Autowired
    private PersonDetailsRepository repository;

    public Optional<PersonDetails> getById(Integer person_mal_id) {
        return repository.findById(person_mal_id);
    }

    public long count() {
        return repository.count();
    }

    public Page<PersonDetails> findWithFilters(String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByName(search, pageable);
        }

        return repository.findAll(pageable);
    }

    /**
     * Find records with filters including NULL/NOT NULL filters
     */
    public Page<PersonDetails> findWithFilters(String search,
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
        return findWithFilters(search, pageable);
    }

    /**
     * Handle NULL filtering for specific field
     */
    private Page<PersonDetails> handleNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "website_url", "websiteurl" -> repository.findByWebsiteUrlIsNull(pageable);
            case "given_name", "givenname" -> repository.findByGivenNameIsNull(pageable);
            case "family_name", "familyname" -> repository.findByFamilyNameIsNull(pageable);
            case "birthday" -> repository.findByBirthdayIsNull(pageable);
            case "relevant_location", "relevantlocation" -> repository.findByRelevantLocationIsNull(pageable);
            default ->
                // Invalid field name, return all records
                    repository.findAll(pageable);
        };
    }

    /**
     * Handle NOT NULL filtering for specific field
     */
    private Page<PersonDetails> handleNotNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "website_url", "websiteurl" -> repository.findByWebsiteUrlIsNotNull(pageable);
            case "given_name", "givenname" -> repository.findByGivenNameIsNotNull(pageable);
            case "family_name", "familyname" -> repository.findByFamilyNameIsNotNull(pageable);
            case "birthday" -> repository.findByBirthdayIsNotNull(pageable);
            case "relevant_location", "relevantlocation" -> repository.findByRelevantLocationIsNotNull(pageable);
            default ->
                // Invalid field name, return all records
                    repository.findAll(pageable);
        };
    }

    /**
     * Get statistics on NULL values
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("website_url", repository.countByWebsiteUrlIsNull());
        counts.put("given_name", repository.countByGivenNameIsNull());
        counts.put("family_name", repository.countByFamilyNameIsNull());
        counts.put("birthday", repository.countByBirthdayIsNull());
        counts.put("relevant_location", repository.countByRelevantLocationIsNull());
        return counts;
    }
}
