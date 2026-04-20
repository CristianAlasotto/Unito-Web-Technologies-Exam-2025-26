package com.example.dataserverspringboot.entities.details;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Hidden
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

    /**
     * Converts a raw search string into a lowercase LIKE pattern ("%value%").
     * Returns null if the input is null or blank, so the repository can skip
     * the filter entirely with ":param IS NULL" checks in JPQL.
     *
     * This is the key fix for the "lower(bytea)" PostgreSQL error: by building
     * the wildcard here (in Java) and passing a concrete non-null String,
     * Hibernate always infers VARCHAR correctly — no CAST needed in the query.
     */
    private String likePattern(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.toLowerCase() + "%";
    }

    /**
     * Find records with filters - COMBINED (not priority-based)
     * This method now combines ALL filters with AND logic
     */
    public Page<Details> findWithFilters(String search, String type, Integer year, String status,
                                         String rating, String source, String genres, Integer episodes,
                                         Pageable pageable) {

        // Pre-build wildcard patterns (null when not provided)
        String searchPattern = likePattern(search);
        String genresPattern = likePattern(genres);

        // Count active filters
        int filterCount = 0;
        if (searchPattern != null) filterCount++;
        if (type != null) filterCount++;
        if (year != null) filterCount++;
        if (status != null) filterCount++;
        if (rating != null) filterCount++;
        if (source != null) filterCount++;
        if (genresPattern != null) filterCount++;
        if (episodes != null) filterCount++;

        // If multiple filters, use combined query
        if (filterCount > 1) {
            return repository.findWithCombinedFilters(
                    searchPattern, type, year, status, rating, source, genresPattern, episodes, pageable);
        }

        // If single filter or no filters, use dedicated methods
        if (searchPattern != null) {
            return repository.searchByTitle(searchPattern, pageable);
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
        if (genresPattern != null) {
            return repository.findByGenresContaining(genresPattern, pageable);
        }
        if (episodes != null) {
            return repository.findByEpisodes(episodes, pageable);
        }

        // No filters - return all
        return repository.findAll(pageable);
    }

    /**
     * Find records with filters including NULL/NOT NULL filters
     */
    public Page<Details> findWithFilters(String search, String type, Integer year, String status,
                                         String rating, String source, String genres, Integer episodes,
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

        // Fall back to regular combined filters
        return findWithFilters(search, type, year, status, rating, source, genres, episodes, pageable);
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
            case "favorites" -> repository.findByFavoritesIsNull(pageable);
            default -> repository.findAll(pageable);
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
            case "favorites" -> repository.findByFavoritesIsNotNull(pageable);
            default -> repository.findAll(pageable);
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
        counts.put("favorites", repository.countByFavoritesIsNull());
        return counts;
    }

    /**
     * Update the score for a specific anime
     * @param malId The MAL ID of the anime
     * @param newScore The new score value (0.00 to 10.00)
     * @return Updated Details entity, or empty if not found
     */
    public Optional<Details> updateScore(Integer malId, BigDecimal newScore) {
        Optional<Details> detailsOpt = repository.findById(malId);

        if (detailsOpt.isPresent()) {
            Details details = detailsOpt.get();
            details.setScore(newScore);
            Details updated = repository.save(details);
            return Optional.of(updated);
        }

        return Optional.empty();
    }
}