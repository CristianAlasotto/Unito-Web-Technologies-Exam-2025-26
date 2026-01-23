package com.example.dataserverspringboot.entities.personanimeworks;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "person_anime_works")
@IdClass(PersonAnimeWorks.PersonAnimeWorksId.class)
public class PersonAnimeWorks {
    
    @Id
    @Column(name = "person_mal_id")
    private Integer personMalId;
    
    @Id
    @Column(name = "position")
    private String position;
    
    @Id
    @Column(name = "anime_mal_id")
    private Integer animeMalId;

    // Constructors
    public PersonAnimeWorks() {
    }

    public PersonAnimeWorks(Integer personMalId, String position, Integer anime_mal_id) {
        this.personMalId = personMalId;
        this.position = position;
        this.animeMalId = animeMalId;
    }

    // Getters and Setters
    public Integer getPersonMalId() {
        return personMalId;
    }

    public void setPersonMalId(Integer personMalId) {
        this.personMalId = personMalId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getAnimeMalId() {
        return animeMalId;
    }

    public void setAnimeMalId(Integer animeMalId) {
        this.animeMalId = animeMalId;
    }

    @Override
    public String toString() {
        return "PersonAnimeWorks{" +
                "person_mal_id=" + personMalId +
                ", position='" + position + '\'' +
                ", anime_mal_id=" + animeMalId +
                '}';
    }

    // Composite Key Class
    public static class PersonAnimeWorksId implements Serializable {
        private Integer personMalId;
        private String position;
        private Integer animeMalId;

        public PersonAnimeWorksId() {
        }

        public PersonAnimeWorksId(Integer personMalId, String position, Integer anime_mal_id) {
            this.personMalId = personMalId;
            this.position = position;
            this.animeMalId = animeMalId;
        }

        public Integer getPersonMalId() {
            return personMalId;
        }

        public void setPersonMalId(Integer personMalId) {
            this.personMalId = personMalId;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
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
            PersonAnimeWorksId that = (PersonAnimeWorksId) o;
            return Objects.equals(personMalId, that.personMalId) && 
                   Objects.equals(position, that.position) && 
                   Objects.equals(animeMalId, that.animeMalId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(personMalId, position, animeMalId);
        }
    }
}
