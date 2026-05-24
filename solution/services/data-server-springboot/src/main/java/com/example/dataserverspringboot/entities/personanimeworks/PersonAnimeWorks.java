package com.example.dataserverspringboot.entities.personanimeworks;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity representing a staff production credit.
 *
 * <p>Maps to the {@code person_anime_works} table in PostgreSQL. Each row
 * links a person (staff member) to an anime with a specific production role
 * (position), such as Director, Music, or Animation Director.</p>
 *
 * <p>The primary key is a three-column composite key:
 * {@code (person_mal_id, position, anime_mal_id)}.
 * The composite key class {@link PersonAnimeWorksId} is declared via
 * {@code @IdClass} and implements {@link Serializable} with
 * {@code equals()} and {@code hashCode()} as required by the JPA specification.</p>
 *
 * <p>All three key columns are non-nullable. There are no nullable
 * non-key fields — every column in the table is part of the primary key.</p>
 */
@Schema(description = "Anime staff works entity (Relationship between Person and Anime with Position)")
@Entity
@Table(name = "person_anime_works")
@IdClass(PersonAnimeWorks.PersonAnimeWorksId.class)
public class PersonAnimeWorks {

    /** Person MAL ID — first part of the composite primary key. */
    @Schema(description = "Person MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "person_mal_id")
    private Integer personMalId;

    /**
     * Staff production role — second part of the composite primary key.
     * Examples: Director, Music, Animation Director, Character Design.
     */
    @Schema(description = "Staff Position/Role (Composite Key)", example = "Director")
    @Id
    @Column(name = "position")
    private String position;

    /** Anime MAL ID — third part of the composite primary key. */
    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "anime_mal_id")
    private Integer animeMalId;

    /** Required no-args constructor for JPA. */
    public PersonAnimeWorks() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param personMalId person MAL ID
     * @param position    staff production role
     * @param animeMalId  anime MAL ID
     */
    public PersonAnimeWorks(Integer personMalId, String position, Integer animeMalId) {
        this.personMalId = personMalId;
        this.position    = position;
        this.animeMalId  = animeMalId;
    }

    /** @return person MAL ID */
    public Integer getPersonMalId() { return personMalId; }
    /** @param personMalId person MAL ID */
    public void setPersonMalId(Integer personMalId) { this.personMalId = personMalId; }

    /** @return staff production role */
    public String getPosition() { return position; }
    /** @param position staff production role */
    public void setPosition(String position) { this.position = position; }

    /** @return anime MAL ID */
    public Integer getAnimeMalId() { return animeMalId; }
    /** @param animeMalId anime MAL ID */
    public void setAnimeMalId(Integer animeMalId) { this.animeMalId = animeMalId; }

    @Override
    public String toString() {
        return "PersonAnimeWorks{person_mal_id=" + personMalId
                + ", position='" + position + "', anime_mal_id=" + animeMalId + '}';
    }

    /**
     * Composite primary key class for {@link PersonAnimeWorks}.
     *
     * <p>Required by JPA when using {@code @IdClass}. Must implement
     * {@link Serializable} and override {@link #equals(Object)} and
     * {@link #hashCode()} using all three key fields so that the persistence
     * context can correctly identify entity instances.</p>
     */
    public static class PersonAnimeWorksId implements Serializable {

        private Integer personMalId;
        private String  position;
        private Integer animeMalId;

        /** Required no-args constructor for JPA. */
        public PersonAnimeWorksId() {}

        /**
         * Full constructor.
         *
         * @param personMalId person MAL ID
         * @param position    staff production role
         * @param animeMalId  anime MAL ID
         */
        public PersonAnimeWorksId(Integer personMalId, String position, Integer animeMalId) {
            this.personMalId = personMalId;
            this.position    = position;
            this.animeMalId  = animeMalId;
        }

        /** @return person MAL ID */
        public Integer getPersonMalId() { return personMalId; }
        /** @param personMalId person MAL ID */
        public void setPersonMalId(Integer personMalId) { this.personMalId = personMalId; }

        /** @return staff production role */
        public String getPosition() { return position; }
        /** @param position staff production role */
        public void setPosition(String position) { this.position = position; }

        /** @return anime MAL ID */
        public Integer getAnimeMalId() { return animeMalId; }
        /** @param animeMalId anime MAL ID */
        public void setAnimeMalId(Integer animeMalId) { this.animeMalId = animeMalId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonAnimeWorksId that = (PersonAnimeWorksId) o;
            return Objects.equals(personMalId, that.personMalId)
                    && Objects.equals(position,    that.position)
                    && Objects.equals(animeMalId,  that.animeMalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(personMalId, position, animeMalId);
        }
    }
}
