package com.example.dataserverspringboot.entities.personalternatenames;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PersonAlternateNamesService {

    @Autowired
    private PersonAlternateNamesRepository repository;

    public Optional<PersonAlternateNames> getById(PersonAlternateNames.PersonAlternateNamesId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<PersonAlternateNames> findWithFilters(String search, Integer person_mal_id, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByAltName(search, pageable);
        }

        if (person_mal_id != null) {
            return repository.findByPersonMalId(person_mal_id, pageable);
        }

        return repository.findAll(pageable);
    }
}
