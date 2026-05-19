package com.example.dataserverspringboot.entities.persondetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Data Transfer Object for the PersonDetails entity.
 *
 * PURPOSE:
 *   Decouples the internal JPA entity (PersonDetails.java) from the data exposed
 *   through the REST API — same pattern as DetailsDTO, ProfilesDTO, PersonVoiceWorksDTO.
 *   The raw PersonDetails entity NEVER leaves the service layer; only this DTO is
 *   returned to the controller and serialised to JSON.
 *
 * DESIGN:
 *   - Private constructor — prevents accidental instantiation.
 *   - Static factory method fromEntity() — the only way to build a DTO.
 *   - Getters only — DTO is read-only once built (no setters).
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

    // ── Private constructor — use fromEntity() ────────────────────────────────
    private PersonDetailsDTO() {}

    /**
     * Static factory method: converts a PersonDetails JPA entity into a DTO.
     *
     * @param p the PersonDetails entity fetched from the database
     * @return a fully populated PersonDetailsDTO ready to be serialised as JSON
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

    // ── Getters (no setters — DTO is read-only once built) ───────────────────
    // @JsonProperty ensures Jackson serialises each field with the correct
    // snake_case key automatically — no manual Map conversion needed.
    @JsonProperty("person_mal_id")
    public Integer   getPersonMalId()      { return personMalId; }
    public String    getUrl()              { return url; }
    @JsonProperty("website_url")
    public String    getWebsiteUrl()       { return websiteUrl; }
    @JsonProperty("image_url")
    public String    getImageUrl()         { return imageUrl; }
    public String    getName()             { return name; }
    @JsonProperty("given_name")
    public String    getGivenName()        { return givenName; }
    @JsonProperty("family_name")
    public String    getFamilyName()       { return familyName; }
    public LocalDate getBirthday()         { return birthday; }
    public Integer   getFavorites()        { return favorites; }
    @JsonProperty("relevant_location")
    public String    getRelevantLocation() { return relevantLocation; }
}
