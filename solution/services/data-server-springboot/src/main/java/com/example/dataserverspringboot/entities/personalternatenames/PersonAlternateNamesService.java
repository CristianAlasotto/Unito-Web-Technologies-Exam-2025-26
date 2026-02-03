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

    public Optional<PersonAlternateNames> getById(PersonAlternateNames.PersonAlternateNamesId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<PersonAlternateNames> findWithFilters(String search, Integer personMalId, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByAltName(search, pageable);
        }

        if (personMalId != null) {
            return repository.findByPersonMalId(personMalId, pageable);
        }

        return repository.findAll(pageable);
    }
}
