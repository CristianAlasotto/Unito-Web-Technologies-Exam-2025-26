package com.example.dataserverspringboot.entities.details;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Hidden
@Repository
public interface DetailsRepository extends JpaRepository<Details, Integer> {

    /**
     * Executes a combined AND filter across all searchable fields.
     *
     * <p>Uses pure JPQL (no {@code nativeQuery=true}) so Hibernate correctly maps
     * camelCase Java field names to snake_case SQL columns via {@code @Column}
     * annotations, and Spring Data's pagination ORDER BY generation works correctly.</p>
     *
     * <p>The {@code lower(bytea)} PostgreSQL error is avoided by pre-building
     * wildcard patterns in {@link DetailsService#likePattern} before calling
     * this method. Parameters {@code searchPattern} and {@code genresPattern} arrive
     * already as {@code "%value%"} (a non-null {@link String}) or {@code null},
     * so Hibernate always infers {@code VARCHAR} correctly.</p>
     *
     * <p>The {@code :param IS NULL OR condition} pattern makes every filter optional:
     * when a parameter is {@code null}, the left side evaluates to {@code TRUE}
     * and the right side is never evaluated, effectively removing that filter.</p>
     *
     * @param searchPattern pre-built LIKE pattern, e.g. {@code "%naruto%"}, or {@code null}
     * @param type          exact type filter, or {@code null}
     * @param year          exact year filter, or {@code null}
     * @param status        exact status filter, or {@code null}
     * @param rating        exact rating filter, or {@code null}
     * @param source        exact source filter, or {@code null}
     * @param genresPattern pre-built LIKE pattern, e.g. {@code "%action%"}, or {@code null}
     * @param episodes      exact episode count filter, or {@code null}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of matching {@link Details} records
     */
    @Query("SELECT e FROM Details e WHERE " +
            "(:searchPattern IS NULL OR LOWER(e.title) LIKE :searchPattern) AND " +
            "(:type IS NULL OR e.type = :type) AND " +
            "(:year IS NULL OR e.year = :year) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:rating IS NULL OR e.rating = :rating) AND " +
            "(:source IS NULL OR e.source = :source) AND " +
            "(:genresPattern IS NULL OR LOWER(e.genres) LIKE :genresPattern) AND " +
            "(:episodes IS NULL OR e.episodes = :episodes)")
    Page<Details> findWithCombinedFilters(
            @Param("searchPattern") String searchPattern,
            @Param("type") String type,
            @Param("year") Integer year,
            @Param("status") String status,
            @Param("rating") String rating,
            @Param("source") String source,
            @Param("genresPattern") String genresPattern,
            @Param("episodes") Integer episodes,
            Pageable pageable);

    /**
     * Searches anime by title with a case-insensitive partial match.
     *
     * @param searchPattern pre-built lowercase LIKE pattern, e.g. {@code "%cowboy%"}
     * @param pageable      pagination and sorting parameters
     * @return paginated page of matching records
     */
    @Query("SELECT e FROM Details e WHERE LOWER(e.title) LIKE :searchPattern")
    Page<Details> searchByTitle(@Param("searchPattern") String searchPattern, Pageable pageable);

    /**
     * Returns all anime with an exact type match.
     *
     * @param type     exact type value, e.g. {@code "TV"}
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<Details> findByType(String type, Pageable pageable);

    /**
     * Returns all anime broadcast in the given year.
     *
     * @param year     broadcast year
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<Details> findByYear(Integer year, Pageable pageable);

    /**
     * Returns all anime with the given airing status.
     *
     * @param status   airing status, e.g. {@code "Finished Airing"}
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<Details> findByStatus(String status, Pageable pageable);

    /**
     * Returns all anime with the given age rating.
     *
     * @param rating   age rating, e.g. {@code "PG-13"}
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<Details> findByRating(String rating, Pageable pageable);

    /**
     * Returns all anime with the given source material.
     *
     * @param source   source material, e.g. {@code "Manga"}
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<Details> findBySource(String source, Pageable pageable);

    /**
     * Returns all anime with exactly the given episode count.
     *
     * @param episodes exact episode count
     * @param pageable pagination and sorting parameters
     * @return paginated page of matching records
     */
    Page<Details> findByEpisodes(Integer episodes, Pageable pageable);

