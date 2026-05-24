package com.example.dataserverspringboot.entities.persondetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Data Transfer Object for the {@link PersonDetails} entity.
 *
 * <p>Decouples the internal JPA entity from the data exposed through the REST API.
 * The raw {@link PersonDetails} entity is never serialised to JSON — only this
 * DTO leaves the service layer.</p>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Private constructor — prevents accidental instantiation outside the factory.</li>
 *   <li>{@link #fromEntity(PersonDetails)} is the only way to build a DTO;
 *       construction logic is centralised in one place.</li>
 *   <li>Getters only — the DTO is read-only once constructed (no setters).</li>
 *   <li>{@code @JsonProperty} on camelCase getters tells Jackson to use snake_case
 *       JSON keys automatically, without any manual {@code Map} construction
 *       in the controller.</li>
 * </ul>
 */
@Schema(description = "Person details data transfer object")
public class PersonDetailsDTO {

    @Schema(description = "Person MyAnimeList ID (Primary Key)", example = "1")
    private Integer personMalId;

    @Schema(description = "MyAnimeList URL", example = "https://myanimelist.net/people/1")
    private String url;

    @Schema(description = "Personal website URL", example = "http://www.example.com")
    private String websiteUrl;

    @Schema(description = "Image URL", example = "https://cdn.myanimelist.net/images/people/1.jpg")
    private String imageUrl;

    @Schema(description = "Full name", example = "Miyazaki Hayao")
    private String name;

    @Schema(description = "Given name", example = "Hayao")
    private String givenName;

    @Schema(description = "Family name", example = "Miyazaki")
    private String familyName;

    @Schema(description = "Birthday", example = "1941-01-05")
    private LocalDate birthday;

    @Schema(description = "User favorites count", example = "30000")
    private Integer favorites;

    @Schema(description = "Relevant location / hometown", example = "Tokyo, Japan")
    private String relevantLocation;

    /** Private constructor — use {@link #fromEntity(PersonDetails)}. */
    private PersonDetailsDTO() {}

    /**
     * Static factory method: converts a {@link PersonDetails} JPA entity into a DTO.
     *
     * <p>This is the only way to build a DTO — keeps construction logic in one
     * place and prevents partially initialised instances.</p>
     *
     * @param p the {@link PersonDetails} entity fetched from the database
     * @return a fully populated {@link PersonDetailsDTO} ready to be serialised as JSON
     */
    public static PersonDetailsDTO fromEntity(PersonDetails p) {
        PersonDetailsDTO dto = new PersonDetailsDTO();
        dto.personMalId      = p.getPersonMalId();
        dto.url              = p.getUrl();
        dto.websiteUrl       = p.getWebsiteUrl();
        dto.imageUrl         = p.getImageUrl();
        dto.name             = p.getName();
        dto.givenName        = p.getGivenName();
        dto.familyName       = p.getFamilyName();
        dto.birthday         = p.getBirthday();
        dto.favorites        = p.getFavorites();
        dto.relevantLocation = p.getRelevantLocation();
        return dto;
    }

    /**
     * Returns the person MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to {@code "person_mal_id"}.
     *
     * @return person MAL ID
     */
    @JsonProperty("person_mal_id")
    public Integer   getPersonMalId()      { return personMalId; }

    /** @return MyAnimeList profile URL */
    public String    getUrl()              { return url; }

    /**
     * Returns the personal website URL.
     * {@code @JsonProperty} maps this getter to {@code "website_url"}.
     *
     * @return personal website URL, or {@code null} if not set
     */
    @JsonProperty("website_url")
    public String    getWebsiteUrl()       { return websiteUrl; }

    /**
     * Returns the profile image URL.
     * {@code @JsonProperty} maps this getter to {@code "image_url"}.
     *
     * @return profile image URL
     */
    @JsonProperty("image_url")
    public String    getImageUrl()         { return imageUrl; }

    /** @return full name */
    public String    getName()             { return name; }

    /**
     * Returns the given name.
     * {@code @JsonProperty} maps this getter to {@code "given_name"}.
     *
     * @return given name, or {@code null} if not set
     */
    @JsonProperty("given_name")
    public String    getGivenName()        { return givenName; }

    /**
     * Returns the family name.
     * {@code @JsonProperty} maps this getter to {@code "family_name"}.
     *
     * @return family name, or {@code null} if not set
     */
    @JsonProperty("family_name")
    public String    getFamilyName()       { return familyName; }

    /** @return date of birth, or {@code null} if not set */
    public LocalDate getBirthday()         { return birthday; }

    /** @return favourites count */
    public Integer   getFavorites()        { return favorites; }

    /**
     * Returns the hometown or relevant location.
     * {@code @JsonProperty} maps this getter to {@code "relevant_location"}.
     *
     * @return hometown or location, or {@code null} if not set
     */
    @JsonProperty("relevant_location")
    public String    getRelevantLocation() { return relevantLocation; }
}
