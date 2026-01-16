package com.example.dataserverspringboot.entities.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DetailsService {

    @Autowired
    private DetailsRepository repository;

    /**
     * Get anime by ID
     */
    public Optional<Details> getAnimeById(Integer mal_id) {
        return repository.findById(mal_id);
    }

    /**
     * Count all anime
     */
    public long countAllAnime() {
        return repository.count();
    }

    /**
     * Find with filters and pagination
     * Supports: type, year, status, rating, source, search
     */
    public Page<Details> findWithFilters(
            String type,
            Integer year,
            String status,
            String rating,
            String source,
            String search,
            Pageable pageable) {

        // If search is provided, use search query
        if (search != null && !search.isEmpty()) {
            return repository.searchByTitle(search, pageable);
        }

        // Apply filters
        if (type != null && year != null) {
            return repository.findByTypeAndYear(type, year, pageable);
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

        // No filters - return all
        return repository.findAll(pageable);
    }
}