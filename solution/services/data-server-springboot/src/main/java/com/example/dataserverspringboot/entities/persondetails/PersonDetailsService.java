package com.example.dataserverspringboot.entities.persondetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PersonDetailsService {

    @Autowired
    private PersonDetailsRepository repository;

    public Optional<PersonDetails> getById(Integer person_mal_id) {
        return repository.findById(person_mal_id);
    }

    public long count() {
        return repository.count();
    }

    public Page<PersonDetails> findWithFilters(String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByName(search, pageable);
        }

        return repository.findAll(pageable);
    }
}
