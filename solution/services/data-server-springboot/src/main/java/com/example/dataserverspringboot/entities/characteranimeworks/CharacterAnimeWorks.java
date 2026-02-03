package com.example.dataserverspringboot.entities.characteranimeworks;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Schema(description = "Anime character works entity (Relationship between Character and Anime)")
@Entity
@Table(name = "character_anime_works")
@IdClass(CharacterAnimeWorks.CharacterAnimeWorksId.class)
public class CharacterAnimeWorks {

    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;

    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "anime_mal_id")
    private Integer animeMalId;

    @Schema(description = "Character name as credited in this specific anime", example = "Spike Spiegel")
    @Column(name = "character_name")
    private String characterName;

    @Schema(description = "Role of the character in the anime (e.g., Main, Supporting)", example = "Main")
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
                "characterMalId=" + characterMalId +
                ", animeMalId=" + animeMalId +
                ", characterName='" + characterName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    // Composite Key Class
    public static class CharacterAnimeWorksId implements Serializable {
        private Integer characterMalId;
        private Integer animeMalId;

        public CharacterAnimeWorksId() {
        }

        public CharacterAnimeWorksId(Integer characterMalId, Integer animeMalId) {
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