package com.example.dataserverspringboot.entities.characternicknames;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Schema(description = "Anime character nicknames entity")
@Entity
@Table(name = "character_nicknames")
@IdClass(CharacterNicknames.CharacterNicknamesId.class)
public class CharacterNicknames {
    
    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;
    
    @Id
    @Column(name = "nickname")
    private String nickname;

    // Constructors
    public CharacterNicknames() {
    }

    public CharacterNicknames(Integer characterMalId, String nickname) {
        this.characterMalId = characterMalId;
        this.nickname = nickname;
    }

    // Getters and Setters
    public Integer getCharacterMalId() {
        return characterMalId;
    }

    public void setCharacterMalId(Integer characterMalId) {
        this.characterMalId = characterMalId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "CharacterNicknames{" +
                "character_mal_id=" + characterMalId +
                ", nickname='" + nickname + '\'' +
                '}';
    }

    // Composite Key Class
    public static class CharacterNicknamesId implements Serializable {
        private Integer characterMalId;
        private String nickname;

        public CharacterNicknamesId() {
        }

        public CharacterNicknamesId(Integer characterMalId, String nickname) {
            this.characterMalId = characterMalId;
            this.nickname = nickname;
        }

        public Integer getCharacterMalId() {
            return characterMalId;
        }

        public void setCharacterMalId(Integer characterMalId) {
            this.characterMalId = characterMalId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CharacterNicknamesId that = (CharacterNicknamesId) o;
            return Objects.equals(characterMalId, that.characterMalId) && 
                   Objects.equals(nickname, that.nickname);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterMalId, nickname);
        }
    }
}
