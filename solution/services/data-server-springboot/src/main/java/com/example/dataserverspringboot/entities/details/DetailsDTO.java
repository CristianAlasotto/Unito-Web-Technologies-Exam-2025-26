package com.example.dataserverspringboot.entities.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for the Details entity.
 *
 * PURPOSE:
 *   Decouples the internal JPA entity (Details.java) from the data exposed
 *   through the REST API. This means:
 *     - The database schema can change without breaking the API contract.
 *     - Sensitive or irrelevant fields can be excluded from responses.
 *     - Field names/shapes can be customised for the client without touching the entity.
 *
 * USAGE:
 *   Built from a Details entity via the static factory method fromEntity(Details d).
 *   The controller and service always return DetailsDTO, never the raw Details entity.
 */
@Schema(description = "Anime details data transfer object")
public class DetailsDTO {

    @Schema(description = "MyAnimeList ID (primary key)", example = "1")
    private Integer malId;

    @Schema(description = "Anime title", example = "Cowboy Bebop")
    private String title;

    @Schema(description = "Japanese title", example = "カウボーイビバップ")
    private String titleJapanese;

    @Schema(description = "MyAnimeList URL", example = "https://myanimelist.net/anime/1")
    private String url;

    @Schema(description = "Cover image URL")
    private String imageUrl;

    @Schema(description = "Anime type", example = "TV")
    private String type;

    @Schema(description = "Airing status", example = "Finished Airing")
    private String status;

    @Schema(description = "User rating score (0.00-10.00)", example = "8.78")
    private BigDecimal score;

    @Schema(description = "Number of users who scored this anime", example = "500000")
    private Integer scoredBy;

    @Schema(description = "Airing start date", example = "1998-04-03")
    private LocalDate startDate;

    @Schema(description = "Airing end date", example = "1999-04-24")
    private LocalDate endDate;

    @Schema(description = "Plot synopsis")
    private String synopsis;

    @Schema(description = "Ranking position", example = "28")
    private Integer rank;

    @Schema(description = "Popularity ranking", example = "43")
    private Integer popularity;

    @Schema(description = "Number of members", example = "1800000")
    private Integer members;

    @Schema(description = "Number of users who favorited", example = "75000")
    private Integer favorites;

    @Schema(description = "Comma-separated genres", example = "Action, Adventure, Sci-Fi")
    private String genres;

    @Schema(description = "Comma-separated studios", example = "Sunrise")
    private String studios;

    @Schema(description = "Comma-separated themes", example = "Adult Cast, Space")
    private String themes;

    @Schema(description = "Target demographic", example = "Shounen")
    private String demographics;

    @Schema(description = "Source material", example = "Original")
    private String source;

    @Schema(description = "Age rating", example = "PG-13")
    private String rating;

    @Schema(description = "Number of episodes", example = "26")
    private Integer episodes;

    @Schema(description = "Broadcast season", example = "Spring")
    private String season;

    @Schema(description = "Broadcast year", example = "1998")
    private Integer year;

    @Schema(description = "Producer names", example = "Agent 21")
    private String producers;

    @Schema(description = "Explicit genres")
    private String explicitGenres;

    @Schema(description = "Licensor names", example = "Funimation")
    private String licensors;

    @Schema(description = "Streaming platform names", example = "Crunchyroll")
    private String streaming;

    // ── Private constructor — use fromEntity() ────────────────────────────────
    private DetailsDTO() {}

    /**
     * Static factory method: converts a Details JPA entity into a DetailsDTO.
     * This is the only way to build a DTO — keeps construction logic in one place.
     *
     * @param d the Details entity fetched from the database
     * @return a fully populated DetailsDTO ready to be serialised as JSON
     */
    public static DetailsDTO fromEntity(Details d) {
        DetailsDTO dto = new DetailsDTO();
        dto.malId         = d.getMalId();
        dto.title         = d.getTitle();
        dto.titleJapanese = d.getTitleJapanese();
        dto.url           = d.getUrl();
        dto.imageUrl      = d.getImageUrl();
        dto.type          = d.getType();
        dto.status        = d.getStatus();
        dto.score         = d.getScore();
        dto.scoredBy      = d.getScoredBy();
        dto.startDate     = d.getStartDate();
        dto.endDate       = d.getEndDate();
        dto.synopsis      = d.getSynopsis();
        dto.rank          = d.getRank();
        dto.popularity    = d.getPopularity();
        dto.members       = d.getMembers();
        dto.favorites     = d.getFavorites();
        dto.genres        = d.getGenres();
        dto.studios       = d.getStudios();
        dto.themes        = d.getThemes();
        dto.demographics  = d.getDemographics();
        dto.source        = d.getSource();
        dto.rating        = d.getRating();
        dto.episodes      = d.getEpisodes();
        dto.season        = d.getSeason();
        dto.year          = d.getYear();
        dto.producers     = d.getProducers();
        dto.explicitGenres= d.getExplicitGenres();
        dto.licensors     = d.getLicensors();
        dto.streaming     = d.getStreaming();
        return dto;
    }

    // ── Getters (no setters — DTO is read-only once built) ───────────────────
    // @JsonProperty ensures Jackson serialises each field with the correct
    // snake_case key automatically — no manual Map conversion needed.
    @JsonProperty("mal_id")
    public Integer getMalId()          { return malId; }
    public String getTitle()           { return title; }
    @JsonProperty("title_japanese")
    public String getTitleJapanese()   { return titleJapanese; }
    public String getUrl()             { return url; }
    @JsonProperty("image_url")
    public String getImageUrl()        { return imageUrl; }
    public String getType()            { return type; }
    public String getStatus()          { return status; }
    public BigDecimal getScore()       { return score; }
    @JsonProperty("scored_by")
    public Integer getScoredBy()       { return scoredBy; }
    @JsonProperty("start_date")
    public LocalDate getStartDate()    { return startDate; }
    @JsonProperty("end_date")
    public LocalDate getEndDate()      { return endDate; }
    public String getSynopsis()        { return synopsis; }
    public Integer getRank()           { return rank; }
    public Integer getPopularity()     { return popularity; }
    public Integer getMembers()        { return members; }
    public Integer getFavorites()      { return favorites; }
    public String getGenres()          { return genres; }
    public String getStudios()         { return studios; }
    public String getThemes()          { return themes; }
    public String getDemographics()    { return demographics; }
    public String getSource()          { return source; }
    public String getRating()          { return rating; }
    public Integer getEpisodes()       { return episodes; }
    public String getSeason()          { return season; }
    public Integer getYear()           { return year; }
    public String getProducers()       { return producers; }
    @JsonProperty("explicit_genres")
    public String getExplicitGenres()  { return explicitGenres; }
    public String getLicensors()       { return licensors; }
    public String getStreaming()       { return streaming; }
}
