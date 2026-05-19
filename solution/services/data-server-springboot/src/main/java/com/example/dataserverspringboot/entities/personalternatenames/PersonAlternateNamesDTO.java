package com.example.dataserverspringboot.entities.personalternatenames;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the PersonAlternateNames entity.
 *
 * PURPOSE:
 *   Decouples the internal JPA entity (PersonAlternateNames.java) from the data
 *   exposed through the REST API — same pattern as all other DTOs in the project.
 *   The raw PersonAlternateNames entity NEVER leaves the service layer; only this
 *   DTO is returned to the controller and serialised to JSON.
 *
 * DESIGN:
 *   - Private constructor — prevents accidental instantiation.
 *   - Static factory method fromEntity() — the only way to build a DTO.
 *   - Getters only — DTO is read-only once built (no setters).
 *   - @JsonProperty ensures Jackson serialises camelCase fields as snake_case
 *     automatically — no manual Map conversion needed in the controller.
 */
@Schema(description = "Person alternate names data transfer object")
public class PersonAlternateNamesDTO {

    @Schema(description = "Person/Staff MyAnimeList ID (Composite Key)", example = "1")
    private Integer personMalId;

    @Schema(description = "Alternate name (Composite Key)", example = "Miyazaki Hayao")
    private String altName;

    // ── Private constructor — use fromEntity() ────────────────────────────────
    private PersonAlternateNamesDTO() {}

    /**
     * Static factory method: converts a PersonAlternateNames JPA entity into a DTO.
     *
     * @param e the PersonAlternateNames entity fetched from the database
     * @return a fully populated PersonAlternateNamesDTO ready to be serialised as JSON
     */
    public static PersonAlternateNamesDTO fromEntity(PersonAlternateNames e) {
        PersonAlternateNamesDTO dto = new PersonAlternateNamesDTO();
        dto.personMalId = e.getPersonMalId();
        dto.altName     = e.getAltName();
        return dto;
    }

    // ── Getters (no setters — DTO is read-only once built) ───────────────────
    @JsonProperty("person_mal_id")
    public Integer getPersonMalId() { return personMalId; }
    @JsonProperty("alt_name")
    public String  getAltName()     { return altName; }
}
