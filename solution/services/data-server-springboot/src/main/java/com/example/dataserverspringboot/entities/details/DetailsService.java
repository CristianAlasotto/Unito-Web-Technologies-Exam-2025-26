package com.example.dataserverspringboot.entities.details;

import com.example.dataserverspringboot.entities.characters.Characters;
import com.example.dataserverspringboot.entities.personanimeworks.PersonAnimeWorks;
import com.example.dataserverspringboot.entities.personanimeworks.PersonAnimeWorksRepository;
import com.example.dataserverspringboot.entities.persondetails.PersonDetailsRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Hidden
@Service
public class DetailsService {

    @Autowired
    private DetailsRepository repository;

    @Autowired
    private PersonAnimeWorksRepository personAnimeWorksRepository;

    @Autowired
    private PersonDetailsRepository personDetailsRepository;

    // ── Basic lookups ─────────────────────────────────────────────────────────

    public Optional<DetailsDTO> getById(Integer malId) {
        return repository.findById(malId)
                         .map(DetailsDTO::fromEntity);
    }

    public boolean existsById(Integer malId) {
        return repository.existsById(malId);
    }

    public long count() {
        return repository.count();
    }

    // ── Relation helpers (moved from controller) ──────────────────────────────

    /**
     * Returns a paginated page of Details for anime recommended by the given malId.
     * Delegates to the repository query — controller never touches the repository.
     */
    public Page<DetailsDTO> getRecommendationsForAnime(Integer malId, Pageable pageable) {
        return repository.findRecommendationsForAnime(malId, pageable)
                         .map(DetailsDTO::fromEntity);
    }

    /**
     * Returns a paginated page of Characters that appear in the given anime.
     * Delegates to the repository query — controller never touches the repository.
     */
    public Page<Characters> getCharactersForAnime(Integer malId, Pageable pageable) {
        return repository.findCharactersInAnime(malId, pageable);
    }

