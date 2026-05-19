package com.example.dataserverspringboot.entities.personvoiceworks;

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
public class PersonVoiceWorksService {

    @Autowired
    private PersonVoiceWorksRepository repository;

    // ── Basic lookups ─────────────────────────────────────────────────────────

    /**
     * Fetch a single voice work by composite key.
     * Returns Optional<PersonVoiceWorksDTO> — raw entity never leaves the service layer.
     */
    public Optional<PersonVoiceWorksDTO> getById(PersonVoiceWorks.PersonVoiceWorksId id) {
        return repository.findById(id)
                         .map(PersonVoiceWorksDTO::fromEntity);
    }

    public long count() {
        return repository.count();
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
     * Find voice works with optional filters, returning a paginated page of DTOs.
     * The Page<PersonVoiceWorks> result is mapped to Page<PersonVoiceWorksDTO>
     * so the raw JPA entity never leaves the service layer.
     */
    public Page<PersonVoiceWorksDTO> findWithFilters(
            String search, String language, String role,
            Integer personMalId, Integer characterMalId, Integer animeMalId,
            Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByLanguage(searchPattern, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (language != null) {
            return repository.findByLanguage(language, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (role != null) {
            return repository.findByRole(role, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (personMalId != null) {
            return repository.findByPersonMalId(personMalId, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (characterMalId != null) {
            return repository.findByCharacterMalId(characterMalId, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        if (animeMalId != null) {
            return repository.findByAnimeMalId(animeMalId, pageable)
                    .map(PersonVoiceWorksDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(PersonVoiceWorksDTO::fromEntity);
    }

    /**
     * Overload that also handles IS NULL / IS NOT NULL filters.
     */
    public Page<PersonVoiceWorksDTO> findWithFilters(
            String search, String language, String role,
            Integer personMalId, Integer characterMalId, Integer animeMalId,
            String nullFilter, String notNullFilter,
            Pageable pageable) {

        if (nullFilter != null && !nullFilter.isEmpty()) {
            return handleNullFilter(nullFilter, pageable);
        }
        if (notNullFilter != null && !notNullFilter.isEmpty()) {
            return handleNotNullFilter(notNullFilter, pageable);
        }
        return findWithFilters(search, language, role,
                personMalId, characterMalId, animeMalId, pageable);
    }

    /**
     * Route IS NULL filter to the correct derived repository method.
     */
    private Page<PersonVoiceWorksDTO> handleNullFilter(String field, Pageable pageable) {
        Page<PersonVoiceWorks> result = switch (field.toLowerCase()) {
            case "role"     -> repository.findByRoleIsNull(pageable);
            case "language" -> repository.findByLanguageIsNull(pageable);
            default         -> repository.findAll(pageable);
        };
        return result.map(PersonVoiceWorksDTO::fromEntity);
    }

    /**
     * Route IS NOT NULL filter to the correct derived repository method.
     */
    private Page<PersonVoiceWorksDTO> handleNotNullFilter(String field, Pageable pageable) {
        Page<PersonVoiceWorks> result = switch (field.toLowerCase()) {
            case "role"     -> repository.findByRoleIsNotNull(pageable);
            case "language" -> repository.findByLanguageIsNotNull(pageable);
            default         -> repository.findAll(pageable);
        };
        return result.map(PersonVoiceWorksDTO::fromEntity);
    }

    /**
     * Returns a map of field names to the count of records where that field is NULL.
     */
    public Map<String, Long> getNullCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("role",     repository.countByRoleIsNull());
        counts.put("language", repository.countByLanguageIsNull());
        return counts;
    }
}
