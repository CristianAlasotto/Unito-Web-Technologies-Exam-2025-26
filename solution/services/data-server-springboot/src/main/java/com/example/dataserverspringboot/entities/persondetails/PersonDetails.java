package com.example.dataserverspringboot.entities.persondetails;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "person_details")
public class PersonDetails {
    
    @Id
    @Column(name = "person_mal_id")
    private Integer personMalId;
    
    @Column(name = "url")
    private String url;
    
    @Column(name = "website_url")
    private String websiteUrl;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "given_name")
    private String givenName;
    
    @Column(name = "family_name")
    private String familyName;
    
    @Column(name = "birthday")
    private LocalDate birthday;
    
    @Column(name = "favorites")
    private Integer favorites;
    
    @Column(name = "relevant_location")
    private String relevantLocation;

    // Constructors
    public PersonDetails() {
    }

    public PersonDetails(Integer personMalId, String url, String websiteUrl, String imageUrl, 
                          String name, String givenName, String familyName, LocalDate birthday, 
                          Integer favorites, String relevantLocation) {
        this.personMalId = personMalId;
        this.url = url;
        this.websiteUrl = websiteUrl;
        this.imageUrl = imageUrl;
        this.name = name;
        this.givenName = givenName;
        this.familyName = familyName;
        this.birthday = birthday;
        this.favorites = favorites;
        this.relevantLocation = relevantLocation;
    }

    // Getters and Setters
    public Integer getPersonMalId() {
        return personMalId;
    }

    public void setPersonMalId(Integer personMalId) {
        this.personMalId = personMalId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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

    public String getRelevantLocation() {
        return relevantLocation;
    }

    public void setRelevantLocation(String relevantLocation) {
        this.relevantLocation = relevantLocation;
    }

    @Override
    public String toString() {
        return "PersonDetails{" +
                "personMalId=" + personMalId +
                ", name='" + name + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", birthday=" + birthday +
                ", favorites=" + favorites +
                '}';
    }
}
