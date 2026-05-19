package com.example.dataserverspringboot.entities.recommendations;

import com.example.dataserverspringboot.entities.details.Details;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Data Transfer Object for the Recommendations entity.
 *
 * PURPOSE:
 *   Replaces direct exposure of the Recommendations JPA entity in the API.
 *   The raw entity is never serialised to JSON — only this DTO leaves the service layer.
 *
 * TWO FACTORY METHODS:
 *   - fromEntity(Recommendations)       → simple pair (mal_id + recommendation_mal_id)
 *   - fromEntityWithDetails(...)        → enriched pair that also embeds key fields from
 *                                         both the source and recommended anime Details entities.
 *                                         Replaces RecommendationWithDetails, which held raw
 *                                         Details entities and forced the controller to call
 *                                         getters on the JPA entity directly.
 *
 * DESIGN NOTE ON RecommendationWithDetails:
 *   The old RecommendationWithDetails DTO held two full Details JPA entities as fields.
 *   This meant the controller (convertToDetailsMap) was calling entity getters directly,
 *   which defeats the purpose of a DTO. This class fixes that: only the fields needed by
 *   the API response are extracted here, and the controller receives plain values.
 */
@Schema(description = "Recommendations data transfer object")
public class RecommendationsDTO {

    // ── Core recommendation fields ────────────────────────────────────────────

    @Schema(description = "Source anime MAL ID", example = "1")
    private Integer malId;

    @Schema(description = "Recommended anime MAL ID", example = "5")
    private Integer recommendationMalId;

    // ── Enriched source anime fields (populated only by fromEntityWithDetails) ─

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

    // ── Enriched recommended anime fields ────────────────────────────────────

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

    // ── Flag to know if enriched fields are present ───────────────────────────

    private boolean withDetails = false;

    // ── Private constructor — use factory methods ─────────────────────────────
    private RecommendationsDTO() {}

    /**
     * Simple factory: only the two IDs — used by GET /api/recommendations.
     *
     * @param r the Recommendations JPA entity
     * @return a DTO with only malId and recommendationMalId populated
     */
    public static RecommendationsDTO fromEntity(Recommendations r) {
        RecommendationsDTO dto = new RecommendationsDTO();
        dto.malId               = r.getMalId();
        dto.recommendationMalId = r.getRecommendationMalId();
        return dto;
    }

    /**
     * Enriched factory: IDs plus key fields from both Details entities.
     * Used by GET /api/recommendations/details.
     *
     * Either sourceAnime or recommendedAnime may be null (orphan recommendation),
     * in which case the corresponding fields remain null in the DTO.
     *
     * @param r               the Recommendations JPA entity
     * @param sourceAnime     the Details entity for the source anime (may be null)
     * @param recommendedAnime the Details entity for the recommended anime (may be null)
     * @return a fully enriched DTO ready for serialisation
     */
    public static RecommendationsDTO fromEntityWithDetails(
            Recommendations r, Details sourceAnime, Details recommendedAnime) {

        RecommendationsDTO dto = new RecommendationsDTO();
        dto.malId               = r.getMalId();
        dto.recommendationMalId = r.getRecommendationMalId();
        dto.withDetails         = true;

        if (sourceAnime != null) {
            dto.sourceTitle          = sourceAnime.getTitle();
            dto.sourceTitleJapanese  = sourceAnime.getTitleJapanese();
            dto.sourceType           = sourceAnime.getType();
            dto.sourceScore          = sourceAnime.getScore();
            dto.sourceImageUrl       = sourceAnime.getImageUrl();
            dto.sourceEpisodes       = sourceAnime.getEpisodes();
            dto.sourceYear           = sourceAnime.getYear();
            dto.sourceStatus         = sourceAnime.getStatus();
        }

        if (recommendedAnime != null) {
            dto.recommendationTitle          = recommendedAnime.getTitle();
            dto.recommendationTitleJapanese  = recommendedAnime.getTitleJapanese();
            dto.recommendationType           = recommendedAnime.getType();
            dto.recommendationScore          = recommendedAnime.getScore();
            dto.recommendationImageUrl       = recommendedAnime.getImageUrl();
            dto.recommendationEpisodes       = recommendedAnime.getEpisodes();
            dto.recommendationYear           = recommendedAnime.getYear();
            dto.recommendationStatus         = recommendedAnime.getStatus();
        }

        return dto;
    }

    // ── Getters (read-only — no setters) ─────────────────────────────────────
    // @JsonProperty ensures Jackson serialises each field with the correct
    // snake_case key automatically — no manual Map conversion needed.
    @JsonProperty("mal_id")
    public Integer getMalId()                         { return malId; }
    @JsonProperty("recommendation_mal_id")
    public Integer getRecommendationMalId()           { return recommendationMalId; }
    @JsonProperty("with_details")
    public boolean isWithDetails()                    { return withDetails; }

    @JsonProperty("source_title")
    public String    getSourceTitle()                 { return sourceTitle; }
    @JsonProperty("source_title_japanese")
    public String    getSourceTitleJapanese()         { return sourceTitleJapanese; }
    @JsonProperty("source_type")
    public String    getSourceType()                  { return sourceType; }
    @JsonProperty("source_score")
    public BigDecimal getSourceScore()                { return sourceScore; }
    @JsonProperty("source_image_url")
    public String    getSourceImageUrl()              { return sourceImageUrl; }
    @JsonProperty("source_episodes")
    public Integer   getSourceEpisodes()              { return sourceEpisodes; }
    @JsonProperty("source_year")
    public Integer   getSourceYear()                  { return sourceYear; }
    @JsonProperty("source_status")
    public String    getSourceStatus()                { return sourceStatus; }

    @JsonProperty("recommendation_title")
    public String    getRecommendationTitle()         { return recommendationTitle; }
    @JsonProperty("recommendation_title_japanese")
    public String    getRecommendationTitleJapanese() { return recommendationTitleJapanese; }
    @JsonProperty("recommendation_type")
    public String    getRecommendationType()          { return recommendationType; }
    @JsonProperty("recommendation_score")
    public BigDecimal getRecommendationScore()        { return recommendationScore; }
    @JsonProperty("recommendation_image_url")
    public String    getRecommendationImageUrl()      { return recommendationImageUrl; }
    @JsonProperty("recommendation_episodes")
    public Integer   getRecommendationEpisodes()      { return recommendationEpisodes; }
    @JsonProperty("recommendation_year")
    public Integer   getRecommendationYear()          { return recommendationYear; }
    @JsonProperty("recommendation_status")
    public String    getRecommendationStatus()        { return recommendationStatus; }
}
