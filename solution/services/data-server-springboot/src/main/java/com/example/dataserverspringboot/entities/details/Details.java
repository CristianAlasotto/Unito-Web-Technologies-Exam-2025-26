package com.example.dataserverspringboot.entities.details;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity class for the details table (anime information)
 */
@Entity
@Table(name = "details")
public class Details {

    @Id
    @Column(name = "mal_id")
    private Integer mal_id;

    @Column(name = "title")
    private String title;

    @Column(name = "title_japanese")
    private String title_japanese;

    @Column(name = "url")
    private String url;

    @Column(name = "image_url")
    private String image_url;

    @Column(name = "type")
    private String type;

    @Column(name = "status")
    private String status;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "scored_by")
    private Integer scored_by;

    @Column(name = "start_date")
    private LocalDate start_date;

    @Column(name = "end_date")
    private LocalDate end_date;

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
    private BigDecimal episodes;

    @Column(name = "season")
    private String season;

    @Column(name = "year")
    private BigDecimal year;

    @Column(name = "producers")
    private String producers;

    @Column(name = "explicit_genres")
    private String explicit_genres;

    @Column(name = "licensors")
    private String licensors;

    @Column(name = "streaming")
    private String streaming;

    // Constructors
    public Details() {
    }

    public Details(Integer mal_id, String title, String title_japanese, String url, String image_url,
                   String type, String status, BigDecimal score, Integer scored_by, LocalDate start_date,
                   LocalDate end_date, String synopsis, Integer rank, Integer popularity, Integer members,
                   Integer favorites, String genres, String studios, String themes, String demographics,
                   String source, String rating, BigDecimal episodes, String season, BigDecimal year,
                   String producers, String explicit_genres, String licensors, String streaming) {
        this.mal_id = mal_id;
        this.title = title;
        this.title_japanese = title_japanese;
        this.url = url;
        this.image_url = image_url;
        this.type = type;
        this.status = status;
        this.score = score;
        this.scored_by = scored_by;
        this.start_date = start_date;
        this.end_date = end_date;
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
        this.explicit_genres = explicit_genres;
        this.licensors = licensors;
        this.streaming = streaming;
    }

    // Getters and Setters
    public Integer getMal_id() {
        return mal_id;
    }

    public void setMal_id(Integer mal_id) {
        this.mal_id = mal_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle_japanese() {
        return title_japanese;
    }

    public void setTitle_japanese(String title_japanese) {
        this.title_japanese = title_japanese;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
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

    public Integer getScored_by() {
        return scored_by;
    }

    public void setScored_by(Integer scored_by) {
        this.scored_by = scored_by;
    }

    public LocalDate getStart_date() {
        return start_date;
    }

    public void setStart_date(LocalDate start_date) {
        this.start_date = start_date;
    }

    public LocalDate getEnd_date() {
        return end_date;
    }

    public void setEnd_date(LocalDate end_date) {
        this.end_date = end_date;
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

    public BigDecimal getEpisodes() {
        return episodes;
    }

    public void setEpisodes(BigDecimal episodes) {
        this.episodes = episodes;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public BigDecimal getYear() {
        return year;
    }

    public void setYear(BigDecimal year) {
        this.year = year;
    }

    public String getProducers() {
        return producers;
    }

    public void setProducers(String producers) {
        this.producers = producers;
    }

    public String getExplicit_genres() {
        return explicit_genres;
    }

    public void setExplicit_genres(String explicit_genres) {
        this.explicit_genres = explicit_genres;
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
                "mal_id=" + mal_id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", score=" + score +
                '}';
    }
}