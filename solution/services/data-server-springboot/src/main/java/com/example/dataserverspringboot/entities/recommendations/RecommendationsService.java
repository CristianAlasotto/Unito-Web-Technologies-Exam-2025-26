package com.example.dataserverspringboot.entities.recommendations;

import com.example.dataserverspringboot.entities.details.Details;
import com.example.dataserverspringboot.entities.details.DetailsRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Hidden
@Service
public class RecommendationsService {

    @Autowired
    private RecommendationsRepository repository;
    
    @Autowired
    private DetailsRepository detailsRepository;

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
    
    /**
     * Find all recommendations with full details for both source and recommended anime (JPA)
     */
    public Page<RecommendationWithDetails> findAllWithDetails(Integer malId, Integer recommendationMalId, Pageable pageable) {
        // Get recommendations with filters
        Page<Recommendations> recommendations;
        
        if (malId != null && recommendationMalId != null) {
            // Both filters
            recommendations = repository.findByMalIdAndRecommendationMalId(malId, recommendationMalId, pageable);
        } else if (malId != null) {
            recommendations = repository.findByMalId(malId, pageable);
        } else if (recommendationMalId != null) {
            recommendations = repository.findByRecommendationMalId(recommendationMalId, pageable);
        } else {
            recommendations = repository.findAll(pageable);
        }
        
        // Fetch full details for each recommendation
        List<RecommendationWithDetails> results = new ArrayList<>();
        for (Recommendations rec : recommendations.getContent()) {
            Optional<Details> sourceAnime = detailsRepository.findById(rec.getMalId());
            Optional<Details> recommendedAnime = detailsRepository.findById(rec.getRecommendationMalId());
            
            results.add(new RecommendationWithDetails(
                rec.getMalId(),
                rec.getRecommendationMalId(),
                sourceAnime.orElse(null),
                recommendedAnime.orElse(null)
            ));
        }
        
        return new PageImpl<>(results, pageable, recommendations.getTotalElements());
    }
}
