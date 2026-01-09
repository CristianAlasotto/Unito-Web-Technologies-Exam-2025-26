package com.example.dataserverspringboot.entities.character_anime_works;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "character_anime_works")
@IdClass(Character_anime_works.Character_anime_worksId.class)
public class Character_anime_works {

    @Id
    @Column(name = "character_mal_id")
    private Integer character_mal_id;

    @Id
    @Column(name = "anime_mal_id")
    private Integer anime_mal_id;

    @Column(name = "character_name")
    private String character_name;

    @Column(name = "role")
    private String role;

    // Constructors
    public Character_anime_works() {
    }

    public Character_anime_works(Integer character_mal_id, Integer anime_mal_id,
                                 String character_name, String role) {
        this.character_mal_id = character_mal_id;
        this.anime_mal_id = anime_mal_id;
        this.character_name = character_name;
        this.role = role;
    }

    // Getters and Setters
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

    public String getCharacter_name() {
        return character_name;
    }

    public void setCharacter_name(String character_name) {
        this.character_name = character_name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Character_anime_works{" +
                "character_mal_id=" + character_mal_id +
                ", anime_mal_id=" + anime_mal_id +
                ", character_name='" + character_name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    // Composite Key Class
    public static class Character_anime_worksId implements Serializable {
        private Integer character_mal_id;
        private Integer anime_mal_id;

        public Character_anime_worksId() {
        }

        public Character_anime_worksId(Integer character_mal_id, Integer anime_mal_id) {
            this.character_mal_id = character_mal_id;
            this.anime_mal_id = anime_mal_id;
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
            Character_anime_worksId that = (Character_anime_worksId) o;
            return Objects.equals(character_mal_id, that.character_mal_id) &&
                   Objects.equals(anime_mal_id, that.anime_mal_id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(character_mal_id, anime_mal_id);
        }
    }
}
