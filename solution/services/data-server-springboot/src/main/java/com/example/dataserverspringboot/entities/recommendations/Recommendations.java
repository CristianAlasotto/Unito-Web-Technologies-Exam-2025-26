package com.example.dataserverspringboot.entities.recommendations;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Schema(description = "Anime recommended entity")  // For entity
@Entity
@Table(name = "recommendations")
@IdClass(Recommendations.RecommendationsId.class)
public class Recommendations {
    
    @Id
    @Column(name = "mal_id")
    private Integer malId;
    
    @Id
    @Column(name = "recommendation_mal_id")
    private Integer recommendationMalId;

    // Constructors
    public Recommendations() {
    }

    public Recommendations(Integer malId, Integer recommendationMalId) {
        this.malId = malId;
        this.recommendationMalId = recommendationMalId;
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

    @Override
    public String toString() {
        return "Recommendations{" +
                "mal_id=" + malId +
                ", recommendation_mal_id=" + recommendationMalId +
                '}';
    }

    // Composite Key Class
    public static class RecommendationsId implements Serializable {
        private Integer malId;
        private Integer recommendationMalId;

        public RecommendationsId() {
        }

        public RecommendationsId(Integer malId, Integer recommendationMalId) {
            this.malId = malId;
            this.recommendationMalId = recommendationMalId;
        }

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecommendationsId that = (RecommendationsId) o;
            return Objects.equals(malId, that.malId) && 
                   Objects.equals(recommendationMalId, that.recommendationMalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(malId, recommendationMalId);
        }
    }
}
