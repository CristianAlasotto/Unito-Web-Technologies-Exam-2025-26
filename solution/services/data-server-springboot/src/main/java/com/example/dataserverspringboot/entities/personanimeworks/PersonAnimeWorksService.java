package com.example.dataserverspringboot.entities.personanimeworks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PersonAnimeWorksService {

    @Autowired
    private PersonAnimeWorksRepository repository;

    public Optional<PersonAnimeWorks> getById(PersonAnimeWorks.PersonAnimeWorksId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<PersonAnimeWorks> findWithFilters(String search, String position, Integer person_mal_id, Integer anime_mal_id, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByPosition(search, pageable);
        }

        if (position != null) {
            return repository.findByPosition(position, pageable);
        }

        if (person_mal_id != null) {
            return repository.findByPersonMalId(person_mal_id, pageable);
        }

        if (anime_mal_id != null) {
            return repository.findByAnimeMalId(anime_mal_id, pageable);
        }

        return repository.findAll(pageable);
    }
}
