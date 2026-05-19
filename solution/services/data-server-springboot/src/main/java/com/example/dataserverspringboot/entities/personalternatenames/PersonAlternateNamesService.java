package com.example.dataserverspringboot.entities.personalternatenames;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Hidden
@Service
public class PersonAlternateNamesService {

    @Autowired
    private PersonAlternateNamesRepository repository;

    // ── Basic lookups ─────────────────────────────────────────────────────────

    /**
     * Fetch a single record by composite key.
     * Returns Optional<PersonAlternateNamesDTO> — raw entity never leaves the service layer.
     */
    public Optional<PersonAlternateNamesDTO> getById(PersonAlternateNames.PersonAlternateNamesId id) {
        return repository.findById(id)
                         .map(PersonAlternateNamesDTO::fromEntity);
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
     * Find records with optional filters, returning a paginated page of DTOs.
     * The Page<PersonAlternateNames> result is mapped to Page<PersonAlternateNamesDTO>
     * so the raw JPA entity never leaves the service layer.
     */
    public Page<PersonAlternateNamesDTO> findWithFilters(
            String search, Integer personMalId, Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByAltName(searchPattern, pageable)
                    .map(PersonAlternateNamesDTO::fromEntity);
        }
        if (personMalId != null) {
            return repository.findByPersonMalId(personMalId, pageable)
                    .map(PersonAlternateNamesDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(PersonAlternateNamesDTO::fromEntity);
    }
}
