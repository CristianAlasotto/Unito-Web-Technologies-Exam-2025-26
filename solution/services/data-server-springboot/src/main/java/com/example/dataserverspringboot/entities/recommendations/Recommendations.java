package com.example.dataserverspringboot.entities.recommendations;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "recommendations")
@IdClass(Recommendations.RecommendationsId.class)
public class Recommendations {
    
    @Id
    @Column(name = "mal_id")
    private Integer mal_id;
    
    @Id
    @Column(name = "recommendation_mal_id")
    private Integer recommendation_mal_id;

    // Constructors
    public Recommendations() {
    }

    public Recommendations(Integer mal_id, Integer recommendation_mal_id) {
        this.mal_id = mal_id;
        this.recommendation_mal_id = recommendation_mal_id;
    }

    // Getters and Setters
    public Integer getMal_id() {
        return mal_id;
    }

    public void setMal_id(Integer mal_id) {
        this.mal_id = mal_id;
    }

    public Integer getRecommendation_mal_id() {
        return recommendation_mal_id;
    }

    public void setRecommendation_mal_id(Integer recommendation_mal_id) {
        this.recommendation_mal_id = recommendation_mal_id;
    }

    @Override
    public String toString() {
        return "Recommendations{" +
                "mal_id=" + mal_id +
                ", recommendation_mal_id=" + recommendation_mal_id +
                '}';
    }

    // Composite Key Class
    public static class RecommendationsId implements Serializable {
        private Integer mal_id;
        private Integer recommendation_mal_id;

        public RecommendationsId() {
        }

        public RecommendationsId(Integer mal_id, Integer recommendation_mal_id) {
            this.mal_id = mal_id;
            this.recommendation_mal_id = recommendation_mal_id;
        }

        public Integer getMal_id() {
            return mal_id;
        }

        public void setMal_id(Integer mal_id) {
            this.mal_id = mal_id;
        }

        public Integer getRecommendation_mal_id() {
            return recommendation_mal_id;
        }

        public void setRecommendation_mal_id(Integer recommendation_mal_id) {
            this.recommendation_mal_id = recommendation_mal_id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecommendationsId that = (RecommendationsId) o;
            return Objects.equals(mal_id, that.mal_id) && 
                   Objects.equals(recommendation_mal_id, that.recommendation_mal_id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mal_id, recommendation_mal_id);
        }
    }
}
