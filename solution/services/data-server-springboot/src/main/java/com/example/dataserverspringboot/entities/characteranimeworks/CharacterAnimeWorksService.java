package com.example.dataserverspringboot.entities.characteranimeworks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CharacterAnimeWorksService {

    @Autowired
    private CharacterAnimeWorksRepository repository;

    public Optional<CharacterAnimeWorks> getById(CharacterAnimeWorks.CharacterAnimeWorksId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<CharacterAnimeWorks> findWithFilters(String search, String role, Integer characterMalId, Integer animeMalId, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByRole(search, pageable);
        }

        if (role != null) {
            return repository.findByRole(role, pageable);
        }

        if (characterMalId != null) {
            return repository.findByCharacterMalId(characterMalId, pageable);
        }

        if (animeMalId != null) {
            return repository.findByAnimeMalId(animeMalId, pageable);
        }

        return repository.findAll(pageable);
    }
}
