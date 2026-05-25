package com.example.dataserverspringboot.entities.recommendations;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity representing a recommendation relationship between two anime.
 *
 * <p>Maps to the {@code recommendations} table in PostgreSQL. Each record
 * expresses that anime {@code mal_id} recommends anime {@code recommendation_mal_id}.
 * The table is self-referencing on {@code details(mal_id)}.</p>
 *
 * <p>The primary key is composite: {@code (mal_id, recommendation_mal_id)}.
 * The composite key class {@link RecommendationsId} is declared via
 * {@code @IdClass} and implements {@link Serializable} with {@code equals()}
 * and {@code hashCode()} as required by the JPA specification.</p>
 */
@Schema(description = "Anime recommendations junction entity (Relationship between two Anime)")
@Entity
@Table(name = "recommendations")
@IdClass(Recommendations.RecommendationsId.class)
public class Recommendations {

    /** Source anime MAL ID — first part of the composite primary key. */
    @Schema(description = "Source Anime MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "mal_id")
    private Integer malId;

    /** Recommended anime MAL ID — second part of the composite primary key. */
    @Schema(description = "Recommended Anime MyAnimeList ID (Composite Key)", example = "5")
    @Id
    @Column(name = "recommendation_mal_id")
    private Integer recommendationMalId;

    /** Required no-args constructor for JPA. */
    public Recommendations() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param malId               source anime MAL ID
     * @param recommendationMalId recommended anime MAL ID
     */
    public Recommendations(Integer malId, Integer recommendationMalId) {
        this.malId = malId;
        this.recommendationMalId = recommendationMalId;
    }

    /**
     * Returns the source anime MAL ID.
     *
     * @return source anime MAL ID
     */
    public Integer getMalId() { return malId; }

    /**
     * Sets the source anime MAL ID.
     *
     * @param malId source anime MAL ID
     */
    public void setMalId(Integer malId) { this.malId = malId; }

    /**
     * Returns the recommended anime MAL ID.
     *
     * @return recommended anime MAL ID
     */
    public Integer getRecommendationMalId() { return recommendationMalId; }

    /**
     * Sets the recommended anime MAL ID.
     *
     * @param recommendationMalId recommended anime MAL ID
     */
    public void setRecommendationMalId(Integer recommendationMalId) {
        this.recommendationMalId = recommendationMalId;
    }

    @Override
    public String toString() {
        return "Recommendations{mal_id=" + malId
                + ", recommendation_mal_id=" + recommendationMalId + '}';
    }

    /**
     * Composite primary key class for {@link Recommendations}.
     *
     * <p>Required by JPA when using {@code @IdClass}. Must implement
     * {@link Serializable} and override {@link #equals(Object)} and
     * {@link #hashCode()} so that the persistence context can correctly
     * identify entity instances by their composite key.</p>
     */
    public static class RecommendationsId implements Serializable {

        private Integer malId;
        private Integer recommendationMalId;

        /** Required no-args constructor for JPA. */
        public RecommendationsId() {}

        /**
         * Full constructor.
         *
         * @param malId               source anime MAL ID
         * @param recommendationMalId recommended anime MAL ID
         */
        public RecommendationsId(Integer malId, Integer recommendationMalId) {
            this.malId = malId;
            this.recommendationMalId = recommendationMalId;
        }

        /** @return source anime MAL ID */
        public Integer getMalId() { return malId; }

        /** @param malId source anime MAL ID */
        public void setMalId(Integer malId) { this.malId = malId; }

        /** @return recommended anime MAL ID */
        public Integer getRecommendationMalId() { return recommendationMalId; }

        /** @param recommendationMalId recommended anime MAL ID */
        public void setRecommendationMalId(Integer recommendationMalId) {
            this.recommendationMalId = recommendationMalId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecommendationsId that = (RecommendationsId) o;
            return Objects.equals(malId, that.malId)
                    && Objects.equals(recommendationMalId, that.recommendationMalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(malId, recommendationMalId);
        }
    }
}
