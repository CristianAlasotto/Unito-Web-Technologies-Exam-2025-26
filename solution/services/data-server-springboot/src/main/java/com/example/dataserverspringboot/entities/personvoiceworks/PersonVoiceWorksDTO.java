package com.example.dataserverspringboot.entities.personvoiceworks;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the PersonVoiceWorks entity.
 *
 * PURPOSE:
 *   Decouples the internal JPA entity (PersonVoiceWorks.java) from the data
 *   exposed through the REST API — same pattern as DetailsDTO and ProfilesDTO.
 *   The raw PersonVoiceWorks entity NEVER leaves the service layer; only this
 *   DTO is returned to the controller and serialised to JSON.
 *
 * DESIGN:
 *   - Private constructor — prevents accidental instantiation.
 *   - Static factory method fromEntity() — the only way to build a DTO.
 *   - Getters only — DTO is read-only once built (no setters).
 */
@Schema(description = "Person voice works data transfer object")
public class PersonVoiceWorksDTO {

    @Schema(description = "Person MyAnimeList ID (Composite Key)", example = "1")
    private Integer personMalId;

    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    private Integer characterMalId;

    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    private Integer animeMalId;

    @Schema(description = "Role type (e.g., Main, Supporting)", example = "Main")
    private String role;

    @Schema(description = "Language of the voice work", example = "Japanese")
    private String language;

    // ── Private constructor — use fromEntity() ────────────────────────────────
    private PersonVoiceWorksDTO() {}

    /**
     * Static factory method: converts a PersonVoiceWorks JPA entity into a DTO.
     *
     * @param e the PersonVoiceWorks entity fetched from the database
     * @return a fully populated PersonVoiceWorksDTO ready to be serialised as JSON
     */
    public static PersonVoiceWorksDTO fromEntity(PersonVoiceWorks e) {
        PersonVoiceWorksDTO dto = new PersonVoiceWorksDTO();
        dto.personMalId    = e.getPersonMalId();
        dto.characterMalId = e.getCharacterMalId();
        dto.animeMalId     = e.getAnimeMalId();
        dto.role           = e.getRole();
        dto.language       = e.getLanguage();
        return dto;
    }

    // ── Getters (no setters — DTO is read-only once built) ───────────────────
    // @JsonProperty ensures Jackson serialises each field with the correct
    // snake_case key automatically — no manual Map conversion needed.
    @JsonProperty("person_mal_id")
    public Integer getPersonMalId()    { return personMalId; }
    @JsonProperty("character_mal_id")
    public Integer getCharacterMalId() { return characterMalId; }
    @JsonProperty("anime_mal_id")
    public Integer getAnimeMalId()     { return animeMalId; }
    public String  getRole()           { return role; }
    public String  getLanguage()       { return language; }
}
