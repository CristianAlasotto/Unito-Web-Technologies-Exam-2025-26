package com.example.dataserverspringboot.entities.characternicknames;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CharacterNicknamesService {

    @Autowired
    private CharacterNicknamesRepository repository;

    public Optional<CharacterNicknames> getById(CharacterNicknames.CharacterNicknamesId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<CharacterNicknames> findWithFilters(String search, Integer characterMalId, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByNickname(search, pageable);
        }

        if (characterMalId != null) {
            return repository.findByCharacterMalId(characterMalId, pageable);
        }

        return repository.findAll(pageable);
    }
}
