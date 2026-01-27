package com.example.dataserverspringboot.entities.details;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "details")
public class Details {
    
    @Id
    @Column(name = "mal_id")
    private Integer malId;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "title_japanese")
    private String titleJapanese;
    
    @Column(name = "url")
    private String url;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "score")
    private BigDecimal score;
    
    @Column(name = "scored_by")
    private Integer scoredBy;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "synopsis")
    private String synopsis;
    
    @Column(name = "rank")
    private Integer rank;
    
    @Column(name = "popularity")
    private Integer popularity;
    
    @Column(name = "members")
    private Integer members;
    
    @Column(name = "favorites")
    private Integer favorites;
    
    @Column(name = "genres")
    private String genres;
    
    @Column(name = "studios")
    private String studios;
    
    @Column(name = "themes")
    private String themes;
    
    @Column(name = "demographics")
    private String demographics;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "rating")
    private String rating;
    
    @Column(name = "episodes")
    private Integer episodes;
    
    @Column(name = "season")
    private String season;
    
    @Column(name = "year")
    private Integer year;
    
    @Column(name = "producers")
    private String producers;
    
    @Column(name = "explicit_genres")
    private String explicitGenres;
    
    @Column(name = "licensors")
    private String licensors;
    
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
