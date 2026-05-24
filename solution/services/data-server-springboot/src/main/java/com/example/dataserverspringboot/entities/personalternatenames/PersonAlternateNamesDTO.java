package com.example.dataserverspringboot.entities.personalternatenames;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the {@link PersonAlternateNames} entity.
 *
 * <p>Decouples the internal JPA entity from the data exposed through the REST API.
 * The raw {@link PersonAlternateNames} entity is never serialised to JSON — only
 * this DTO leaves the service layer.</p>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Private constructor — prevents accidental instantiation outside the factory.</li>
 *   <li>{@link #fromEntity(PersonAlternateNames)} is the only way to build a DTO;
 *       construction logic is centralised in one place.</li>
 *   <li>Getters only — the DTO is read-only once constructed (no setters).</li>
 *   <li>{@code @JsonProperty} on both getters tells Jackson to use snake_case JSON
 *       keys ({@code "person_mal_id"} and {@code "alt_name"}) automatically, without
 *       any manual {@code Map} construction in the controller.</li>
 * </ul>
 */
@Schema(description = "Person alternate names data transfer object")
public class PersonAlternateNamesDTO {

    @Schema(description = "Person/Staff MyAnimeList ID (Composite Key)", example = "1")
    private Integer personMalId;

    @Schema(description = "Alternate name (Composite Key)", example = "Miyazaki Hayao")
    private String altName;

    /** Private constructor — use {@link #fromEntity(PersonAlternateNames)}. */
    private PersonAlternateNamesDTO() {}

    /**
     * Static factory method: converts a {@link PersonAlternateNames} JPA entity
     * into a {@link PersonAlternateNamesDTO}.
     *
     * <p>This is the only way to build a DTO — keeps construction logic in one
     * place and prevents partially initialised instances.</p>
     *
     * @param e the {@link PersonAlternateNames} entity fetched from the database
     * @return a fully populated {@link PersonAlternateNamesDTO} ready to be serialised as JSON
     */
    public static PersonAlternateNamesDTO fromEntity(PersonAlternateNames e) {
        PersonAlternateNamesDTO dto = new PersonAlternateNamesDTO();
        dto.personMalId = e.getPersonMalId();
        dto.altName     = e.getAltName();
        return dto;
    }

    /**
     * Returns the person MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to {@code "person_mal_id"}.
     *
     * @return person MAL ID
     */
    @JsonProperty("person_mal_id")
    public Integer getPersonMalId() { return personMalId; }

    /**
     * Returns the alternate name.
     * {@code @JsonProperty} maps this camelCase getter to {@code "alt_name"}.
     *
     * @return alternate name
     */
    @JsonProperty("alt_name")
    public String getAltName() { return altName; }
}
