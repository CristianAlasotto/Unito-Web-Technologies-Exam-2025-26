package com.example.dataserverspringboot.entities.character_nicknames;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "character_nicknames")
@IdClass(Character_nicknames.Character_nicknamesId.class)
public class Character_nicknames {
    
    @Id
    @Column(name = "character_mal_id")
    private Integer character_mal_id;
    
    @Id
    @Column(name = "nickname")
    private String nickname;

    // Constructors
    public Character_nicknames() {
    }

    public Character_nicknames(Integer character_mal_id, String nickname) {
        this.character_mal_id = character_mal_id;
        this.nickname = nickname;
    }

    // Getters and Setters
    public Integer getCharacter_mal_id() {
        return character_mal_id;
    }

    public void setCharacter_mal_id(Integer character_mal_id) {
        this.character_mal_id = character_mal_id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "Character_nicknames{" +
                "character_mal_id=" + character_mal_id +
                ", nickname='" + nickname + '\'' +
                '}';
    }

    // Composite Key Class
    public static class Character_nicknamesId implements Serializable {
        private Integer character_mal_id;
        private String nickname;

        public Character_nicknamesId() {
        }

        public Character_nicknamesId(Integer character_mal_id, String nickname) {
            this.character_mal_id = character_mal_id;
            this.nickname = nickname;
        }

        public Integer getCharacter_mal_id() {
            return character_mal_id;
        }

        public void setCharacter_mal_id(Integer character_mal_id) {
            this.character_mal_id = character_mal_id;
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
            Character_nicknamesId that = (Character_nicknamesId) o;
            return Objects.equals(character_mal_id, that.character_mal_id) && 
                   Objects.equals(nickname, that.nickname);
        }

        @Override
        public int hashCode() {
            return Objects.hash(character_mal_id, nickname);
        }
    }
}
