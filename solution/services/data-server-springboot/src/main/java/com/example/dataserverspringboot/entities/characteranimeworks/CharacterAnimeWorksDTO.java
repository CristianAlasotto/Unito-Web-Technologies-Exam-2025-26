package com.example.dataserverspringboot.entities.characteranimeworks;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the CharacterAnimeWorks entity.
 *
 * PURPOSE:
 *   Decouples the internal JPA entity (CharacterAnimeWorks.java) from the data
 *   exposed through the REST API — same pattern as all other DTOs in the project.
 *   The raw CharacterAnimeWorks entity NEVER leaves the service layer; only this
 *   DTO is returned to the controller and serialised to JSON.
 *
 * DESIGN:
 *   - Private constructor — prevents accidental instantiation.
 *   - Static factory method fromEntity() — the only way to build a DTO.
 *   - Getters only — DTO is read-only once built (no setters).
 *   - @JsonProperty ensures Jackson serialises camelCase fields as snake_case
 *     automatically — no manual Map conversion needed in the controller.
 */
@Schema(description = "Character anime works data transfer object")
public class CharacterAnimeWorksDTO {

    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    private Integer characterMalId;

    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    private Integer animeMalId;

    @Schema(description = "Character name as credited in this specific anime", example = "Spike Spiegel")
    private String characterName;

    @Schema(description = "Role of the character in the anime (e.g., Main, Supporting)", example = "Main")
    private String role;

    // ── Private constructor — use fromEntity() ────────────────────────────────
    private CharacterAnimeWorksDTO() {}

    /**
     * Static factory method: converts a CharacterAnimeWorks JPA entity into a DTO.
     *
     * @param e the CharacterAnimeWorks entity fetched from the database
     * @return a fully populated CharacterAnimeWorksDTO ready to be serialised as JSON
     */
    public static CharacterAnimeWorksDTO fromEntity(CharacterAnimeWorks e) {
        CharacterAnimeWorksDTO dto = new CharacterAnimeWorksDTO();
        dto.characterMalId = e.getCharacterMalId();
        dto.animeMalId     = e.getAnimeMalId();
        dto.characterName  = e.getCharacterName();
        dto.role           = e.getRole();
        return dto;
    }

    // ── Getters (no setters — DTO is read-only once built) ───────────────────
    @JsonProperty("character_mal_id")
    public Integer getCharacterMalId() { return characterMalId; }
    @JsonProperty("anime_mal_id")
    public Integer getAnimeMalId()     { return animeMalId; }
    @JsonProperty("character_name")
    public String  getCharacterName()  { return characterName; }
    public String  getRole()           { return role; }
}
