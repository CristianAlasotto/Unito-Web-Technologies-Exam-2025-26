package com.example.dataserverspringboot.entities.persondetails;

import com.example.dataserverspringboot.entities.details.Details;
import com.example.dataserverspringboot.entities.details.DetailsDTO;
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
public class PersonDetailsService {

    @Autowired
    private PersonDetailsRepository repository;

    // ── Basic lookups ─────────────────────────────────────────────────────────

    /**
     * Fetch a single person by MAL ID.
     * Returns Optional<PersonDetailsDTO> — raw entity never leaves the service layer.
     */
    public Optional<PersonDetailsDTO> getById(Integer personMalId) {
        return repository.findById(personMalId)
                         .map(PersonDetailsDTO::fromEntity);
    }

    public boolean existsById(Integer personMalId) {
        return repository.existsById(personMalId);
    }

    public long count() {
        return repository.count();
    }

    // ── Relation helpers ──────────────────────────────────────────────────────

    /**
     * Returns a paginated page of DetailsDTO for all anime this person has
     * worked on (via PersonVoiceWorks). Delegates to the repository query —
     * controller never touches the repository directly.
     */
    public Page<DetailsDTO> getAnimeWorksForPerson(Integer personMalId, Pageable pageable) {
        return repository.findAnimeWorks(personMalId, pageable)
                         .map(DetailsDTO::fromEntity);
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
     * Find people with optional filters, returning a paginated page of DTOs.
     * The Page<PersonDetails> result is mapped to Page<PersonDetailsDTO> so
     * the raw JPA entity never leaves the service layer.
     */
    public Page<PersonDetailsDTO> findWithFilters(
            String search, String city, Pageable pageable) {

        String searchPattern = likePattern(search);
        String cityPattern   = likePattern(city);

        if (searchPattern != null) {
            return repository.searchByName(searchPattern, pageable)
                    .map(PersonDetailsDTO::fromEntity);
        }
        if (cityPattern != null) {
            return repository.findByCityContaining(cityPattern, pageable)
                    .map(PersonDetailsDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(PersonDetailsDTO::fromEntity);
    }

    /**
     * Overload that also handles IS NULL / IS NOT NULL filters.
     */
    public Page<PersonDetailsDTO> findWithFilters(
            String search, String city,
            String nullFilter, String notNullFilter,
            Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, city, pageable);
    }

    /**
     * Route IS NULL filter to the correct derived repository method.
     */
    private Page<PersonDetailsDTO> handleNullFilter(String field, Pageable pageable) {
        Page<PersonDetails> result = switch (field.toLowerCase()) {
            case "website_url", "websiteurl"             -> repository.findByWebsiteUrlIsNull(pageable);
            case "given_name", "givenname"               -> repository.findByGivenNameIsNull(pageable);
            case "family_name", "familyname"             -> repository.findByFamilyNameIsNull(pageable);
            case "birthday"                              -> repository.findByBirthdayIsNull(pageable);
            case "relevant_location", "relevantlocation" -> repository.findByRelevantLocationIsNull(pageable);
            default                                      -> repository.findAll(pageable);
        };
        return result.map(PersonDetailsDTO::fromEntity);
    }

    /**
     * Route IS NOT NULL filter to the correct derived repository method.
     */
    private Page<PersonDetailsDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<PersonDetails> result = switch (field.toLowerCase()) {
            case "website_url", "websiteurl"             -> repository.findByWebsiteUrlIsNotNull(pageable);
            case "given_name", "givenname"               -> repository.findByGivenNameIsNotNull(pageable);
            case "family_name", "familyname"             -> repository.findByFamilyNameIsNotNull(pageable);
            case "birthday"                              -> repository.findByBirthdayIsNotNull(pageable);
            case "relevant_location", "relevantlocation" -> repository.findByRelevantLocationIsNotNull(pageable);
            default                                      -> repository.findAll(pageable);
        };
        return result.map(PersonDetailsDTO::fromEntity);
    }

    /**
     * Returns a map of field names to the count of records where that field is NULL.
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("website_url",       repository.countByWebsiteUrlIsNull());
        counts.put("given_name",        repository.countByGivenNameIsNull());
        counts.put("family_name",       repository.countByFamilyNameIsNull());
        counts.put("birthday",          repository.countByBirthdayIsNull());
        counts.put("relevant_location", repository.countByRelevantLocationIsNull());
        return counts;
    }
}
