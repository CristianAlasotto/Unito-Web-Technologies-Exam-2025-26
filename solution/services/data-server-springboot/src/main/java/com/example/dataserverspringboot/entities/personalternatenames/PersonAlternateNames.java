package com.example.dataserverspringboot.entities.personalternatenames;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA entity representing an alternate name for a person (staff member or voice actor).
 *
 * <p>Maps to the {@code person_alternate_names} table in PostgreSQL. Each row
 * associates one alternate name (romanisation, stage name, alias, etc.) with a
 * person identified by their MyAnimeList ID.</p>
 *
 * <p>The primary key is a two-column composite key:
 * {@code (person_mal_id, alt_name)}.
 * The composite key class {@link PersonAlternateNamesId} is declared via
 * {@code @IdClass} and implements {@link Serializable} with
 * {@code equals()} and {@code hashCode()} as required by the JPA specification.</p>
 *
 * <p>All columns are part of the primary key — there are no nullable
 * non-key fields in this table.</p>
 */
@Schema(description = "Anime staff alternate name entity")
@Entity
@Table(name = "person_alternate_names")
@IdClass(PersonAlternateNames.PersonAlternateNamesId.class)
public class PersonAlternateNames {

    /** Person MAL ID — first part of the composite primary key. */
    @Schema(description = "Person/Staff MyAnimeList ID (Composite Key)", example = "1")
    @Id
    @Column(name = "person_mal_id")
    private Integer personMalId;

    /** Alternate name — second part of the composite primary key. */
    @Schema(description = "Alternate Name (Composite Key)", example = "Miyazaki Hayao")
    @Id
    @Column(name = "alt_name")
    private String altName;

    /** Required no-args constructor for JPA. */
    public PersonAlternateNames() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param personMalId person MAL ID
     * @param altName     alternate name
     */
    public PersonAlternateNames(Integer personMalId, String altName) {
        this.personMalId = personMalId;
        this.altName     = altName;
    }

    /** @return person MAL ID */
    public Integer getPersonMalId() { return personMalId; }
    /** @param personMalId person MAL ID */
    public void setPersonMalId(Integer personMalId) { this.personMalId = personMalId; }

    /** @return alternate name */
    public String getAltName() { return altName; }
    /** @param altName alternate name */
    public void setAltName(String altName) { this.altName = altName; }

    @Override
    public String toString() {
        return "PersonAlternateNames{person_mal_id=" + personMalId
                + ", alt_name='" + altName + "'}";
    }

    /**
     * Composite primary key class for {@link PersonAlternateNames}.
     *
     * <p>Required by JPA when using {@code @IdClass}. Must implement
     * {@link Serializable} and override {@link #equals(Object)} and
     * {@link #hashCode()} using both key fields so that the persistence
     * context can correctly identify entity instances.</p>
     */
    public static class PersonAlternateNamesId implements Serializable {

        private Integer personMalId;
        private String  altName;

        /** Required no-args constructor for JPA. */
        public PersonAlternateNamesId() {}

        /**
         * Full constructor.
         *
         * @param personMalId person MAL ID
         * @param altName     alternate name
         */
        public PersonAlternateNamesId(Integer personMalId, String altName) {
            this.personMalId = personMalId;
            this.altName     = altName;
        }

        /** @return person MAL ID */
        public Integer getPersonMalId() { return personMalId; }
        /** @param personMalId person MAL ID */
        public void setPersonMalId(Integer personMalId) { this.personMalId = personMalId; }

        /** @return alternate name */
        public String getAltName() { return altName; }
        /** @param altName alternate name */
        public void setAltName(String altName) { this.altName = altName; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonAlternateNamesId that = (PersonAlternateNamesId) o;
            return Objects.equals(personMalId, that.personMalId)
                    && Objects.equals(altName, that.altName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(personMalId, altName);
        }
    }
}
