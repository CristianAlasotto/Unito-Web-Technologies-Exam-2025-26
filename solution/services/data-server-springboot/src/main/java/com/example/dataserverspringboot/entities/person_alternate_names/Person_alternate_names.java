package com.example.dataserverspringboot.entities.person_alternate_names;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "person_alternate_names")
@IdClass(Person_alternate_names.Person_alternate_namesId.class)
public class Person_alternate_names {
    
    @Id
    @Column(name = "person_mal_id")
    private Integer person_mal_id;
    
    @Id
    @Column(name = "alt_name")
    private String alt_name;

    // Constructors
    public Person_alternate_names() {
    }

    public Person_alternate_names(Integer person_mal_id, String alt_name) {
        this.person_mal_id = person_mal_id;
        this.alt_name = alt_name;
    }

    // Getters and Setters
    public Integer getPerson_mal_id() {
        return person_mal_id;
    }

    public void setPerson_mal_id(Integer person_mal_id) {
        this.person_mal_id = person_mal_id;
    }

    public String getAlt_name() {
        return alt_name;
    }

    public void setAlt_name(String alt_name) {
        this.alt_name = alt_name;
    }

    @Override
    public String toString() {
        return "Person_alternate_names{" +
                "person_mal_id=" + person_mal_id +
                ", alt_name='" + alt_name + '\'' +
                '}';
    }

    // Composite Key Class
    public static class Person_alternate_namesId implements Serializable {
        private Integer person_mal_id;
        private String alt_name;

        public Person_alternate_namesId() {
        }

        public Person_alternate_namesId(Integer person_mal_id, String alt_name) {
            this.person_mal_id = person_mal_id;
            this.alt_name = alt_name;
        }

        public Integer getPerson_mal_id() {
            return person_mal_id;
        }

        public void setPerson_mal_id(Integer person_mal_id) {
            this.person_mal_id = person_mal_id;
        }

        public String getAlt_name() {
            return alt_name;
        }

        public void setAlt_name(String alt_name) {
            this.alt_name = alt_name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person_alternate_namesId that = (Person_alternate_namesId) o;
            return Objects.equals(person_mal_id, that.person_mal_id) && 
                   Objects.equals(alt_name, that.alt_name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(person_mal_id, alt_name);
        }
    }
}
