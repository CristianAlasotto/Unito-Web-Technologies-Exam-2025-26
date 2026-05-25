package com.example.dataserverspringboot.entities.personvoiceworks;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the {@link PersonVoiceWorks} entity.
 *
 * <p>Decouples the internal JPA entity from the data exposed through the REST API.
 * The raw {@link PersonVoiceWorks} entity is never serialised to JSON — only this
 * DTO leaves the service layer.</p>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Private constructor — prevents accidental instantiation outside the factory.</li>
 *   <li>{@link #fromEntity(PersonVoiceWorks)} is the only way to build a DTO;
 *       construction logic is centralised in one place.</li>
 *   <li>Getters only — the DTO is read-only once constructed (no setters).</li>
 *   <li>{@code @JsonProperty} on camelCase getters tells Jackson to use snake_case
 *       JSON keys automatically, without any manual {@code Map} construction
 *       in the controller.</li>
 * </ul>
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

    /** Private constructor — use {@link #fromEntity(PersonVoiceWorks)}. */
    private PersonVoiceWorksDTO() {}

    /**
     * Static factory method: converts a {@link PersonVoiceWorks} JPA entity
     * into a {@link PersonVoiceWorksDTO}.
     *
     * <p>This is the only way to build a DTO — keeps construction logic in
     * one place and prevents partially initialised instances.</p>
     *
     * @param e the {@link PersonVoiceWorks} entity fetched from the database
     * @return a fully populated {@link PersonVoiceWorksDTO} ready to be serialised as JSON
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

    /**
     * Returns the person MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to the snake_case
     * JSON key {@code "person_mal_id"}.
     *
     * @return person MAL ID
     */
    @JsonProperty("person_mal_id")
    public Integer getPersonMalId()    { return personMalId; }

    /**
     * Returns the character MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to the snake_case
     * JSON key {@code "character_mal_id"}.
     *
     * @return character MAL ID
     */
    @JsonProperty("character_mal_id")
    public Integer getCharacterMalId() { return characterMalId; }

    /**
     * Returns the anime MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to the snake_case
     * JSON key {@code "anime_mal_id"}.
     *
     * @return anime MAL ID
     */
    @JsonProperty("anime_mal_id")
    public Integer getAnimeMalId()     { return animeMalId; }

    /**
     * Returns the role type.
     * No {@code @JsonProperty} needed — {@code "role"} is already snake_case.
     *
     * @return role type (e.g. Main, Supporting), or {@code null} if not set
     */
    public String getRole()            { return role; }

    /**
     * Returns the dubbing language.
     * No {@code @JsonProperty} needed — {@code "language"} is already snake_case.
     *
     * @return dubbing language (e.g. Japanese), or {@code null} if not set
     */
    public String getLanguage()        { return language; }
}
