package com.example.dataserverspringboot.entities.recommendations;

import com.example.dataserverspringboot.entities.details.Details;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) for recommendations with full details for both anime
 */
@Schema(description = "DTO containing IDs and full details for both source and recommended anime")
public class RecommendationWithDetails {

    @Schema(description = "Source Anime ID", example = "1")
    private Integer malId;

    @Schema(description = "Recommended Anime ID", example = "5")
    private Integer recommendationMalId;

    @Schema(description = "Full details of the source anime")
    private Details sourceAnime;

    @Schema(description = "Full details of the recommended anime")
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