package com.example.dataserverspringboot.entities.person_details;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "person_details")
public class Person_details {
    
    @Id
    @Column(name = "person_mal_id")
    private Integer person_mal_id;
    
    @Column(name = "url")
    private String url;
    
    @Column(name = "website_url")
    private String website_url;
    
    @Column(name = "image_url")
    private String image_url;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "given_name")
    private String given_name;
    
    @Column(name = "family_name")
    private String family_name;
    
    @Column(name = "birthday")
    private LocalDate birthday;
    
    @Column(name = "favorites")
    private Integer favorites;
    
    @Column(name = "relevant_location")
    private String relevant_location;

    // Constructors
    public Person_details() {
    }

    public Person_details(Integer person_mal_id, String url, String website_url, String image_url, 
                          String name, String given_name, String family_name, LocalDate birthday, 
                          Integer favorites, String relevant_location) {
        this.person_mal_id = person_mal_id;
        this.url = url;
        this.website_url = website_url;
        this.image_url = image_url;
        this.name = name;
        this.given_name = given_name;
        this.family_name = family_name;
        this.birthday = birthday;
        this.favorites = favorites;
        this.relevant_location = relevant_location;
    }

    // Getters and Setters
    public Integer getPerson_mal_id() {
        return person_mal_id;
    }

    public void setPerson_mal_id(Integer person_mal_id) {
        this.person_mal_id = person_mal_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWebsite_url() {
        return website_url;
    }

    public void setWebsite_url(String website_url) {
        this.website_url = website_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Integer getFavorites() {
        return favorites;
    }

    public void setFavorites(Integer favorites) {
        this.favorites = favorites;
    }

    public String getRelevant_location() {
        return relevant_location;
    }

    public void setRelevant_location(String relevant_location) {
        this.relevant_location = relevant_location;
    }

    @Override
    public String toString() {
        return "Person_details{" +
                "person_mal_id=" + person_mal_id +
                ", name='" + name + '\'' +
                ", given_name='" + given_name + '\'' +
                ", family_name='" + family_name + '\'' +
                ", birthday=" + birthday +
                ", favorites=" + favorites +
                '}';
    }
}
