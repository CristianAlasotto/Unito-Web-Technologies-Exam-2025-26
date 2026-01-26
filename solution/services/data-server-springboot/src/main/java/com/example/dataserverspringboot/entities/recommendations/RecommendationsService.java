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

    public Page<Recommendations> findWithFilters(String search, Integer mal_id, Integer recommendation_mal_id, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByMalId(search, pageable);
        }

        if (mal_id != null) {
            return repository.findByMalId(mal_id, pageable);
        }

        if (recommendation_mal_id != null) {
            return repository.findByRecommendationMalId(recommendation_mal_id, pageable);
        }

        return repository.findAll(pageable);
    }
}
