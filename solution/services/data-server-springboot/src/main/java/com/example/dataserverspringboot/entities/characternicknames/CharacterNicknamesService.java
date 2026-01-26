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

    public Page<CharacterNicknames> findWithFilters(String search, Integer character_mal_id, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByNickname(search, pageable);
        }

        if (character_mal_id != null) {
            return repository.findByCharacterMalId(character_mal_id, pageable);
        }

        return repository.findAll(pageable);
    }
}
