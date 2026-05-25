package com.example.dataserverspringboot.entities.details;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JPA entity representing a single anime entry.
 *
 * <p>Maps to the {@code details} table in PostgreSQL. This is the central table
 * of the database — it is referenced by foreign keys from recommendations,
 * character_anime_works, person_voice_works, and person_anime_works.</p>
 *
 * <p>The primary key is {@code mal_id} (a natural {@link Integer} key imported
 * from the MyAnimeList CSV dataset — no {@code @GeneratedValue} because the
 * value is assigned by MyAnimeList).</p>
 *
 * <p>Nullable fields: {@code title_japanese}, {@code score}, {@code end_date},
 * {@code synopsis}, {@code season}, {@code favorites}. These are filtered via
 * IS NULL / IS NOT NULL in {@link DetailsService}.</p>
 */
@Schema(description = "Anime MyAnimeList entity")
@Entity
@Table(name = "details")
public class Details {

    /** Anime MAL ID — natural primary key, imported from the MyAnimeList dataset. */
    @Id
    @Column(name = "mal_id")
    private Integer malId;

    /** Anime title. */
    @Schema(description = "Anime title", example = "Cowboy Bebop")
    @Column(name = "title")
    private String title;

    /** Japanese title — nullable. */
    @Schema(description = "Japanese title", example = "カウボーイビバップ")
    @Column(name = "title_japanese")
    private String titleJapanese;

    /** MyAnimeList profile URL. */
    @Schema(description = "MyAnimeList URL", example = "https://myanimelist.net/anime/1")
    @Column(name = "url")
    private String url;

    /** Cover image URL. */
    @Schema(description = "Cover image URL")
    @Column(name = "image_url")
    private String imageUrl;

    /** Anime type, e.g. TV, Movie, OVA. */
    @Schema(description = "Anime type", example = "TV")
    @Column(name = "type")
    private String type;

    /** Airing status, e.g. Finished Airing, Currently Airing. */
    @Schema(description = "Airing status", example = "Finished Airing")
    @Column(name = "status")
    private String status;

    /** Community score (0.00–10.00) — nullable. */
    @Schema(description = "User rating score (0.00-10.00)", example = "8.78")
    @Column(name = "score")
    private BigDecimal score;

    /** Number of users who submitted a score. */
    @Schema(description = "Number of users who scored this anime", example = "500000")
    @Column(name = "scored_by")
    private Integer scoredBy;

    /** Date the anime started airing. */
    @Schema(description = "Airing start date", example = "1998-04-03")
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Date the anime finished airing — nullable. */
    @Schema(description = "Airing end date", example = "1999-04-24")
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Plot synopsis — nullable. */
    @Schema(description = "Plot synopsis")
    @Column(name = "synopsis")
    private String synopsis;

    /** All-time ranking position. */
    @Schema(description = "Ranking position", example = "28")
    @Column(name = "rank")
    private Integer rank;

    /** Popularity ranking. */
    @Schema(description = "Popularity ranking", example = "43")
    @Column(name = "popularity")
    private Integer popularity;

    /** Number of members who have added this anime to their list. */
    @Schema(description = "Number of members", example = "1800000")
    @Column(name = "members")
    private Integer members;

    /** Number of users who have this anime in their favourites — nullable. */
    @Schema(description = "Number of users who favorited", example = "75000")
    @Column(name = "favorites")
    private Integer favorites;

    /** Comma-separated genres. */
    @Schema(description = "Comma-separated genres", example = "Action, Adventure, Sci-Fi")
    @Column(name = "genres")
    private String genres;

    /** Comma-separated animation studios. */
    @Schema(description = "Comma-separated studios", example = "Sunrise")
    @Column(name = "studios")
    private String studios;

    /** Comma-separated themes. */
    @Schema(description = "Comma-separated themes", example = "Adult Cast, Space")
    @Column(name = "themes")
    private String themes;

    /** Target demographic (e.g. Shounen, Josei). */
    @Schema(description = "Target demographic", example = "Shounen")
    @Column(name = "demographics")
    private String demographics;

    /** Source material (e.g. Manga, Original, Light novel). */
    @Schema(description = "Source material", example = "Original")
    @Column(name = "source")
    private String source;

    /** Age rating (e.g. PG-13, R). */
    @Schema(description = "Age rating", example = "PG-13")
    @Column(name = "rating")
    private String rating;

