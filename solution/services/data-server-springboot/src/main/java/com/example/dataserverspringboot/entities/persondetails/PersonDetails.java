package com.example.dataserverspringboot.entities.persondetails;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * JPA entity representing a person (voice actor or staff member).
 *
 * <p>Maps to the {@code person_details} table in PostgreSQL. Each row
 * represents one person who has worked on anime productions, either as a
 * voice actor or in a staff role.</p>
 *
 * <p>The primary key is {@code person_mal_id} (a natural {@link Integer} key
 * imported from the MyAnimeList CSV dataset — no {@code @GeneratedValue}
 * because the value is assigned by MyAnimeList).</p>
 *
 * <p>Nullable fields: {@code website_url}, {@code given_name},
 * {@code family_name}, {@code birthday}, {@code relevant_location}.
 * These are filtered via IS NULL / IS NOT NULL in
 * {@link PersonDetailsService}.</p>
 */
@Schema(description = "Anime staff info entity")
@Entity
@Table(name = "person_details")
public class PersonDetails {

    /** Person MAL ID — natural primary key imported from the dataset. */
    @Schema(description = "Person MyAnimeList ID (Primary Key)", example = "1")
    @Id
    @Column(name = "person_mal_id")
    private Integer personMalId;

    /** MyAnimeList profile URL. */
    @Schema(description = "MyAnimeList URL", example = "https://myanimelist.net/people/1")
    @Column(name = "url")
    private String url;

    /** Personal website URL — nullable field. */
    @Schema(description = "Personal Website URL", example = "http://www.example.com")
    @Column(name = "website_url")
    private String websiteUrl;

    /** Profile image URL. */
    @Schema(description = "Image URL", example = "https://cdn.myanimelist.net/images/people/1.jpg")
    @Column(name = "image_url")
    private String imageUrl;

    /** Full name as listed on MyAnimeList. */
    @Schema(description = "Full Name", example = "Miyazaki Hayao")
    @Column(name = "name")
    private String name;

    /** Given (first) name — nullable field. */
    @Schema(description = "Given Name", example = "Hayao")
    @Column(name = "given_name")
    private String givenName;

    /** Family (last) name — nullable field. */
    @Schema(description = "Family Name", example = "Miyazaki")
    @Column(name = "family_name")
    private String familyName;

    /** Date of birth — nullable field. */
    @Schema(description = "Birthday", example = "1941-01-05")
    @Column(name = "birthday")
    private LocalDate birthday;

    /** Number of users who have this person in their favourites list. */
    @Schema(description = "User Favorites Count", example = "30000")
    @Column(name = "favorites")
    private Integer favorites;

    /** Hometown or relevant location — nullable field. */
    @Schema(description = "Relevant Location/Hometown", example = "Tokyo, Japan")
    @Column(name = "relevant_location")
    private String relevantLocation;

    /** Required no-args constructor for JPA. */
    public PersonDetails() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param personMalId      person MAL ID
     * @param url              MyAnimeList profile URL
     * @param websiteUrl       personal website URL (nullable)
     * @param imageUrl         profile image URL
     * @param name             full name
     * @param givenName        given name (nullable)
     * @param familyName       family name (nullable)
     * @param birthday         date of birth (nullable)
     * @param favorites        favourites count
     * @param relevantLocation hometown or location (nullable)
     */
    public PersonDetails(Integer personMalId, String url, String websiteUrl, String imageUrl,
                          String name, String givenName, String familyName, LocalDate birthday,
                          Integer favorites, String relevantLocation) {
        this.personMalId      = personMalId;
        this.url              = url;
        this.websiteUrl       = websiteUrl;
        this.imageUrl         = imageUrl;
        this.name             = name;
        this.givenName        = givenName;
        this.familyName       = familyName;
        this.birthday         = birthday;
        this.favorites        = favorites;
        this.relevantLocation = relevantLocation;
    }

    /** @return person MAL ID */
    public Integer   getPersonMalId()      { return personMalId; }
    /** @param personMalId person MAL ID */
    public void      setPersonMalId(Integer personMalId) { this.personMalId = personMalId; }

    /** @return MyAnimeList profile URL */
    public String    getUrl()              { return url; }
    /** @param url MyAnimeList profile URL */
    public void      setUrl(String url)    { this.url = url; }

    /** @return personal website URL, or {@code null} if not set */
    public String    getWebsiteUrl()       { return websiteUrl; }
    /** @param websiteUrl personal website URL */
    public void      setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    /** @return profile image URL */
    public String    getImageUrl()         { return imageUrl; }
    /** @param imageUrl profile image URL */
    public void      setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /** @return full name */
    public String    getName()             { return name; }
    /** @param name full name */
    public void      setName(String name)  { this.name = name; }

    /** @return given name, or {@code null} if not set */
    public String    getGivenName()        { return givenName; }
    /** @param givenName given name */
    public void      setGivenName(String givenName) { this.givenName = givenName; }

    /** @return family name, or {@code null} if not set */
    public String    getFamilyName()       { return familyName; }
    /** @param familyName family name */
    public void      setFamilyName(String familyName) { this.familyName = familyName; }

    /** @return date of birth, or {@code null} if not set */
    public LocalDate getBirthday()         { return birthday; }
    /** @param birthday date of birth */
    public void      setBirthday(LocalDate birthday) { this.birthday = birthday; }

    /** @return favourites count */
    public Integer   getFavorites()        { return favorites; }
    /** @param favorites favourites count */
    public void      setFavorites(Integer favorites) { this.favorites = favorites; }

    /** @return hometown or location, or {@code null} if not set */
    public String    getRelevantLocation() { return relevantLocation; }
    /** @param relevantLocation hometown or location */
    public void      setRelevantLocation(String relevantLocation) {
        this.relevantLocation = relevantLocation;
    }

    @Override
    public String toString() {
        return "PersonDetails{personMalId=" + personMalId
                + ", name='" + name + "', givenName='" + givenName
                + "', familyName='" + familyName + "', birthday=" + birthday
                + ", favorites=" + favorites + '}';
    }
}
