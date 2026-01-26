package com.example.dataserverspringboot.entities.characters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CharactersService {

    @Autowired
    private CharactersRepository repository;

    public Optional<Characters> getById(Integer character_mal_id) {
        return repository.findById(character_mal_id);
    }

    public long count() {
        return repository.count();
    }

    public Page<Characters> findWithFilters(String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByName(search, pageable);
        }

        return repository.findAll(pageable);
    }
}
