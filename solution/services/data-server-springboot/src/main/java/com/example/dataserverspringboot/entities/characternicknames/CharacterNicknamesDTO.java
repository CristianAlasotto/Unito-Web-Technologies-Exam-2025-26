package com.example.dataserverspringboot.entities.characternicknames;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the CharacterNicknames entity.
 *
 * PURPOSE:
 *   Decouples the internal JPA entity (CharacterNicknames.java) from the data
 *   exposed through the REST API — same pattern as all other DTOs in the project.
 *   The raw CharacterNicknames entity NEVER leaves the service layer; only this
 *   DTO is returned to the controller and serialised to JSON.
 *
 * DESIGN:
 *   - Private constructor — prevents accidental instantiation.
 *   - Static factory method fromEntity() — the only way to build a DTO.
 *   - Getters only — DTO is read-only once built (no setters).
 *   - @JsonProperty ensures Jackson serialises camelCase fields as snake_case
 *     automatically — no manual Map conversion needed in the controller.
 */
@Schema(description = "Character nicknames data transfer object")
public class CharacterNicknamesDTO {

    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    private Integer characterMalId;

    @Schema(description = "Nickname associated with the character (Composite Key)", example = "Spike")
    private String nickname;

    // ── Private constructor — use fromEntity() ────────────────────────────────
    private CharacterNicknamesDTO() {}

    /**
     * Static factory method: converts a CharacterNicknames JPA entity into a DTO.
     *
     * @param e the CharacterNicknames entity fetched from the database
     * @return a fully populated CharacterNicknamesDTO ready to be serialised as JSON
     */
    public static CharacterNicknamesDTO fromEntity(CharacterNicknames e) {
        CharacterNicknamesDTO dto = new CharacterNicknamesDTO();
        dto.characterMalId = e.getCharacterMalId();
        dto.nickname       = e.getNickname();
        return dto;
    }

    // ── Getters (no setters — DTO is read-only once built) ───────────────────
    @JsonProperty("character_mal_id")
    public Integer getCharacterMalId() { return characterMalId; }
    public String  getNickname()       { return nickname; }
}
