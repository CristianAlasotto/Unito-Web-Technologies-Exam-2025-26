package com.example.dataserverspringboot.entities.characternicknames;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Hidden
@Service
public class CharacterNicknamesService {

    @Autowired
    private CharacterNicknamesRepository repository;

    // ── Basic lookups ─────────────────────────────────────────────────────────

    /**
     * Fetch a single record by composite key.
     * Returns Optional<CharacterNicknamesDTO> — raw entity never leaves the service layer.
     */
    public Optional<CharacterNicknamesDTO> getById(CharacterNicknames.CharacterNicknamesId id) {
        return repository.findById(id)
                         .map(CharacterNicknamesDTO::fromEntity);
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
     * The Page<CharacterNicknames> result is mapped to Page<CharacterNicknamesDTO>
     * so the raw JPA entity never leaves the service layer.
     */
    public Page<CharacterNicknamesDTO> findWithFilters(
            String search, Integer characterMalId, Pageable pageable) {

        String searchPattern = likePattern(search);

        if (searchPattern != null) {
            return repository.searchByNickname(searchPattern, pageable)
                    .map(CharacterNicknamesDTO::fromEntity);
        }
        if (characterMalId != null) {
            return repository.findByCharacterMalId(characterMalId, pageable)
                    .map(CharacterNicknamesDTO::fromEntity);
        }
        return repository.findAll(pageable)
                .map(CharacterNicknamesDTO::fromEntity);
    }
}
