package com.example.dataserverspringboot.entities.person_anime_works;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "person_anime_works")
@IdClass(Person_anime_works.Person_anime_worksId.class)
public class Person_anime_works {
    
    @Id
    @Column(name = "person_mal_id")
    private Integer person_mal_id;
    
    @Id
    @Column(name = "position")
    private String position;
    
    @Id
    @Column(name = "anime_mal_id")
    private Integer anime_mal_id;

    // Constructors
    public Person_anime_works() {
    }

    public Person_anime_works(Integer person_mal_id, String position, Integer anime_mal_id) {
        this.person_mal_id = person_mal_id;
        this.position = position;
        this.anime_mal_id = anime_mal_id;
    }

    // Getters and Setters
    public Integer getPerson_mal_id() {
        return person_mal_id;
    }

    public void setPerson_mal_id(Integer person_mal_id) {
        this.person_mal_id = person_mal_id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getAnime_mal_id() {
        return anime_mal_id;
    }

    public void setAnime_mal_id(Integer anime_mal_id) {
        this.anime_mal_id = anime_mal_id;
    }

    @Override
    public String toString() {
        return "Person_anime_works{" +
                "person_mal_id=" + person_mal_id +
                ", position='" + position + '\'' +
                ", anime_mal_id=" + anime_mal_id +
                '}';
    }

    // Composite Key Class
    public static class Person_anime_worksId implements Serializable {
        private Integer person_mal_id;
        private String position;
        private Integer anime_mal_id;

        public Person_anime_worksId() {
        }

        public Person_anime_worksId(Integer person_mal_id, String position, Integer anime_mal_id) {
            this.person_mal_id = person_mal_id;
            this.position = position;
            this.anime_mal_id = anime_mal_id;
        }

        public Integer getPerson_mal_id() {
            return person_mal_id;
        }

        public void setPerson_mal_id(Integer person_mal_id) {
            this.person_mal_id = person_mal_id;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public Integer getAnime_mal_id() {
            return anime_mal_id;
        }

        public void setAnime_mal_id(Integer anime_mal_id) {
            this.anime_mal_id = anime_mal_id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person_anime_worksId that = (Person_anime_worksId) o;
            return Objects.equals(person_mal_id, that.person_mal_id) && 
                   Objects.equals(position, that.position) && 
                   Objects.equals(anime_mal_id, that.anime_mal_id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(person_mal_id, position, anime_mal_id);
        }
    }
}
