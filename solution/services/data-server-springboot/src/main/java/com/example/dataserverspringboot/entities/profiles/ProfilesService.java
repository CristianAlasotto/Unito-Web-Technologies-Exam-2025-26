package com.example.dataserverspringboot.entities.profiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ProfilesService {

    @Autowired
    private ProfilesRepository repository;

    public Optional<Profiles> getById(String username) {
        return repository.findById(username);
    }

    public long count() {
        return repository.count();
    }

    public Page<Profiles> findWithFilters(String search, String gender, String location, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByUsername(search, pageable);
        }

        if (gender != null) {
            return repository.findByGender(gender, pageable);
        }

        if (location != null) {
            return repository.findByLocation(location, pageable);
        }

        return repository.findAll(pageable);
    }
}
