package com.example.dataserverspringboot.entities.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DetailsService {

    @Autowired
    private DetailsRepository repository;

    public Optional<Details> getById(Integer malId) {
        return repository.findById(malId);
    }

    public long count() {
        return repository.count();
    }

    public Page<Details> findWithFilters(String search, String type, Integer year, String status, String rating, String source, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByTitle(search, pageable);
        }

        if (type != null) {
            return repository.findByType(type, pageable);
        }

        if (year != null) {
            return repository.findByYear(year, pageable);
        }

        if (status != null) {
            return repository.findByStatus(status, pageable);
        }

        if (rating != null) {
            return repository.findByRating(rating, pageable);
        }

        if (source != null) {
            return repository.findBySource(source, pageable);
        }

        return repository.findAll(pageable);
    }

    /**
     * Find records with filters including NULL/NOT NULL filters
     */
    public Page<Details> findWithFilters(String search, String type, Integer year, String status, 
                                        String rating, String source, 
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
        return findWithFilters(search, type, year, status, rating, source, pageable);
    }

    /**
     * Handle NULL filtering for specific field
     */
    private Page<Details> handleNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "synopsis" -> repository.findBySynopsisIsNull(pageable);
            case "score" -> repository.findByScoreIsNull(pageable);
            case "end_date", "enddate" -> repository.findByEndDateIsNull(pageable);
            case "title_japanese", "titlejapanese" -> repository.findByTitleJapaneseIsNull(pageable);
            case "season" -> repository.findBySeasonIsNull(pageable);
            default ->
                // Invalid field name, return all records
                    repository.findAll(pageable);
        };
    }

    /**
     * Handle NOT NULL filtering for specific field
     */
    private Page<Details> handleNotNullFilter(String field, Pageable pageable) {
        return switch (field.toLowerCase()) {
            case "synopsis" -> repository.findBySynopsisIsNotNull(pageable);
            case "score" -> repository.findByScoreIsNotNull(pageable);
            case "end_date", "enddate" -> repository.findByEndDateIsNotNull(pageable);
            case "title_japanese", "titlejapanese" -> repository.findByTitleJapaneseIsNotNull(pageable);
            case "season" -> repository.findBySeasonIsNotNull(pageable);
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
        counts.put("synopsis", repository.countBySynopsisIsNull());
        counts.put("score", repository.countByScoreIsNull());
        counts.put("end_date", repository.countByEndDateIsNull());
        counts.put("title_japanese", repository.countByTitleJapaneseIsNull());
        counts.put("season", repository.countBySeasonIsNull());
        return counts;
    }
}
