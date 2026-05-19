package com.example.dataserverspringboot.entities.characters;

import com.example.dataserverspringboot.entities.details.DetailsDTO;
import com.example.dataserverspringboot.entities.persondetails.PersonDetailsDTO;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Hidden
@Service
public class CharactersService {

    @Autowired
    private CharactersRepository repository;

    // ── Basic lookups ─────────────────────────────────────────────────────────

    /**
     * Fetch a single character by MAL ID.
     * Returns Optional<CharactersDTO> — raw entity never leaves the service layer.
     */
    public Optional<CharactersDTO> getById(Integer characterMalId) {
        return repository.findById(characterMalId)
                         .map(CharactersDTO::fromEntity);
    }

    public boolean existsById(Integer characterMalId) {
        return repository.existsById(characterMalId);
    }

    public long count() {
        return repository.count();
    }

    // ── Relation helpers (moved from controller) ──────────────────────────────

    /**
     * Returns a paginated page of DetailsDTO for all anime this character appears in.
     * Delegates to the repository query — controller never touches the repository.
     */
    public Page<DetailsDTO> getAnimeAppearancesForCharacter(Integer characterMalId, Pageable pageable) {
        return repository.findAnimeAppearances(characterMalId, pageable)
                         .map(DetailsDTO::fromEntity);
    }

    /**
     * Returns a paginated page of PersonDetailsDTO for all voice actors of this character.
     * Delegates to the repository query — controller never touches the repository.
     */
    public Page<PersonDetailsDTO> getVoiceActorsForCharacter(Integer characterMalId, Pageable pageable) {
        return repository.findVoiceActors(characterMalId, pageable)
                         .map(PersonDetailsDTO::fromEntity);
    }

    // ── Filter helpers ────────────────────────────────────────────────────────

    /**
     * Converts a raw search string into a lowercase LIKE pattern ("%value%").
     * Returns null if the input is null or blank.
     * Same fix as DetailsService.likePattern() — avoids lower(bytea) bug.
     */
    private String likePattern(String value) {
        if (value == null || value.isBlank()) return null;
        return "%" + value.toLowerCase() + "%";
    }

    /**
     * Find characters with optional filters, returning a paginated page of DTOs.
     * The Page<Characters> result is mapped to Page<CharactersDTO> so the raw
     * JPA entity never leaves the service layer.
     */
    public Page<CharactersDTO> findWithFilters(String search, Pageable pageable) {
        String searchPattern = likePattern(search);
        if (searchPattern != null) {
            return repository.searchByName(searchPattern, pageable)
                    .map(CharactersDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(CharactersDTO::fromEntity);
    }

    /**
     * Overload that also handles IS NULL / IS NOT NULL filters.
     */
    public Page<CharactersDTO> findWithFilters(
            String search, String nullFilter, String notNullFilter, Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, pageable);
    }

    /**
     * Route IS NULL filter to the correct derived repository method.
     */
    private Page<CharactersDTO> handleNullFilter(String field, Pageable pageable) {
        Page<Characters> result = switch (field.toLowerCase()) {
            case "name_kanji", "namekanji" -> repository.findByNameKanjiIsNull(pageable);
            case "image"                   -> repository.findByImageIsNull(pageable);
            case "about"                   -> repository.findByAboutIsNull(pageable);
            default                        -> repository.findAll(pageable);
        };
        return result.map(CharactersDTO::fromEntity);
    }

    /**
     * Route IS NOT NULL filter to the correct derived repository method.
     */
    private Page<CharactersDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<Characters> result = switch (field.toLowerCase()) {
            case "name_kanji", "namekanji" -> repository.findByNameKanjiIsNotNull(pageable);
            case "image"                   -> repository.findByImageIsNotNull(pageable);
            case "about"                   -> repository.findByAboutIsNotNull(pageable);
            case "favorites"               -> repository.findByFavoritesIsNotNull(pageable);
            default                        -> repository.findAll(pageable);
        };
        return result.map(CharactersDTO::fromEntity);
    }

    /**
     * Returns a map of field names to the count of records where that field is NULL.
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("name_kanji", repository.countByNameKanjiIsNull());
        counts.put("image",      repository.countByImageIsNull());
        counts.put("about",      repository.countByAboutIsNull());
        counts.put("favorites",  repository.countByFavoritesIsNull());
        return counts;
    }
}
