package com.example.dataserverspringboot.entities.recommendations;

import com.example.dataserverspringboot.entities.details.Details;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Data Transfer Object for the {@link Recommendations} entity.
 *
 * <p>Decouples the internal JPA entity from the data exposed through the REST API.
 * The raw {@link Recommendations} entity is never serialised to JSON — only this
 * DTO leaves the service layer.</p>
 *
 * <p>Two factory methods are provided:</p>
 * <ul>
 *   <li>{@link #fromEntity(Recommendations)} — simple pair containing only
 *       {@code mal_id} and {@code recommendation_mal_id}. Used by
 *       {@code GET /api/recommendations}.</li>
 *   <li>{@link #fromEntityWithDetails(Recommendations, Details, Details)} — enriched
 *       pair that also embeds key fields from both the source and the recommended
 *       anime {@link Details} entities. Used by
 *       {@code GET /api/recommendations/details}. The raw {@link Details} entities
 *       are consumed inside the factory method and never stored as fields.</li>
 * </ul>
 *
 * <p>All getters carry {@code @JsonProperty} annotations so that Jackson
 * serialises camelCase Java field names as snake_case JSON keys automatically,
 * without any manual {@code Map} construction in the controller.</p>
 */
@Schema(description = "Recommendations data transfer object")
public class RecommendationsDTO {

    @Schema(description = "Source anime MAL ID", example = "1")
    private Integer malId;

    @Schema(description = "Recommended anime MAL ID", example = "5")
    private Integer recommendationMalId;

    @Schema(description = "Source anime title")
    private String sourceTitle;

    @Schema(description = "Source anime Japanese title")
    private String sourceTitleJapanese;

    @Schema(description = "Source anime type", example = "TV")
    private String sourceType;

    @Schema(description = "Source anime score")
    private BigDecimal sourceScore;

    @Schema(description = "Source anime cover image URL")
    private String sourceImageUrl;

    @Schema(description = "Source anime episode count")
    private Integer sourceEpisodes;

    @Schema(description = "Source anime broadcast year")
    private Integer sourceYear;

    @Schema(description = "Source anime airing status")
    private String sourceStatus;

    @Schema(description = "Recommended anime title")
    private String recommendationTitle;

    @Schema(description = "Recommended anime Japanese title")
    private String recommendationTitleJapanese;

    @Schema(description = "Recommended anime type", example = "TV")
    private String recommendationType;

    @Schema(description = "Recommended anime score")
    private BigDecimal recommendationScore;

    @Schema(description = "Recommended anime cover image URL")
    private String recommendationImageUrl;

    @Schema(description = "Recommended anime episode count")
    private Integer recommendationEpisodes;

    @Schema(description = "Recommended anime broadcast year")
    private Integer recommendationYear;

    @Schema(description = "Recommended anime airing status")
    private String recommendationStatus;

    /** {@code true} when the enriched factory method was used. */
    private boolean withDetails = false;

    /** Private constructor — use the static factory methods. */
    private RecommendationsDTO() {}

    /**
     * Simple factory: builds a DTO containing only the two IDs.
     *
     * @param r the {@link Recommendations} JPA entity
     * @return a DTO with only {@code malId} and {@code recommendationMalId} populated
     */
    public static RecommendationsDTO fromEntity(Recommendations r) {
        RecommendationsDTO dto = new RecommendationsDTO();
        dto.malId               = r.getMalId();
        dto.recommendationMalId = r.getRecommendationMalId();
        return dto;
    }

    /**
     * Enriched factory: builds a DTO containing the two IDs plus key fields
     * extracted from both {@link Details} entities.
     *
     * <p>Either {@code sourceAnime} or {@code recommendedAnime} may be {@code null}
     * (orphan recommendation row), in which case the corresponding fields remain
     * {@code null} in the DTO. The raw {@link Details} entities are consumed here
     * and never stored as DTO fields.</p>
     *
     * @param r               the {@link Recommendations} JPA entity
     * @param sourceAnime     the {@link Details} entity for the source anime (may be {@code null})
     * @param recommendedAnime the {@link Details} entity for the recommended anime (may be {@code null})
     * @return a fully enriched DTO ready for serialisation
     */
    public static RecommendationsDTO fromEntityWithDetails(
            Recommendations r, Details sourceAnime, Details recommendedAnime) {

        RecommendationsDTO dto = new RecommendationsDTO();
        dto.malId               = r.getMalId();
        dto.recommendationMalId = r.getRecommendationMalId();
        dto.withDetails         = true;

        if (sourceAnime != null) {
            dto.sourceTitle         = sourceAnime.getTitle();
            dto.sourceTitleJapanese = sourceAnime.getTitleJapanese();
            dto.sourceType          = sourceAnime.getType();
            dto.sourceScore         = sourceAnime.getScore();
            dto.sourceImageUrl      = sourceAnime.getImageUrl();
            dto.sourceEpisodes      = sourceAnime.getEpisodes();
            dto.sourceYear          = sourceAnime.getYear();
            dto.sourceStatus        = sourceAnime.getStatus();
        }

        if (recommendedAnime != null) {
            dto.recommendationTitle         = recommendedAnime.getTitle();
            dto.recommendationTitleJapanese = recommendedAnime.getTitleJapanese();
            dto.recommendationType          = recommendedAnime.getType();
            dto.recommendationScore         = recommendedAnime.getScore();
            dto.recommendationImageUrl      = recommendedAnime.getImageUrl();
            dto.recommendationEpisodes      = recommendedAnime.getEpisodes();
            dto.recommendationYear          = recommendedAnime.getYear();
            dto.recommendationStatus        = recommendedAnime.getStatus();
        }

        return dto;
    }

    /** @return source anime MAL ID */
    @JsonProperty("mal_id")
    public Integer getMalId() { return malId; }

    /** @return recommended anime MAL ID */
    @JsonProperty("recommendation_mal_id")
    public Integer getRecommendationMalId() { return recommendationMalId; }

    /** @return {@code true} if enriched details fields are populated */
    @JsonProperty("with_details")
    public boolean isWithDetails() { return withDetails; }

    /** @return source anime title */
    @JsonProperty("source_title")
    public String getSourceTitle() { return sourceTitle; }

    /** @return source anime Japanese title */
    @JsonProperty("source_title_japanese")
    public String getSourceTitleJapanese() { return sourceTitleJapanese; }

    /** @return source anime type (e.g. TV, Movie) */
    @JsonProperty("source_type")
    public String getSourceType() { return sourceType; }

    /** @return source anime score */
    @JsonProperty("source_score")
    public BigDecimal getSourceScore() { return sourceScore; }

    /** @return source anime cover image URL */
    @JsonProperty("source_image_url")
    public String getSourceImageUrl() { return sourceImageUrl; }

    /** @return source anime episode count */
    @JsonProperty("source_episodes")
    public Integer getSourceEpisodes() { return sourceEpisodes; }

    /** @return source anime broadcast year */
    @JsonProperty("source_year")
    public Integer getSourceYear() { return sourceYear; }

    /** @return source anime airing status */
    @JsonProperty("source_status")
    public String getSourceStatus() { return sourceStatus; }

    /** @return recommended anime title */
    @JsonProperty("recommendation_title")
    public String getRecommendationTitle() { return recommendationTitle; }

    /** @return recommended anime Japanese title */
    @JsonProperty("recommendation_title_japanese")
    public String getRecommendationTitleJapanese() { return recommendationTitleJapanese; }

    /** @return recommended anime type (e.g. TV, Movie) */
    @JsonProperty("recommendation_type")
    public String getRecommendationType() { return recommendationType; }

    /** @return recommended anime score */
    @JsonProperty("recommendation_score")
    public BigDecimal getRecommendationScore() { return recommendationScore; }

    /** @return recommended anime cover image URL */
    @JsonProperty("recommendation_image_url")
    public String getRecommendationImageUrl() { return recommendationImageUrl; }

    /** @return recommended anime episode count */
    @JsonProperty("recommendation_episodes")
    public Integer getRecommendationEpisodes() { return recommendationEpisodes; }

    /** @return recommended anime broadcast year */
    @JsonProperty("recommendation_year")
    public Integer getRecommendationYear() { return recommendationYear; }

    /** @return recommended anime airing status */
    @JsonProperty("recommendation_status")
    public String getRecommendationStatus() { return recommendationStatus; }
}
