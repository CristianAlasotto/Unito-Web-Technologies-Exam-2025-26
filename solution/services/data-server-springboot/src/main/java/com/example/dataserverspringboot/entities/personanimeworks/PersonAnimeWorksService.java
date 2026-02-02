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

    public Optional<PersonAnimeWorks> getById(PersonAnimeWorks.PersonAnimeWorksId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<PersonAnimeWorks> findWithFilters(String search, String position, Integer personMalId, Integer animeMalId, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByPosition(search, pageable);
        }

        if (position != null) {
            return repository.findByPosition(position, pageable);
        }

        if (personMalId != null) {
            return repository.findByPersonMalId(personMalId, pageable);
        }

        if (animeMalId != null) {
            return repository.findByAnimeMalId(animeMalId, pageable);
        }

        return repository.findAll(pageable);
    }
}
