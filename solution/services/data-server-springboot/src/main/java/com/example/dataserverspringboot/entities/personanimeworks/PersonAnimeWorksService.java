package com.example.dataserverspringboot.entities.personanimeworks;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Hidden
@Service
public class PersonAnimeWorksService {

    @Autowired
    private PersonAnimeWorksRepository repository;

    // ── Basic lookups ─────────────────────────────────────────────────────────

    /**
     * Fetch a single record by composite key.
     * Returns Optional<PersonAnimeWorksDTO> — raw entity never leaves the service layer.
     */
    public Optional<PersonAnimeWorksDTO> getById(PersonAnimeWorks.PersonAnimeWorksId id) {
        return repository.findById(id)
                         .map(PersonAnimeWorksDTO::fromEntity);
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
     * The Page<PersonAnimeWorks> result is mapped to Page<PersonAnimeWorksDTO>
     * so the raw JPA entity never leaves the service layer.
     */
    public Page<PersonAnimeWorksDTO> findWithFilters(
            String search, String position,
            Integer personMalId, Integer animeMalId,
            Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByPosition(searchPattern, pageable)
                    .map(PersonAnimeWorksDTO::fromEntity);
        }
        if (position != null) {
            return repository.findByPosition(position, pageable)
                    .map(PersonAnimeWorksDTO::fromEntity);
        }
        if (personMalId != null) {
            return repository.findByPersonMalId(personMalId, pageable)
                    .map(PersonAnimeWorksDTO::fromEntity);
        }
        if (animeMalId != null) {
            return repository.findByAnimeMalId(animeMalId, pageable)
                    .map(PersonAnimeWorksDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(PersonAnimeWorksDTO::fromEntity);
    }
}
