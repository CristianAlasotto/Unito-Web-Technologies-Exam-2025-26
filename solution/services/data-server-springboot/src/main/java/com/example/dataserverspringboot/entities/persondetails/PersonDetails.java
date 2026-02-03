package com.example.dataserverspringboot.entities.persondetails;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;

@Schema(description = "Anime staff info entity")
@Entity
@Table(name = "person_details")
public class PersonDetails {
    
    @Schema(description = "Person MyAnimeList ID (Primary Key)", example = "1")
    @Id
    @Column(name = "person_mal_id")
    private Integer personMalId;

    @Schema(description = "MyAnimeList URL", example = "https://myanimelist.net/people/1")
    @Column(name = "url")
    private String url;

    @Schema(description = "Personal Website URL", example = "http://www.example.com")
    @Column(name = "website_url")
    private String websiteUrl;

    @Schema(description = "Image URL", example = "https://cdn.myanimelist.net/images/people/1.jpg")
    @Column(name = "image_url")
    private String imageUrl;

    @Schema(description = "Full Name", example = "Miyazaki Hayao")
    @Column(name = "name")
    private String name;

    @Schema(description = "Given Name", example = "Hayao")
    @Column(name = "given_name")
    private String givenName;

    @Schema(description = "Family Name", example = "Miyazaki")
    @Column(name = "family_name")
    private String familyName;

    @Schema(description = "Birthday", example = "1941-01-05")
    @Column(name = "birthday")
    private LocalDate birthday;

    @Schema(description = "User Favorites Count", example = "30000")
    @Column(name = "favorites")
    private Integer favorites;

    @Schema(description = "Relevant Location/Hometown", example = "Tokyo, Japan")
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