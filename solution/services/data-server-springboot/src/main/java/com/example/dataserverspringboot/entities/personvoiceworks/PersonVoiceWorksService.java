package com.example.dataserverspringboot.entities.personvoiceworks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PersonVoiceWorksService {

    @Autowired
    private PersonVoiceWorksRepository repository;

    public Optional<PersonVoiceWorks> getById(PersonVoiceWorks.PersonVoiceWorksId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<PersonVoiceWorks> findWithFilters(String search, String language, String role, Integer person_mal_id, Integer character_mal_id, Integer anime_mal_id, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByLanguage(search, pageable);
        }

        if (language != null) {
            return repository.findByLanguage(language, pageable);
        }

        if (role != null) {
            return repository.findByRole(role, pageable);
        }

        if (person_mal_id != null) {
            return repository.findByPersonMalId(person_mal_id, pageable);
        }

        if (character_mal_id != null) {
            return repository.findByCharacterMalId(character_mal_id, pageable);
        }

        if (anime_mal_id != null) {
            return repository.findByAnimeMalId(anime_mal_id, pageable);
        }

        return repository.findAll(pageable);
    }

    /**
     * Find records with filters including NULL/NOT NULL filters
     */
    public Page<PersonVoiceWorks> findWithFilters(String search, String language, String role, 
                                                  Integer person_mal_id, Integer character_mal_id, Integer anime_mal_id,
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
        return findWithFilters(search, language, role, person_mal_id, character_mal_id, anime_mal_id, pageable);
    }

    /**
     * Handle NULL filtering for specific field
     */
    private Page<PersonVoiceWorks> handleNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "role" -> repository.findByRoleIsNull(pageable);
            case "language" -> repository.findByLanguageIsNull(pageable);
            default ->
                // Invalid field name, return all records
                    repository.findAll(pageable);
        };
    }

    /**
     * Handle NOT NULL filtering for specific field
     */
    private Page<PersonVoiceWorks> handleNotNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "role" -> repository.findByRoleIsNotNull(pageable);
            case "language" -> repository.findByLanguageIsNotNull(pageable);
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
        counts.put("role", repository.countByRoleIsNull());
        counts.put("language", repository.countByLanguageIsNull());
        return counts;
    }
}
