package com.example.dataserverspringboot.entities.personvoiceworks;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Schema(description = "Anime staff voice characters entity (Relationship between Person, Character, and Anime)")
@Entity
@Table(name = "person_voice_works")
@IdClass(PersonVoiceWorks.PersonVoiceWorksId.class)
public class PersonVoiceWorks {

    @Schema(description = "Person MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "person_mal_id")
    private Integer personMalId;

    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;

    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "anime_mal_id")
    private Integer animeMalId;

    @Schema(description = "Role type (e.g., Main, Supporting)", example = "Main")
    @Column(name = "role")
    private String role;

    @Schema(description = "Language of the voice work", example = "Japanese")
    @Column(name = "language")
    private String language;

    // Constructors
    public PersonVoiceWorks() {
    }

    public PersonVoiceWorks(Integer personMalId, Integer characterMalId,
                              Integer animeMalId, String role, String language) {
        this.personMalId = personMalId;
        this.characterMalId = characterMalId;
        this.animeMalId = animeMalId;
        this.role = role;
        this.language = language;
    }

    // Getters and Setters
    public Integer getPersonMalId() {
        return personMalId;
    }

    public void setPersonMalId(Integer personMalId) {
        this.personMalId = personMalId;
    }

    public Integer getCharacterMalId() {
        return characterMalId;
    }

    public void setCharacterMalId(Integer characterMalId) {
        this.characterMalId = characterMalId;
    }

    public Integer getAnimeMalId() {
        return animeMalId;
    }

    public void setAnimeMalId(Integer animeMalId) {
        this.animeMalId = animeMalId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "PersonVoiceWorks{" +
                "personMalId=" + personMalId +
                ", characterMalId=" + characterMalId +
                ", animeMalId=" + animeMalId +
                ", role='" + role + '\'' +
                ", language='" + language + '\'' +
                '}';
    }

    // Composite Key Class
    public static class PersonVoiceWorksId implements Serializable {
        private Integer personMalId;
        private Integer characterMalId;
        private Integer animeMalId;

        public PersonVoiceWorksId() {
        }

        public PersonVoiceWorksId(Integer personMalId, Integer characterMalId, Integer animeMalId) {
            this.personMalId = personMalId;
            this.characterMalId = characterMalId;
            this.animeMalId = animeMalId;
        }

        public Integer getPersonMalId() {
            return personMalId;
        }

        public void setPersonMalId(Integer personMalId) {
            this.personMalId = personMalId;
        }

        public Integer getCharacterMalId() {
            return characterMalId;
        }

        public void setCharacterMalId(Integer characterMalId) {
            this.characterMalId = characterMalId;
        }

        public Integer getAnimeMalId() {
            return animeMalId;
        }

        public void setAnimeMalId(Integer animeMalId) {
            this.animeMalId = animeMalId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonVoiceWorksId that = (PersonVoiceWorksId) o;
            return Objects.equals(personMalId, that.personMalId) &&
                   Objects.equals(characterMalId, that.characterMalId) &&
                   Objects.equals(animeMalId, that.animeMalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(personMalId, characterMalId, animeMalId);
        }
    }
}