package com.example.dataserverspringboot.entities.characters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Service
public class CharactersService {

    @Autowired
    private CharactersRepository repository;

    public Optional<Characters> getById(Integer characterMalId) {
        return repository.findById(characterMalId);
    }

    public long count() {
        return repository.count();
    }

    public Page<Characters> findWithFilters(String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByName(search, pageable);
        }

        return repository.findAll(pageable);
    }

    /**
     * Find records with filters including NULL/NOT NULL filters
     */
    public Page<Characters> findWithFilters(String search, 
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
    private Page<Characters> handleNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "name_kanji", "namekanji" -> repository.findByNameKanjiIsNull(pageable);
            case "image" -> repository.findByImageIsNull(pageable);
            case "about" -> repository.findByAboutIsNull(pageable);
            default ->
                // Invalid field name, return all records
                    repository.findAll(pageable);
        };
    }

    /**
     * Handle NOT NULL filtering for specific field
     */
    private Page<Characters> handleNotNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "name_kanji", "namekanji" -> repository.findByNameKanjiIsNotNull(pageable);
            case "image" -> repository.findByImageIsNotNull(pageable);
            case "about" -> repository.findByAboutIsNotNull(pageable);
            case "favorites" -> repository.findByFavoritesIsNotNull(pageable);
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
        counts.put("name_kanji", repository.countByNameKanjiIsNull());
        counts.put("image", repository.countByImageIsNull());
        counts.put("about", repository.countByAboutIsNull());
        counts.put("favorites", repository.countByFavoritesIsNull());
        return counts;
    }
}
