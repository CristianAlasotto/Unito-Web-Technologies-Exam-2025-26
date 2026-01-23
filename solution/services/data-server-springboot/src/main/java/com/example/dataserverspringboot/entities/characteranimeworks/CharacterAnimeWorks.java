package com.example.dataserverspringboot.entities.characteranimeworks;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "character_anime_works")
@IdClass(CharacterAnimeWorks.CharacterAnimeWorksId.class)
public class CharacterAnimeWorks {

    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;

    @Id
    @Column(name = "anime_mal_id")
    private Integer animeMalId;

    @Column(name = "character_name")
    private String characterName;

    @Column(name = "role")
    private String role;

    // Constructors
    public CharacterAnimeWorks() {
    }

    public CharacterAnimeWorks(Integer characterMalId, Integer animeMalId,
                                 String characterName, String role) {
        this.characterMalId = characterMalId;
        this.animeMalId = animeMalId;
        this.characterName = characterName;
        this.role = role;
    }

    // Getters and Setters
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

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "CharacterAnimeWorks{" +
                "character_mal_id=" + characterMalId +
                ", anime_mal_id=" + animeMalId +
                ", character_name='" + characterName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    // Composite Key Class
    public static class CharacterAnimeWorksId implements Serializable {
        private Integer characterMalId;
        private Integer animeMalId;

        public CharacterAnimeWorksId() {
        }

        public CharacterAnimeWorksId(Integer characterMalId, Integer anime_mal_id) {
            this.characterMalId = characterMalId;
            this.animeMalId = animeMalId;
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
            CharacterAnimeWorksId that = (CharacterAnimeWorksId) o;
            return Objects.equals(characterMalId, that.characterMalId) &&
                   Objects.equals(animeMalId, that.animeMalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterMalId, animeMalId);
        }
    }
}
