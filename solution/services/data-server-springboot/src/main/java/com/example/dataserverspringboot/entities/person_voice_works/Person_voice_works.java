package com.example.dataserverspringboot.entities.person_voice_works;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "person_voice_works")
@IdClass(Person_voice_works.Person_voice_worksId.class)
public class Person_voice_works {
    
    @Id
    @Column(name = "person_mal_id")
    private Integer person_mal_id;
    
    @Id
    @Column(name = "character_mal_id")
    private Integer character_mal_id;
    
    @Id
    @Column(name = "anime_mal_id")
    private Integer anime_mal_id;
    
    @Column(name = "role")
    private String role;
    
    @Column(name = "language")
    private String language;

    // Constructors
    public Person_voice_works() {
    }

    public Person_voice_works(Integer person_mal_id, Integer character_mal_id, 
                              Integer anime_mal_id, String role, String language) {
        this.person_mal_id = person_mal_id;
        this.character_mal_id = character_mal_id;
        this.anime_mal_id = anime_mal_id;
        this.role = role;
        this.language = language;
    }

    // Getters and Setters
    public Integer getPerson_mal_id() {
        return person_mal_id;
    }

    public void setPerson_mal_id(Integer person_mal_id) {
        this.person_mal_id = person_mal_id;
    }

    public Integer getCharacter_mal_id() {
        return character_mal_id;
    }

    public void setCharacter_mal_id(Integer character_mal_id) {
        this.character_mal_id = character_mal_id;
    }

    public Integer getAnime_mal_id() {
        return anime_mal_id;
    }

    public void setAnime_mal_id(Integer anime_mal_id) {
        this.anime_mal_id = anime_mal_id;
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
        return "Person_voice_works{" +
                "person_mal_id=" + person_mal_id +
                ", character_mal_id=" + character_mal_id +
                ", anime_mal_id=" + anime_mal_id +
                ", role='" + role + '\'' +
                ", language='" + language + '\'' +
                '}';
    }

    // Composite Key Class
    public static class Person_voice_worksId implements Serializable {
        private Integer person_mal_id;
        private Integer character_mal_id;
        private Integer anime_mal_id;

        public Person_voice_worksId() {
        }

        public Person_voice_worksId(Integer person_mal_id, Integer character_mal_id, Integer anime_mal_id) {
            this.person_mal_id = person_mal_id;
            this.character_mal_id = character_mal_id;
            this.anime_mal_id = anime_mal_id;
        }

        public Integer getPerson_mal_id() {
            return person_mal_id;
        }

        public void setPerson_mal_id(Integer person_mal_id) {
            this.person_mal_id = person_mal_id;
        }

        public Integer getCharacter_mal_id() {
            return character_mal_id;
        }

        public void setCharacter_mal_id(Integer character_mal_id) {
            this.character_mal_id = character_mal_id;
        }

        public Integer getAnime_mal_id() {
            return anime_mal_id;
        }

        public void setAnime_mal_id(Integer anime_mal_id) {
            this.anime_mal_id = anime_mal_id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person_voice_worksId that = (Person_voice_worksId) o;
            return Objects.equals(person_mal_id, that.person_mal_id) && 
                   Objects.equals(character_mal_id, that.character_mal_id) && 
                   Objects.equals(anime_mal_id, that.anime_mal_id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(person_mal_id, character_mal_id, anime_mal_id);
        }
    }
}