    /** Number of episodes. */
    @Schema(description = "Number of episodes", example = "26")
    @Column(name = "episodes")
    private Integer episodes;

    /** Broadcast season — nullable. */
    @Schema(description = "Broadcast season", example = "Spring")
    @Column(name = "season")
    private String season;

    /** Broadcast year. */
    @Schema(description = "Broadcast year", example = "1998")
    @Column(name = "year")
    private Integer year;

    /** Comma-separated producers. */
    @Schema(description = "Producer names", example = "Agent 21")
    @Column(name = "producers")
    private String producers;

    /** Explicit genre tags. */
    @Schema(description = "Explicit genres")
    @Column(name = "explicit_genres")
    private String explicitGenres;

    /** Comma-separated licensors. */
    @Schema(description = "Licensor names", example = "Funimation")
    @Column(name = "licensors")
    private String licensors;

    /** Comma-separated streaming platforms. */
    @Schema(description = "Streaming platform names", example = "Crunchyroll")
    @Column(name = "streaming")
    private String streaming;

    /** Required no-args constructor for JPA. */
    public Details() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param malId         anime MAL ID
     * @param title         anime title
     * @param titleJapanese Japanese title (nullable)
     * @param url           MAL profile URL
     * @param imageUrl      cover image URL
     * @param type          anime type
     * @param status        airing status
     * @param score         community score (nullable)
     * @param scoredBy      number of scorers
     * @param startDate     airing start date
     * @param endDate       airing end date (nullable)
     * @param synopsis      plot synopsis (nullable)
     * @param rank          ranking position
     * @param popularity    popularity ranking
     * @param members       member count
     * @param favorites     favourites count (nullable)
     * @param genres        genres string
     * @param studios       studios string
     * @param themes        themes string
     * @param demographics  demographic string
     * @param source        source material
     * @param rating        age rating
     * @param episodes      episode count
     * @param season        broadcast season (nullable)
     * @param year          broadcast year
     * @param producers     producers string
     * @param explicitGenres explicit genres string
     * @param licensors     licensors string
     * @param streaming     streaming platforms string
     */
    public Details(Integer malId, String title, String titleJapanese, String url, String imageUrl,
                   String type, String status, BigDecimal score, Integer scoredBy, LocalDate startDate,
                   LocalDate endDate, String synopsis, Integer rank, Integer popularity, Integer members,
                   Integer favorites, String genres, String studios, String themes, String demographics,
                   String source, String rating, Integer episodes, String season, Integer year,
                   String producers, String explicitGenres, String licensors, String streaming) {
        this.malId          = malId;
        this.title          = title;
        this.titleJapanese  = titleJapanese;
        this.url            = url;
        this.imageUrl       = imageUrl;
        this.type           = type;
        this.status         = status;
        this.score          = score;
        this.scoredBy       = scoredBy;
        this.startDate      = startDate;
        this.endDate        = endDate;
        this.synopsis       = synopsis;
        this.rank           = rank;
        this.popularity     = popularity;
        this.members        = members;
        this.favorites      = favorites;
        this.genres         = genres;
        this.studios        = studios;
        this.themes         = themes;
        this.demographics   = demographics;
        this.source         = source;
        this.rating         = rating;
        this.episodes       = episodes;
        this.season         = season;
        this.year           = year;
        this.producers      = producers;
        this.explicitGenres = explicitGenres;
        this.licensors      = licensors;
        this.streaming      = streaming;
    }

    /** @return anime MAL ID */
    public Integer getMalId()           { return malId; }
    /** @param malId anime MAL ID */
    public void setMalId(Integer malId) { this.malId = malId; }

    /** @return anime title */
    public String getTitle()            { return title; }
    /** @param title anime title */
    public void setTitle(String title)  { this.title = title; }

    /** @return Japanese title, or {@code null} if not set */
    public String getTitleJapanese()    { return titleJapanese; }
    /** @param titleJapanese Japanese title */
    public void setTitleJapanese(String titleJapanese) { this.titleJapanese = titleJapanese; }

    /** @return MAL profile URL */
    public String getUrl()              { return url; }
    /** @param url MAL profile URL */
    public void setUrl(String url)      { this.url = url; }

