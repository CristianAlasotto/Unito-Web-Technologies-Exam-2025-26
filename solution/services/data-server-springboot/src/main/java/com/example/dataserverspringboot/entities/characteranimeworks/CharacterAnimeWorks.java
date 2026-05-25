package com.example.dataserverspringboot.entities.characteranimeworks;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity representing a character's appearance in a specific anime.
 *
 * <p>Maps to the {@code character_anime_works} table in PostgreSQL — the
 * junction table between {@code characters} and {@code details}. Each row
 * records which character appears in which anime, the name used for the
 * character in that anime, and the character's role (Main, Supporting, etc.).</p>
 *
 * <p>The primary key is a two-column composite key:
 * {@code (character_mal_id, anime_mal_id)}.
 * The composite key class {@link CharacterAnimeWorksId} is declared via
 * {@code @IdClass} and implements {@link Serializable} with
 * {@code equals()} and {@code hashCode()} as required by the JPA specification.</p>
 *
 * <p>Non-key nullable fields: {@code character_name}, {@code role}.
 * These are filtered via IS NULL / IS NOT NULL in
 * {@link CharacterAnimeWorksService}.</p>
 */
@Schema(description = "Anime character works entity (Relationship between Character and Anime)")
@Entity
@Table(name = "character_anime_works")
@IdClass(CharacterAnimeWorks.CharacterAnimeWorksId.class)
public class CharacterAnimeWorks {

    /** Character MAL ID — first part of the composite primary key. */
    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;

    /** Anime MAL ID — second part of the composite primary key. */
    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "anime_mal_id")
    private Integer animeMalId;

    /**
     * Character name as credited in this specific anime — nullable.
     * May differ from the canonical name in the {@code characters} table.
     */
    @Schema(description = "Character name as credited in this specific anime",
            example = "Spike Spiegel")
    @Column(name = "character_name")
    private String characterName;

    /** Role of the character in the anime, e.g. Main or Supporting — nullable. */
    @Schema(description = "Role of the character in the anime (e.g., Main, Supporting)",
            example = "Main")
    @Column(name = "role")
    private String role;

    /** Required no-args constructor for JPA. */
    public CharacterAnimeWorks() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param characterMalId character MAL ID
     * @param animeMalId     anime MAL ID
     * @param characterName  character name as credited (nullable)
     * @param role           character role (nullable)
     */
    public CharacterAnimeWorks(Integer characterMalId, Integer animeMalId,
                                String characterName, String role) {
        this.characterMalId = characterMalId;
        this.animeMalId     = animeMalId;
        this.characterName  = characterName;
        this.role           = role;
    }

    /** @return character MAL ID */
    public Integer getCharacterMalId()                        { return characterMalId; }
    /** @param characterMalId character MAL ID */
    public void setCharacterMalId(Integer characterMalId)    { this.characterMalId = characterMalId; }

    /** @return anime MAL ID */
    public Integer getAnimeMalId()                            { return animeMalId; }
    /** @param animeMalId anime MAL ID */
    public void setAnimeMalId(Integer animeMalId)             { this.animeMalId = animeMalId; }

    /** @return character name as credited in this anime, or {@code null} if not set */
    public String getCharacterName()                          { return characterName; }
    /** @param characterName character name as credited */
    public void setCharacterName(String characterName)        { this.characterName = characterName; }

    /** @return character role, or {@code null} if not set */
    public String getRole()                                   { return role; }
    /** @param role character role */
    public void setRole(String role)                          { this.role = role; }

    @Override
    public String toString() {
        return "CharacterAnimeWorks{characterMalId=" + characterMalId
                + ", animeMalId=" + animeMalId
                + ", characterName='" + characterName
                + "', role='" + role + "'}";
    }

    /**
     * Composite primary key class for {@link CharacterAnimeWorks}.
     *
     * <p>Required by JPA when using {@code @IdClass}. Must implement
     * {@link Serializable} and override {@link #equals(Object)} and
     * {@link #hashCode()} using both key fields so that the persistence
     * context can correctly identify entity instances.</p>
     */
    public static class CharacterAnimeWorksId implements Serializable {

        private Integer characterMalId;
        private Integer animeMalId;

        /** Required no-args constructor for JPA. */
        public CharacterAnimeWorksId() {}

        /**
         * Full constructor.
         *
         * @param characterMalId character MAL ID
         * @param animeMalId     anime MAL ID
         */
        public CharacterAnimeWorksId(Integer characterMalId, Integer animeMalId) {
            this.characterMalId = characterMalId;
            this.animeMalId     = animeMalId;
        }

        /** @return character MAL ID */
        public Integer getCharacterMalId()                    { return characterMalId; }
        /** @param characterMalId character MAL ID */
        public void setCharacterMalId(Integer characterMalId) { this.characterMalId = characterMalId; }

        /** @return anime MAL ID */
        public Integer getAnimeMalId()                        { return animeMalId; }
        /** @param animeMalId anime MAL ID */
        public void setAnimeMalId(Integer animeMalId)         { this.animeMalId = animeMalId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CharacterAnimeWorksId that = (CharacterAnimeWorksId) o;
            return Objects.equals(characterMalId, that.characterMalId)
                    && Objects.equals(animeMalId, that.animeMalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterMalId, animeMalId);
        }
    }
}
