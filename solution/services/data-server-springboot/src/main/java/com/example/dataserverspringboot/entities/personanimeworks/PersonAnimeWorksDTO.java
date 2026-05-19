package com.example.dataserverspringboot.entities.personanimeworks;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the PersonAnimeWorks entity.
 *
 * PURPOSE:
 *   Decouples the internal JPA entity (PersonAnimeWorks.java) from the data
 *   exposed through the REST API — same pattern as all other DTOs in the project.
 *   The raw PersonAnimeWorks entity NEVER leaves the service layer; only this
 *   DTO is returned to the controller and serialised to JSON.
 *
 * DESIGN:
 *   - Private constructor — prevents accidental instantiation.
 *   - Static factory method fromEntity() — the only way to build a DTO.
 *   - Getters only — DTO is read-only once built (no setters).
 *   - @JsonProperty ensures Jackson serialises camelCase fields as snake_case
 *     automatically — no manual Map conversion needed in the controller.
 */
@Schema(description = "Person anime works data transfer object")
public class PersonAnimeWorksDTO {

    @Schema(description = "Person MyAnimeList ID (Composite Key)", example = "1")
    private Integer personMalId;

    @Schema(description = "Staff position/role (Composite Key)", example = "Director")
    private String position;

    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    private Integer animeMalId;

    // ── Private constructor — use fromEntity() ────────────────────────────────
    private PersonAnimeWorksDTO() {}

    /**
     * Static factory method: converts a PersonAnimeWorks JPA entity into a DTO.
     *
     * @param e the PersonAnimeWorks entity fetched from the database
     * @return a fully populated PersonAnimeWorksDTO ready to be serialised as JSON
     */
    public static PersonAnimeWorksDTO fromEntity(PersonAnimeWorks e) {
        PersonAnimeWorksDTO dto = new PersonAnimeWorksDTO();
        dto.personMalId = e.getPersonMalId();
        dto.position    = e.getPosition();
        dto.animeMalId  = e.getAnimeMalId();
        return dto;
    }

    // ── Getters (no setters — DTO is read-only once built) ───────────────────
    @JsonProperty("person_mal_id")
    public Integer getPersonMalId() { return personMalId; }
    public String  getPosition()    { return position; }
    @JsonProperty("anime_mal_id")
    public Integer getAnimeMalId()  { return animeMalId; }
}
