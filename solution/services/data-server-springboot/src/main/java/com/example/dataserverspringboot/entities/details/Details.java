package com.example.dataserverspringboot.entities.details;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Anime MyAnimeList ID (primary key)", example = "1")
@Entity
@Table(name = "details")
public class Details {

    @Id
    @Column(name = "mal_id")
    private Integer malId;

    @Schema(description = "Anime title", example = "Cowboy Bebop")
    @Column(name = "title")
    private String title;

    @Schema(description = "Japanese title", example = "カウボーイビバップ")
    @Column(name = "title_japanese")
    private String titleJapanese;

    @Schema(description = "MyAnimeList URL", example = "https://myanimelist.net/anime/1")
    @Column(name = "url")
    private String url;

    @Schema(description = "Cover image URL")
    @Column(name = "image_url")
    private String imageUrl;

    @Schema(description = "Anime type", example = "TV")
    @Column(name = "type")
    private String type;

    @Schema(description = "Airing status", example = "Finished Airing")
    @Column(name = "status")
    private String status;

    @Schema(description = "User rating score (0.00-10.00)", example = "8.78")
    @Column(name = "score")
    private BigDecimal score;

    @Schema(description = "Number of users who scored this anime", example = "500000")
    @Column(name = "scored_by")
    private Integer scoredBy;

    @Schema(description = "Airing start date", example = "1998-04-03")
    @Column(name = "start_date")
    private LocalDate startDate;

    @Schema(description = "Airing end date", example = "1999-04-24")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Schema(description = "Plot synopsis")
    @Column(name = "synopsis")
    private String synopsis;

    @Schema(description = "Ranking position", example = "28")
    @Column(name = "rank")
    private Integer rank;

    @Schema(description = "Popularity ranking", example = "43")
    @Column(name = "popularity")
    private Integer popularity;

    @Schema(description = "Number of members", example = "1800000")
    @Column(name = "members")
    private Integer members;

    @Schema(description = "Number of users who favorited", example = "75000")
    @Column(name = "favorites")
    private Integer favorites;

    @Schema(description = "Comma-separated genres", example = "Action, Adventure, Sci-Fi")
    @Column(name = "genres")
    private String genres;

    @Schema(description = "Comma-separated studios", example = "Sunrise")
    @Column(name = "studios")
    private String studios;

    @Schema(description = "Comma-separated themes", example = "Adult Cast, Space")
    @Column(name = "themes")
    private String themes;

    @Schema(description = "Target demographic", example = "Shounen")
    @Column(name = "demographics")
    private String demographics;

    @Schema(description = "Source material", example = "Original")
    @Column(name = "source")
    private String source;

    @Schema(description = "Age rating", example = "PG-13")
    @Column(name = "rating")
    private String rating;

    @Schema(description = "Number of episodes", example = "26")
    @Column(name = "episodes")
    private Integer episodes;

    @Schema(description = "Broadcast season", example = "Spring")
    @Column(name = "season")
    private String season;

    @Schema(description = "Broadcast year", example = "1998")
    @Column(name = "year")
    private Integer year;

    @Schema(description = "Producers names", example = "Agent 21")
    @Column(name = "producers")
    private String producers;

    @Schema(description = "Explicit genres")
    @Column(name = "explicit_genres")
    private String explicitGenres;

    @Schema(description = "Licensors names", example = "Funimation")
    @Column(name = "licensors")
    private String licensors;

    @Schema(description = "Streaming platform names", example = "Crunchyroll")
    @Column(name = "streaming")
    private String streaming;

    // Constructors
    public Details() {
    }

    public Details(Integer malId, String title, String titleJapanese, String url, String imageUrl,
                   String type, String status, BigDecimal score, Integer scoredBy, LocalDate startDate,
                   LocalDate endDate, String synopsis, Integer rank, Integer popularity, Integer members,
                   Integer favorites, String genres, String studios, String themes, String demographics,
                   String source, String rating, Integer episodes, String season, Integer year,
                   String producers, String explicitGenres, String licensors, String streaming) {
        this.malId = malId;
        this.title = title;
        this.titleJapanese = titleJapanese;
        this.url = url;
        this.imageUrl = imageUrl;
        this.type = type;
        this.status = status;
        this.score = score;
        this.scoredBy = scoredBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.synopsis = synopsis;
        this.rank = rank;
        this.popularity = popularity;
        this.members = members;
        this.favorites = favorites;
        this.genres = genres;
        this.studios = studios;
        this.themes = themes;
        this.demographics = demographics;
        this.source = source;
        this.rating = rating;
        this.episodes = episodes;
        this.season = season;
        this.year = year;
        this.producers = producers;
        this.explicitGenres = explicitGenres;
        this.licensors = licensors;
        this.streaming = streaming;
    }

    // Getters and Setters
    public Integer getMalId() {
        return malId;
    }

    public void setMalId(Integer malId) {
        this.malId = malId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleJapanese() {
        return titleJapanese;
    }

    public void setTitleJapanese(String titleJapanese) {
        this.titleJapanese = titleJapanese;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getScoredBy() {
        return scoredBy;
    }

    public void setScoredBy(Integer scoredBy) {
        this.scoredBy = scoredBy;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public Integer getMembers() {
        return members;
    }

    public void setMembers(Integer members) {
        this.members = members;
    }

    public Integer getFavorites() {
        return favorites;
    }

    public void setFavorites(Integer favorites) {
        this.favorites = favorites;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getStudios() {
        return studios;
    }

    public void setStudios(String studios) {
        this.studios = studios;
    }

    public String getThemes() {
        return themes;
    }

    public void setThemes(String themes) {
        this.themes = themes;
    }

    public String getDemographics() {
        return demographics;
    }

    public void setDemographics(String demographics) {
        this.demographics = demographics;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Integer getEpisodes() {
        return episodes;
    }

    public void setEpisodes(Integer episodes) {
        this.episodes = episodes;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getProducers() {
        return producers;
    }

    public void setProducers(String producers) {
        this.producers = producers;
    }

    public String getExplicitGenres() {
        return explicitGenres;
    }

    public void setExplicitGenres(String explicitGenres) {
        this.explicitGenres = explicitGenres;
    }

    public String getLicensors() {
        return licensors;
    }

    public void setLicensors(String licensors) {
        this.licensors = licensors;
    }

    public String getStreaming() {
        return streaming;
    }

    public void setStreaming(String streaming) {
        this.streaming = streaming;
    }

    @Override
    public String toString() {
        return "Details{" +
                "mal_id=" + malId +
                ", title='" + title + '\'' +
                ", title_japanese='" + titleJapanese + '\'' +
                ", type='" + type + '\'' +
                ", score=" + score +
                '}';
    }
}
