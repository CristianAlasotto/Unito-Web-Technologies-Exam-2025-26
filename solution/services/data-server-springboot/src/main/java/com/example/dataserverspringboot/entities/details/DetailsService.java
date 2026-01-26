package com.example.dataserverspringboot.entities.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class DetailsService {

    @Autowired
    private DetailsRepository repository;

    public Optional<Details> getById(Integer mal_id) {
        return repository.findById(mal_id);
    }

    public long count() {
        return repository.count();
    }

    public Page<Details> findWithFilters(String search, String type, Integer year, String status, String rating, String source, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByTitle(search, pageable);
        }

        if (type != null) {
            return repository.findByType(type, pageable);
        }

        if (year != null) {
            return repository.findByYear(year, pageable);
        }

        if (status != null) {
            return repository.findByStatus(status, pageable);
        }

        if (rating != null) {
            return repository.findByRating(rating, pageable);
        }

        if (source != null) {
            return repository.findBySource(source, pageable);
        }

        return repository.findAll(pageable);
    }
}