    /**
     * Searches anime by genres with a case-insensitive partial match.
     *
     * @param genrePattern pre-built lowercase LIKE pattern, e.g. {@code "%action%"}
     * @param pageable     pagination and sorting parameters
     * @return paginated page of matching records
     */
    @Query("SELECT e FROM Details e WHERE LOWER(e.genres) LIKE :genrePattern")
    Page<Details> findByGenresContaining(@Param("genrePattern") String genrePattern, Pageable pageable);

    /**
     * Returns all anime recommended as similar to the given anime, ordered by score descending.
     *
     * <p>Implements a cross-entity JPQL subquery:
     * {@code details → recommendations → details}.</p>
     *
     * @param malId    source anime MAL ID
     * @param pageable pagination parameters
     * @return paginated page of recommended {@link Details} ordered by score
     */
    @Query("SELECT d FROM Details d WHERE d.malId IN " +
            "(SELECT r.recommendationMalId FROM " +
            "com.example.dataserverspringboot.entities.recommendations.Recommendations r " +
            "WHERE r.malId = :malId) " +
            "ORDER BY d.score DESC")
    Page<Details> findRecommendationsForAnime(@Param("malId") Integer malId, Pageable pageable);

    /**
     * Returns all characters that appear in this anime, ordered by favourites descending.
     *
     * <p>Cross-entity JPQL join:
     * {@code details → character_anime_works → characters}.</p>
     *
     * @param malId    anime MAL ID
     * @param pageable pagination parameters
     * @return paginated page of {@link com.example.dataserverspringboot.entities.characters.Characters}
     */
    @Query("SELECT c FROM com.example.dataserverspringboot.entities.characters.Characters c " +
            "WHERE c.characterMalId IN " +
            "(SELECT caw.characterMalId FROM " +
            "com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
            "WHERE caw.animeMalId = :malId) " +
            "ORDER BY c.favorites DESC NULLS LAST")
    Page<com.example.dataserverspringboot.entities.characters.Characters>
            findCharactersInAnime(@Param("malId") Integer malId, Pageable pageable);

    /**
     * Counts distinct characters that appear in this anime.
     *
     * @param malId anime MAL ID
     * @return count of distinct characters
     */
    @Query("SELECT COUNT(DISTINCT caw.characterMalId) FROM " +
            "com.example.dataserverspringboot.entities.characteranimeworks.CharacterAnimeWorks caw " +
            "WHERE caw.animeMalId = :malId")
    long countCharactersInAnime(@Param("malId") Integer malId);

    /**
     * Counts recommendations for the given anime.
     *
     * @param malId anime MAL ID
     * @return count of recommendation records
     */
    @Query("SELECT COUNT(r) FROM " +
            "com.example.dataserverspringboot.entities.recommendations.Recommendations r " +
            "WHERE r.malId = :malId")
    long countRecommendationsForAnime(@Param("malId") Integer malId);

    Page<Details> findBySynopsisIsNull(Pageable pageable);
    Page<Details> findBySynopsisIsNotNull(Pageable pageable);

    Page<Details> findByScoreIsNull(Pageable pageable);
    Page<Details> findByScoreIsNotNull(Pageable pageable);

    Page<Details> findByEndDateIsNull(Pageable pageable);
    Page<Details> findByEndDateIsNotNull(Pageable pageable);

    Page<Details> findByTitleJapaneseIsNull(Pageable pageable);
    Page<Details> findByTitleJapaneseIsNotNull(Pageable pageable);

    Page<Details> findBySeasonIsNull(Pageable pageable);
    Page<Details> findBySeasonIsNotNull(Pageable pageable);

    Page<Details> findByFavoritesIsNull(Pageable pageable);
    Page<Details> findByFavoritesIsNotNull(Pageable pageable);

    long countBySynopsisIsNull();
    long countByScoreIsNull();
    long countByEndDateIsNull();
    long countByTitleJapaneseIsNull();
    long countBySeasonIsNull();
    long countByFavoritesIsNull();
}