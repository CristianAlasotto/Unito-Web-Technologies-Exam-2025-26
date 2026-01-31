package com.example.dataserverspringboot.entities.recommendations;

import com.example.dataserverspringboot.entities.details.Details;

/**
 * DTO (Data Transfer Object) for recommendations with full details for both anime
 */
public class RecommendationWithDetails {
    private Integer malId;
    private Integer recommendationMalId;
    private Details sourceAnime;
    private Details recommendedAnime;

    // Constructor for JPQL
    public RecommendationWithDetails(Integer malId, Integer recommendationMalId, 
                                    Details sourceAnime, Details recommendedAnime) {
        this.malId = malId;
        this.recommendationMalId = recommendationMalId;
        this.sourceAnime = sourceAnime;
        this.recommendedAnime = recommendedAnime;
    }

    // Getters and Setters
    public Integer getMalId() {
        return malId;
    }

    public void setMalId(Integer malId) {
        this.malId = malId;
    }

    public Integer getRecommendationMalId() {
        return recommendationMalId;
    }

    public void setRecommendationMalId(Integer recommendationMalId) {
        this.recommendationMalId = recommendationMalId;
    }

    public Details getSourceAnime() {
        return sourceAnime;
    }

    public void setSourceAnime(Details sourceAnime) {
        this.sourceAnime = sourceAnime;
    }

    public Details getRecommendedAnime() {
        return recommendedAnime;
    }

    public void setRecommendedAnime(Details recommendedAnime) {
        this.recommendedAnime = recommendedAnime;
    }
}
