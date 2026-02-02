package com.example.dataserverspringboot.entities.personalternatenames;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Schema(description = "Anime staff alternate name entity")  // For entity
@Entity
@Table(name = "person_alternate_names")
@IdClass(PersonAlternateNames.PersonAlternateNamesId.class)
public class PersonAlternateNames {
    
    @Id
    @Column(name = "person_mal_id")
    private Integer personMalId;
    
    @Id
    @Column(name = "alt_name")
    private String altName;

    // Constructors
    public PersonAlternateNames() {
    }

    public PersonAlternateNames(Integer personMalId, String altName) {
        this.personMalId = personMalId;
        this.altName = altName;
    }

    // Getters and Setters
    public Integer getPersonMalId() {
        return personMalId;
    }

    public void setPersonMalId(Integer personMalId) {
        this.personMalId = personMalId;
    }

    public String getAltName() {
        return altName;
    }

    public void setAltName(String altName) {
        this.altName = altName;
    }

    @Override
    public String toString() {
        return "PersonAlternateNames{" +
                "person_mal_id=" + personMalId +
                ", alt_name='" + altName + '\'' +
                '}';
    }

    // Composite Key Class
    public static class PersonAlternateNamesId implements Serializable {
        private Integer personMalId;
        private String altName;

        public PersonAlternateNamesId() {
        }

        public PersonAlternateNamesId(Integer personMalId, String altName) {
            this.personMalId = personMalId;
            this.altName = altName;
        }

        public Integer getPersonMalId() {
            return personMalId;
        }

        public void setPersonMalId(Integer personMalId) {
            this.personMalId = personMalId;
        }

        public String getAltName() {
            return altName;
        }

        public void setAltName(String altName) {
            this.altName = altName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonAlternateNamesId that = (PersonAlternateNamesId) o;
            return Objects.equals(personMalId, that.personMalId) && 
                   Objects.equals(altName, that.altName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(personMalId, altName);
        }
    }
}
