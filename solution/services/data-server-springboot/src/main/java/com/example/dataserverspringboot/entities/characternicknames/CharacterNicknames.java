package com.example.dataserverspringboot.entities.characternicknames;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity representing a nickname for an anime character.
 *
 * <p>Maps to the {@code character_nicknames} table in PostgreSQL. Each row
 * associates one nickname (alias, fan-given name, short form, etc.) with a
 * character identified by their MyAnimeList ID.</p>
 *
 * <p>The primary key is a two-column composite key:
 * {@code (character_mal_id, nickname)}.
 * The composite key class {@link CharacterNicknamesId} is declared via
 * {@code @IdClass} and implements {@link Serializable} with
 * {@code equals()} and {@code hashCode()} as required by the JPA specification.</p>
 *
 * <p>All columns are part of the primary key — there are no nullable
 * non-key fields in this table.</p>
 */
@Schema(description = "Anime character nicknames entity")
@Entity
@Table(name = "character_nicknames")
@IdClass(CharacterNicknames.CharacterNicknamesId.class)
public class CharacterNicknames {

    /** Character MAL ID — first part of the composite primary key. */
    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;

    /** Nickname — second part of the composite primary key. */
    @Schema(description = "Nickname associated with the character (Composite Key)", example = "Spike")
    @Id
    @Column(name = "nickname")
    private String nickname;

    /** Required no-args constructor for JPA. */
    public CharacterNicknames() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param characterMalId character MAL ID
     * @param nickname       nickname string
     */
    public CharacterNicknames(Integer characterMalId, String nickname) {
        this.characterMalId = characterMalId;
        this.nickname       = nickname;
    }

    /** @return character MAL ID */
    public Integer getCharacterMalId()                       { return characterMalId; }
    /** @param characterMalId character MAL ID */
    public void setCharacterMalId(Integer characterMalId)    { this.characterMalId = characterMalId; }

    /** @return nickname string */
    public String getNickname()                              { return nickname; }
    /** @param nickname nickname string */
    public void setNickname(String nickname)                 { this.nickname = nickname; }

    @Override
    public String toString() {
        return "CharacterNicknames{character_mal_id=" + characterMalId
                + ", nickname='" + nickname + "'}";
    }

    /**
     * Composite primary key class for {@link CharacterNicknames}.
     *
     * <p>Required by JPA when using {@code @IdClass}. Must implement
     * {@link Serializable} and override {@link #equals(Object)} and
     * {@link #hashCode()} using both key fields so that the persistence
     * context can correctly identify entity instances.</p>
     */
    public static class CharacterNicknamesId implements Serializable {

        private Integer characterMalId;
        private String  nickname;

        /** Required no-args constructor for JPA. */
        public CharacterNicknamesId() {}

        /**
         * Full constructor.
         *
         * @param characterMalId character MAL ID
         * @param nickname       nickname string
         */
        public CharacterNicknamesId(Integer characterMalId, String nickname) {
            this.characterMalId = characterMalId;
            this.nickname       = nickname;
        }

        /** @return character MAL ID */
        public Integer getCharacterMalId()                    { return characterMalId; }
        /** @param characterMalId character MAL ID */
        public void setCharacterMalId(Integer characterMalId) { this.characterMalId = characterMalId; }

        /** @return nickname string */
        public String getNickname()                           { return nickname; }
        /** @param nickname nickname string */
        public void setNickname(String nickname)              { this.nickname = nickname; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CharacterNicknamesId that = (CharacterNicknamesId) o;
            return Objects.equals(characterMalId, that.characterMalId)
                    && Objects.equals(nickname, that.nickname);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterMalId, nickname);
        }
    }
}