    /**
     * Returns a list of staff (person + position) for the given anime.
     * Fetches PersonAnimeWorks rows then resolves each PersonDetails —
     * controller never touches the repository directly.
     */
    public List<Map<String, Object>> getStaffForAnime(Integer malId) {
        List<Map<String, Object>> staff = new ArrayList<>();
        List<PersonAnimeWorks> works = personAnimeWorksRepository
                .findByAnimeMalId(malId, Pageable.unpaged()).getContent();
        for (PersonAnimeWorks work : works) {
            personDetailsRepository.findById(work.getPersonMalId()).ifPresent(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("person_mal_id", p.getPersonMalId());
                map.put("name",          p.getName());
                map.put("given_name",    p.getGivenName());
                map.put("family_name",   p.getFamilyName());
                map.put("position",      work.getPosition());
                staff.add(map);
            });
        }
        return staff;
    }

    // ── Filter helpers ────────────────────────────────────────────────────────

    /**
     * Converts a raw search string into a lowercase LIKE pattern ("%value%").
     * Returns null if the input is null or blank.
     *
     * This is the key fix for the "lower(bytea)" PostgreSQL error:
     * by building the wildcard in Java and passing a concrete non-null String,
     * Hibernate always infers VARCHAR correctly — no CAST needed in the query.
     */
    private String likePattern(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.toLowerCase() + "%";
    }

    /**
     * Find records with optional filters, returning a paginated page of DTOs.
     *
     * HOW IT WORKS:
     *   1. likePattern() pre-builds wildcard strings for search and genres.
     *      This fixes the bytea type inference bug without any CAST in SQL.
     *   2. If more than one filter is active, findWithCombinedFilters() is
     *      called, which uses a single JPQL @Query with AND logic and
     *      ":param IS NULL OR condition" to make every filter optional.
     *   3. If only one filter is active, the dedicated derived method is
     *      called (simpler and slightly faster).
     *   4. If no filters are active, findAll() is called.
     *   5. The Page<Details> result is mapped to Page<DetailsDTO> so the
     *      raw JPA entity never leaves the service layer.
     */
    public Page<DetailsDTO> findWithFilters(
            String search, String type, Integer year, String status,
            String rating, String source, String genres, Integer episodes,
            Pageable pageable) {

        String searchPattern = likePattern(search);
        String genresPattern = likePattern(genres);

        int filterCount = 0;
        if (searchPattern != null) filterCount++;
        if (type != null)          filterCount++;
        if (year != null)          filterCount++;
        if (status != null)        filterCount++;
        if (rating != null)        filterCount++;
        if (source != null)        filterCount++;
        if (genresPattern != null) filterCount++;
        if (episodes != null)      filterCount++;

        if (filterCount > 1) {
            return repository.findWithCombinedFilters(
                    searchPattern, type, year, status,
                    rating, source, genresPattern, episodes, pageable)
                    .map(DetailsDTO::fromEntity);
        }

        if (searchPattern != null) return repository.searchByTitle(searchPattern, pageable).map(DetailsDTO::fromEntity);
        if (type     != null)      return repository.findByType(type, pageable).map(DetailsDTO::fromEntity);
        if (year     != null)      return repository.findByYear(year, pageable).map(DetailsDTO::fromEntity);
        if (status   != null)      return repository.findByStatus(status, pageable).map(DetailsDTO::fromEntity);
        if (rating   != null)      return repository.findByRating(rating, pageable).map(DetailsDTO::fromEntity);
        if (source   != null)      return repository.findBySource(source, pageable).map(DetailsDTO::fromEntity);
        if (genresPattern != null) return repository.findByGenresContaining(genresPattern, pageable).map(DetailsDTO::fromEntity);
        if (episodes != null)      return repository.findByEpisodes(episodes, pageable).map(DetailsDTO::fromEntity);

        return repository.findAll(pageable).map(DetailsDTO::fromEntity);
    }

    /**
     * Overload that also handles IS NULL / IS NOT NULL filters.
     */
    public Page<DetailsDTO> findWithFilters(
            String search, String type, Integer year, String status,
            String rating, String source, String genres, Integer episodes,
            String nullFilter, String notNullFilter,
            Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, type, year, status,
                rating, source, genres, episodes, pageable);
    }

    private Page<DetailsDTO> handleNullFilter(String field, Pageable pageable) {
        Page<Details> result = switch (field.toLowerCase()) {
            case "synopsis"                        -> repository.findBySynopsisIsNull(pageable);
            case "score"                           -> repository.findByScoreIsNull(pageable);
            case "end_date", "enddate"             -> repository.findByEndDateIsNull(pageable);
            case "title_japanese", "titlejapanese" -> repository.findByTitleJapaneseIsNull(pageable);
            case "season"                          -> repository.findBySeasonIsNull(pageable);
            case "favorites"                       -> repository.findByFavoritesIsNull(pageable);
            default                                -> repository.findAll(pageable);
        };
        return result.map(DetailsDTO::fromEntity);
    }

    private Page<DetailsDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<Details> result = switch (field.toLowerCase()) {
            case "synopsis"                        -> repository.findBySynopsisIsNotNull(pageable);
            case "score"                           -> repository.findByScoreIsNotNull(pageable);
            case "end_date", "enddate"             -> repository.findByEndDateIsNotNull(pageable);
            case "title_japanese", "titlejapanese" -> repository.findByTitleJapaneseIsNotNull(pageable);
            case "season"                          -> repository.findBySeasonIsNotNull(pageable);
            case "favorites"                       -> repository.findByFavoritesIsNotNull(pageable);
            default                                -> repository.findAll(pageable);
        };
        return result.map(DetailsDTO::fromEntity);
    }

    /**
     * Returns a map of field names to the count of records where that field is NULL.
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("synopsis",       repository.countBySynopsisIsNull());
        counts.put("score",          repository.countByScoreIsNull());
        counts.put("end_date",       repository.countByEndDateIsNull());
        counts.put("title_japanese", repository.countByTitleJapaneseIsNull());
        counts.put("season",         repository.countBySeasonIsNull());
        counts.put("favorites",      repository.countByFavoritesIsNull());
        return counts;
    }

    /**
     * Update the score for a specific anime.
     *
     * @param malId    the MAL ID of the anime to update
     * @param newScore the new score value (0.00 to 10.00) — already validated by @Valid
     * @return an Optional containing the updated DetailsDTO, or empty if not found
     */
    public Optional<DetailsDTO> updateScore(Integer malId, BigDecimal newScore) {
        return repository.findById(malId)
                .map(details -> {
                    details.setScore(newScore);
                    return DetailsDTO.fromEntity(repository.save(details));
                });
    }
}
