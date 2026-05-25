package com.example.dataserverspringboot.entities.personvoiceworks;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity representing a voice acting assignment.
 *
 * <p>Maps to the {@code person_voice_works} table in PostgreSQL. Each row
 * links a person (voice actor), a character, and an anime, and records the
 * role type and the language of the dubbing.</p>
 *
 * <p>The primary key is a three-column composite key:
 * {@code (person_mal_id, character_mal_id, anime_mal_id)}.
 * The composite key class {@link PersonVoiceWorksId} is declared via
 * {@code @IdClass} and implements {@link Serializable} with
 * {@code equals()} and {@code hashCode()} as required by the JPA specification.</p>
 *
 * <p>Fields {@code role} and {@code language} are nullable and are filtered
 * via IS NULL / IS NOT NULL in {@link PersonVoiceWorksService}.</p>
 */
@Schema(description = "Anime staff voice characters entity (Relationship between Person, Character, and Anime)")
@Entity
@Table(name = "person_voice_works")
@IdClass(PersonVoiceWorks.PersonVoiceWorksId.class)
public class PersonVoiceWorks {

    /** Person MAL ID — first part of the composite primary key. */
    @Schema(description = "Person MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "person_mal_id")
    private Integer personMalId;

    /** Character MAL ID — second part of the composite primary key. */
    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;

    /** Anime MAL ID — third part of the composite primary key. */
    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "anime_mal_id")
    private Integer animeMalId;

    /** Role type in the anime, e.g. Main or Supporting — nullable. */
    @Schema(description = "Role type (e.g., Main, Supporting)", example = "Main")
    @Column(name = "role")
    private String role;

    /** Language of the voice work, e.g. Japanese — nullable. */
    @Schema(description = "Language of the voice work", example = "Japanese")
    @Column(name = "language")
    private String language;

    /** Required no-args constructor for JPA. */
    public PersonVoiceWorks() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param personMalId    person MAL ID
     * @param characterMalId character MAL ID
     * @param animeMalId     anime MAL ID
     * @param role           role type (nullable)
     * @param language       dubbing language (nullable)
     */
    public PersonVoiceWorks(Integer personMalId, Integer characterMalId,
                             Integer animeMalId, String role, String language) {
        this.personMalId    = personMalId;
        this.characterMalId = characterMalId;
        this.animeMalId     = animeMalId;
        this.role           = role;
        this.language       = language;
    }

    /** @return person MAL ID */
    public Integer getPersonMalId()    { return personMalId; }
    /** @param personMalId person MAL ID */
    public void setPersonMalId(Integer personMalId) { this.personMalId = personMalId; }

    /** @return character MAL ID */
    public Integer getCharacterMalId() { return characterMalId; }
    /** @param characterMalId character MAL ID */
    public void setCharacterMalId(Integer characterMalId) { this.characterMalId = characterMalId; }

    /** @return anime MAL ID */
    public Integer getAnimeMalId()     { return animeMalId; }
    /** @param animeMalId anime MAL ID */
    public void setAnimeMalId(Integer animeMalId) { this.animeMalId = animeMalId; }

    /** @return role type, or {@code null} if not set */
    public String getRole()            { return role; }
    /** @param role role type */
    public void setRole(String role)   { this.role = role; }

    /** @return dubbing language, or {@code null} if not set */
    public String getLanguage()        { return language; }
    /** @param language dubbing language */
    public void setLanguage(String language) { this.language = language; }

    @Override
    public String toString() {
        return "PersonVoiceWorks{personMalId=" + personMalId
                + ", characterMalId=" + characterMalId
                + ", animeMalId=" + animeMalId
                + ", role='" + role + "', language='" + language + "'}";
    }

    /**
     * Composite primary key class for {@link PersonVoiceWorks}.
     *
     * <p>Required by JPA when using {@code @IdClass}. Must implement
     * {@link Serializable} and override {@link #equals(Object)} and
     * {@link #hashCode()} using all three key fields so that the persistence
     * context can correctly identify entity instances.</p>
     */
    public static class PersonVoiceWorksId implements Serializable {

        private Integer personMalId;
        private Integer characterMalId;
        private Integer animeMalId;

        /** Required no-args constructor for JPA. */
        public PersonVoiceWorksId() {}

        /**
         * Full constructor.
         *
         * @param personMalId    person MAL ID
         * @param characterMalId character MAL ID
         * @param animeMalId     anime MAL ID
         */
        public PersonVoiceWorksId(Integer personMalId, Integer characterMalId,
                                   Integer animeMalId) {
            this.personMalId    = personMalId;
            this.characterMalId = characterMalId;
            this.animeMalId     = animeMalId;
        }

        /** @return person MAL ID */
        public Integer getPersonMalId()    { return personMalId; }
        /** @param personMalId person MAL ID */
        public void setPersonMalId(Integer personMalId) { this.personMalId = personMalId; }

        /** @return character MAL ID */
        public Integer getCharacterMalId() { return characterMalId; }
        /** @param characterMalId character MAL ID */
        public void setCharacterMalId(Integer characterMalId) { this.characterMalId = characterMalId; }

        /** @return anime MAL ID */
        public Integer getAnimeMalId()     { return animeMalId; }
        /** @param animeMalId anime MAL ID */
        public void setAnimeMalId(Integer animeMalId) { this.animeMalId = animeMalId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonVoiceWorksId that = (PersonVoiceWorksId) o;
            return Objects.equals(personMalId,    that.personMalId)
                    && Objects.equals(characterMalId, that.characterMalId)
                    && Objects.equals(animeMalId,     that.animeMalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(personMalId, characterMalId, animeMalId);
        }
    }
}
