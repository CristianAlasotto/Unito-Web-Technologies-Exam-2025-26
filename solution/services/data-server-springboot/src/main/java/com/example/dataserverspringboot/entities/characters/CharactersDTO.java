package com.example.dataserverspringboot.entities.characters;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the {@link Characters} entity.
 *
 * <p>Decouples the internal JPA entity from the data exposed through the REST API.
 * The raw {@link Characters} entity is never serialised to JSON — only this DTO
 * leaves the service layer.</p>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Private constructor — prevents accidental instantiation outside the factory.</li>
 *   <li>{@link #fromEntity(Characters)} is the only way to build a DTO;
 *       construction logic is centralised in one place.</li>
 *   <li>Getters only — the DTO is read-only once constructed (no setters).</li>
 *   <li>{@code @JsonProperty} on camelCase getters tells Jackson to use snake_case
 *       JSON keys automatically. Only two fields need annotations:
 *       {@code characterMalId} and {@code nameKanji}.</li>
 * </ul>
 */
@Schema(description = "Anime character data transfer object")
public class CharactersDTO {

    @Schema(description = "Character MyAnimeList ID (primary key)", example = "1")
    private Integer characterMalId;

    @Schema(description = "MyAnimeList character URL",
            example = "https://myanimelist.net/character/1")
    private String url;

    @Schema(description = "Character name", example = "Spike Spiegel")
    private String name;

    @Schema(description = "Character name in Japanese/Kanji",
            example = "スパイク・スピーゲル")
    private String nameKanji;

    @Schema(description = "Character image URL",
            example = "https://cdn.myanimelist.net/images/characters/1/1.jpg")
    private String image;

    @Schema(description = "Number of users who favorited this character", example = "50000")
    private Integer favorites;

    @Schema(description = "Character biography/description")
    private String about;

    /** Private constructor — use {@link #fromEntity(Characters)}. */
    private CharactersDTO() {}

    /**
     * Static factory method: converts a {@link Characters} JPA entity into a
     * {@link CharactersDTO}.
     *
     * <p>This is the only way to build a DTO — keeps construction logic in one
     * place and prevents partially initialised instances.</p>
     *
     * @param c the {@link Characters} entity fetched from the database
     * @return a fully populated {@link CharactersDTO} ready to be serialised as JSON
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

    /**
     * Returns the character MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to {@code "character_mal_id"}.
     *
     * @return character MAL ID
     */
    @JsonProperty("character_mal_id")
    public Integer getCharacterMalId() { return characterMalId; }

    /** @return MAL character profile URL */
    public String getUrl()             { return url; }

    /** @return character name in romaji */
    public String getName()            { return name; }

    /**
     * Returns the character name in kanji/kana.
     * {@code @JsonProperty} maps this camelCase getter to {@code "name_kanji"}.
     *
     * @return character name in Japanese, or {@code null} if not set
     */
    @JsonProperty("name_kanji")
    public String getNameKanji()       { return nameKanji; }

    /** @return profile image URL, or {@code null} if not set */
    public String getImage()           { return image; }

    /** @return favourites count, or {@code null} if not set */
    public Integer getFavorites()      { return favorites; }

    /** @return biography, or {@code null} if not set */
    public String getAbout()           { return about; }
}
