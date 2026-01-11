package com.example.dataserverspringboot.entities.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Details entity
 * Contains business logic for anime operations
 */
@Service
public class DetailsService {

    @Autowired
    private DetailsRepository repository;

    /**
     * Get all anime from database
     * Used by: GET /api/anime
     */
    public List<Details> getAllAnime() {
        return repository.findAll();
    }

    /**
     * Get a single anime by mal_id
     * Used by: GET /api/anime/{id}
     */
    public Optional<Details> getAnimeById(Integer mal_id) {
        return repository.findById(mal_id);
    }

    /**
     * Count total number of anime in database
     * Used by: GET /api/anime/stats
     */
    public long countAllAnime() {
        return repository.count();
    }

    /**
     * Search anime by title (case-insensitive partial match)
     * Used by: GET /api/anime/search?title=xxx
     */
    public List<Details> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        return repository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Get anime by type (TV, Movie, OVA, etc.)
     * Used by: GET /api/anime/type/{type}
     */
    public List<Details> getByType(String type) {
        return repository.findByType(type);
    }

    /**
     * Get top 10 highest rated anime
     * Used by: GET /api/anime/top-rated
     */
    public List<Details> getTopRated() {
        return repository.findTop10ByOrderByScoreDesc();
    }

    /**
     * Get anime by genre
     * Used by: GET /api/anime/genre/{genre}
     */
    public List<Details> getByGenre(String genre) {
        return repository.findByGenreContaining(genre);
    }

    /**
     * Get anime within a score range
     * Used by: GET /api/anime/score?minScore=8&maxScore=10
     */
    public List<Details> getByScoreRange(Double minScore, Double maxScore) {
        if (minScore < 0 || maxScore > 10) {
            throw new IllegalArgumentException("Score must be between 0 and 10");
        }
        if (minScore > maxScore) {
            throw new IllegalArgumentException("Min score cannot be greater than max score");
        }
        return repository.findByScoreRange(minScore, maxScore);
    }
}