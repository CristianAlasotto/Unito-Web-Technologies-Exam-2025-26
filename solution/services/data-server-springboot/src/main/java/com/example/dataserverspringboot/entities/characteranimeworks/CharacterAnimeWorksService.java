package com.example.dataserverspringboot.entities.characteranimeworks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CharacterAnimeWorksService {

    @Autowired
    private CharacterAnimeWorksRepository repository;

    public Optional<CharacterAnimeWorks> getById(CharacterAnimeWorks.CharacterAnimeWorksId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<CharacterAnimeWorks> findWithFilters(String search, String role, Integer character_mal_id, Integer anime_mal_id, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByRole(search, pageable);
        }

        if (role != null) {
            return repository.findByRole(role, pageable);
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
    public Page<CharacterAnimeWorks> findWithFilters(String search, String role, Integer character_mal_id, Integer anime_mal_id,
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
        return findWithFilters(search, role, character_mal_id, anime_mal_id, pageable);
    }

    /**
     * Handle NULL filtering for specific field
     */
    private Page<CharacterAnimeWorks> handleNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "character_name", "charactername" -> repository.findByCharacterNameIsNull(pageable);
            case "role" -> repository.findByRoleIsNull(pageable);
            default ->
                // Invalid field name, return all records
                    repository.findAll(pageable);
        };
    }

    /**
     * Handle NOT NULL filtering for specific field
     */
    private Page<CharacterAnimeWorks> handleNotNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "character_name", "charactername" -> repository.findByCharacterNameIsNotNull(pageable);
            case "role" -> repository.findByRoleIsNotNull(pageable);
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
        counts.put("character_name", repository.countByCharacterNameIsNull());
        counts.put("role", repository.countByRoleIsNull());
        return counts;
    }
}