    /** @return cover image URL */
    public String getImageUrl()         { return imageUrl; }
    /** @param imageUrl cover image URL */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /** @return anime type */
    public String getType()             { return type; }
    /** @param type anime type */
    public void setType(String type)    { this.type = type; }

    /** @return airing status */
    public String getStatus()           { return status; }
    /** @param status airing status */
    public void setStatus(String status) { this.status = status; }

    /** @return community score, or {@code null} if not set */
    public BigDecimal getScore()        { return score; }
    /** @param score community score */
    public void setScore(BigDecimal score) { this.score = score; }

    /** @return number of scorers */
    public Integer getScoredBy()        { return scoredBy; }
    /** @param scoredBy number of scorers */
    public void setScoredBy(Integer scoredBy) { this.scoredBy = scoredBy; }

    /** @return airing start date */
    public LocalDate getStartDate()     { return startDate; }
    /** @param startDate airing start date */
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    /** @return airing end date, or {@code null} if still airing */
    public LocalDate getEndDate()       { return endDate; }
    /** @param endDate airing end date */
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    /** @return plot synopsis, or {@code null} if not set */
    public String getSynopsis()         { return synopsis; }
    /** @param synopsis plot synopsis */
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    /** @return ranking position */
    public Integer getRank()            { return rank; }
    /** @param rank ranking position */
    public void setRank(Integer rank)   { this.rank = rank; }

    /** @return popularity ranking */
    public Integer getPopularity()      { return popularity; }
    /** @param popularity popularity ranking */
    public void setPopularity(Integer popularity) { this.popularity = popularity; }

    /** @return member count */
    public Integer getMembers()         { return members; }
    /** @param members member count */
    public void setMembers(Integer members) { this.members = members; }

    /** @return favourites count, or {@code null} if not set */
    public Integer getFavorites()       { return favorites; }
    /** @param favorites favourites count */
    public void setFavorites(Integer favorites) { this.favorites = favorites; }

    /** @return genres string */
    public String getGenres()           { return genres; }
    /** @param genres genres string */
    public void setGenres(String genres) { this.genres = genres; }

    /** @return studios string */
    public String getStudios()          { return studios; }
    /** @param studios studios string */
    public void setStudios(String studios) { this.studios = studios; }

    /** @return themes string */
    public String getThemes()           { return themes; }
    /** @param themes themes string */
    public void setThemes(String themes) { this.themes = themes; }

    /** @return demographic string */
    public String getDemographics()     { return demographics; }
    /** @param demographics demographic string */
    public void setDemographics(String demographics) { this.demographics = demographics; }

    /** @return source material */
    public String getSource()           { return source; }
    /** @param source source material */
    public void setSource(String source) { this.source = source; }

    /** @return age rating */
    public String getRating()           { return rating; }
    /** @param rating age rating */
    public void setRating(String rating) { this.rating = rating; }

    /** @return episode count */
    public Integer getEpisodes()        { return episodes; }
    /** @param episodes episode count */
    public void setEpisodes(Integer episodes) { this.episodes = episodes; }

    /** @return broadcast season, or {@code null} if not set */
    public String getSeason()           { return season; }
    /** @param season broadcast season */
    public void setSeason(String season) { this.season = season; }

    /** @return broadcast year */
    public Integer getYear()            { return year; }
    /** @param year broadcast year */
    public void setYear(Integer year)   { this.year = year; }

    /** @return producers string */
    public String getProducers()        { return producers; }
    /** @param producers producers string */
    public void setProducers(String producers) { this.producers = producers; }

    /** @return explicit genres string */
    public String getExplicitGenres()   { return explicitGenres; }
    /** @param explicitGenres explicit genres string */
    public void setExplicitGenres(String explicitGenres) { this.explicitGenres = explicitGenres; }

    /** @return licensors string */
    public String getLicensors()        { return licensors; }
    /** @param licensors licensors string */
    public void setLicensors(String licensors) { this.licensors = licensors; }

    /** @return streaming platforms string */
    public String getStreaming()        { return streaming; }
    /** @param streaming streaming platforms string */
    public void setStreaming(String streaming) { this.streaming = streaming; }

    @Override
    public String toString() {
        return "Details{mal_id=" + malId + ", title='" + title
                + "', title_japanese='" + titleJapanese
                + "', type='" + type + "', score=" + score + '}';
    }
}
