package com.example.dataserverspringboot.entities.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
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
     * Update the score for a specific anime
     * @param malId The anime MAL ID
     * @param newScore The new score value
     * @return The updated Details object, or empty if not found
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
