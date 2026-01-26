package com.example.dataserverspringboot.entities.personvoiceworks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PersonVoiceWorksService {

    @Autowired
    private PersonVoiceWorksRepository repository;

    public Optional<PersonVoiceWorks> getById(PersonVoiceWorks.PersonVoiceWorksId id) {
        return repository.findById(id);
    }

    public long count() {
        return repository.count();
    }

    public Page<PersonVoiceWorks> findWithFilters(String search, String language, String role, Integer personMalId, Integer characterMalId, Integer animeMalId, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return repository.searchByLanguage(search, pageable);
        }

        if (language != null) {
            return repository.findByLanguage(language, pageable);
        }

        if (role != null) {
            return repository.findByRole(role, pageable);
        }

        if (personMalId != null) {
            return repository.findByPersonMalId(personMalId, pageable);
        }

        if (characterMalId != null) {
            return repository.findByCharacterMalId(characterMalId, pageable);
        }

        if (animeMalId != null) {
            return repository.findByAnimeMalId(animeMalId, pageable);
        }

        return repository.findAll(pageable);
    }
}
