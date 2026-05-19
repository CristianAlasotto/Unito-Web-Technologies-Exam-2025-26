package com.example.dataserverspringboot.entities.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the Characters entity.
 *
 * PURPOSE:
 *   Decouples the internal JPA entity (Characters.java) from the data exposed
 *   through the REST API — same pattern as all other DTOs in the project.
 *   The raw Characters entity NEVER leaves the service layer; only this DTO is
 *   returned to the controller and serialised to JSON.
 *
 * DESIGN:
 *   - Private constructor — prevents accidental instantiation.
 *   - Static factory method fromEntity() — the only way to build a DTO.
 *   - Getters only — DTO is read-only once built (no setters).
 *   - @JsonProperty ensures Jackson serialises camelCase fields as snake_case
 *     automatically — no manual Map conversion needed in the controller.
 */
@Schema(description = "Anime character data transfer object")
public class CharactersDTO {

    @Schema(description = "Character MyAnimeList ID (primary key)", example = "1")
    private Integer characterMalId;

    @Schema(description = "MyAnimeList character URL", example = "https://myanimelist.net/character/1")
    private String url;

    @Schema(description = "Character name", example = "Spike Spiegel")
    private String name;

    @Schema(description = "Character name in Japanese/Kanji", example = "スパイク・スピーゲル")
    private String nameKanji;

    @Schema(description = "Character image URL", example = "https://cdn.myanimelist.net/images/characters/1/1.jpg")
    private String image;

    @Schema(description = "Number of users who favorited this character", example = "50000")
    private Integer favorites;

    @Schema(description = "Character biography/description")
    private String about;

    // ── Private constructor — use fromEntity() ────────────────────────────────
    private CharactersDTO() {}

    /**
     * Static factory method: converts a Characters JPA entity into a DTO.
     *
     * @param c the Characters entity fetched from the database
     * @return a fully populated CharactersDTO ready to be serialised as JSON
     */
    public static CharactersDTO fromEntity(Characters c) {
        CharactersDTO dto = new CharactersDTO();
        dto.characterMalId = c.getCharacterMalId();
        dto.url            = c.getUrl();
        dto.name           = c.getName();
        dto.nameKanji      = c.getNameKanji();
        dto.image          = c.getImage();
        dto.favorites      = c.getFavorites();
        dto.about          = c.getAbout();
        return dto;
    }

    // ── Getters (no setters — DTO is read-only once built) ───────────────────
    @JsonProperty("character_mal_id")
    public Integer getCharacterMalId() { return characterMalId; }
    public String  getUrl()            { return url; }
    public String  getName()           { return name; }
    @JsonProperty("name_kanji")
    public String  getNameKanji()      { return nameKanji; }
    public String  getImage()          { return image; }
    public Integer getFavorites()      { return favorites; }
    public String  getAbout()          { return about; }
}
