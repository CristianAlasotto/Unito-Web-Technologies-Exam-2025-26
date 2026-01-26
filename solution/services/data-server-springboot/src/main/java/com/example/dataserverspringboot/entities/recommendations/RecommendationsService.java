package com.example.dataserverspringboot.entities.recommendations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class RecommendationsService {

    @Autowired
    private RecommendationsRepository repository;

    public Optional<Recommendations> getById(Recommendations.RecommendationsId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<Recommendations> findWithFilters(String search, Integer malId, Integer recommendationMalId, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByMalId(search, pageable);
        }

        if (malId != null) {
            return repository.findByMalId(malId, pageable);
        }

        if (recommendationMalId != null) {
            return repository.findByRecommendationMalId(recommendationMalId, pageable);
        }

        return repository.findAll(pageable);
    }
}
