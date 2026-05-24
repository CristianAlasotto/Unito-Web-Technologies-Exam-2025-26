package com.example.dataserverspringboot.entities.characteranimeworks;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the {@link CharacterAnimeWorks} entity.
 *
 * <p>Decouples the internal JPA entity from the data exposed through the REST API.
 * The raw {@link CharacterAnimeWorks} entity is never serialised to JSON — only
 * this DTO leaves the service layer.</p>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Private constructor — prevents accidental instantiation outside the factory.</li>
 *   <li>{@link #fromEntity(CharacterAnimeWorks)} is the only way to build a DTO;
 *       construction logic is centralised in one place.</li>
 *   <li>Getters only — the DTO is read-only once constructed (no setters).</li>
 *   <li>{@code @JsonProperty} is needed on three of the four getters:
 *       {@code characterMalId}, {@code animeMalId}, and {@code characterName}
 *       all have camelCase Java names that differ from their snake_case JSON keys.
 *       {@code role} needs no annotation as its name is already snake_case.</li>
 * </ul>
 */
@Schema(description = "Character anime works data transfer object")
public class CharacterAnimeWorksDTO {

    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    private Integer characterMalId;

    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    private Integer animeMalId;

    @Schema(description = "Character name as credited in this specific anime",
            example = "Spike Spiegel")
    private String characterName;

    @Schema(description = "Role of the character in the anime (e.g., Main, Supporting)",
            example = "Main")
    private String role;

    /** Private constructor — use {@link #fromEntity(CharacterAnimeWorks)}. */
    private CharacterAnimeWorksDTO() {}

    /**
     * Static factory method: converts a {@link CharacterAnimeWorks} JPA entity
     * into a {@link CharacterAnimeWorksDTO}.
     *
     * <p>This is the only way to build a DTO — keeps construction logic in one
     * place and prevents partially initialised instances.</p>
     *
     * @param e the {@link CharacterAnimeWorks} entity fetched from the database
     * @return a fully populated {@link CharacterAnimeWorksDTO} ready to be serialised as JSON
     */
    public static CharacterAnimeWorksDTO fromEntity(CharacterAnimeWorks e) {
        CharacterAnimeWorksDTO dto = new CharacterAnimeWorksDTO();
        dto.characterMalId = e.getCharacterMalId();
        dto.animeMalId     = e.getAnimeMalId();
        dto.characterName  = e.getCharacterName();
        dto.role           = e.getRole();
        return dto;
    }

    /**
     * Returns the character MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to {@code "character_mal_id"}.
     *
     * @return character MAL ID
     */
    @JsonProperty("character_mal_id")
    public Integer getCharacterMalId() { return characterMalId; }

    /**
     * Returns the anime MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to {@code "anime_mal_id"}.
     *
     * @return anime MAL ID
     */
    @JsonProperty("anime_mal_id")
    public Integer getAnimeMalId()     { return animeMalId; }

    /**
     * Returns the character name as credited in this anime.
     * {@code @JsonProperty} maps this camelCase getter to {@code "character_name"}.
     *
     * @return character name as credited, or {@code null} if not set
     */
    @JsonProperty("character_name")
    public String getCharacterName()   { return characterName; }

    /**
     * Returns the character role.
     * No {@code @JsonProperty} needed — {@code "role"} is already snake_case.
     *
     * @return role (e.g. Main, Supporting), or {@code null} if not set
     */
    public String getRole()            { return role; }
}
