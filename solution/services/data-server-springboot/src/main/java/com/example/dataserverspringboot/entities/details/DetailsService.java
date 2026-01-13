package com.example.dataserverspringboot.entities.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
     * Get first 10 anime from database
     * Used by: GET /api/anime
     */
    public List<Details> getAllAnime() {
        // Return only first 10 records
        Pageable limit = PageRequest.of(0, 10);
        return repository.findAll(limit).getContent();
    }

    /**
     * Get a single anime by its mal_id
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
     * Search anime by title (limited to 10 results)
     */
    public List<Details> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        Pageable limit = PageRequest.of(0, 10);
        return repository.findByTitleContainingIgnoreCase(title, limit);
    }

    /**
     * Get anime by type (limited to 10 results)
     */
    public List<Details> getByType(String type) {
        Pageable limit = PageRequest.of(0, 10);
        return repository.findByType(type, limit);
    }

    /**
     * Get top 10 highest rated anime
     */
    public List<Details> getTopRated() {
        return repository.findTop10ByOrderByScoreDesc();
    }

    /**
     * Get anime by genre (limited to 10 results)
     */
    public List<Details> getByGenre(String genre) {
        Pageable limit = PageRequest.of(0, 10);
        return repository.findByGenreContaining(genre, limit);
    }

    /**
     * Get anime within a score range (limited to 10 results)
     */
    public List<Details> getByScoreRange(Double minScore, Double maxScore) {
        if (minScore < 0 || maxScore > 10) {
            throw new IllegalArgumentException("Score must be between 0 and 10");
        }
        if (minScore > maxScore) {
            throw new IllegalArgumentException("Min score cannot be greater than max score");
        }
        Pageable limit = PageRequest.of(0, 10);
        return repository.findByScoreRange(minScore, maxScore, limit);
    }
}