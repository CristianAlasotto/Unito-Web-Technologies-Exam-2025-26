package com.example.dataserverspringboot.entities.characternicknames;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the {@link CharacterNicknames} entity.
 *
 * <p>Decouples the internal JPA entity from the data exposed through the REST API.
 * The raw {@link CharacterNicknames} entity is never serialised to JSON — only
 * this DTO leaves the service layer.</p>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Private constructor — prevents accidental instantiation outside the factory.</li>
 *   <li>{@link #fromEntity(CharacterNicknames)} is the only way to build a DTO;
 *       construction logic is centralised in one place.</li>
 *   <li>Getters only — the DTO is read-only once constructed (no setters).</li>
 *   <li>{@code @JsonProperty} is needed only on {@code getCharacterMalId()} because
 *       the Java name {@code characterMalId} differs from the desired JSON key
 *       {@code "character_mal_id"}. The field {@code nickname} needs no annotation
 *       as its Java name is already snake_case-compatible.</li>
 * </ul>
 */
@Schema(description = "Character nicknames data transfer object")
public class CharacterNicknamesDTO {

    @Schema(description = "Character MyAnimeList ID (Composite Key)", example = "1")
    private Integer characterMalId;

    @Schema(description = "Nickname associated with the character (Composite Key)", example = "Spike")
    private String nickname;

    /** Private constructor — use {@link #fromEntity(CharacterNicknames)}. */
    private CharacterNicknamesDTO() {}

    /**
     * Static factory method: converts a {@link CharacterNicknames} JPA entity
     * into a {@link CharacterNicknamesDTO}.
     *
     * <p>This is the only way to build a DTO — keeps construction logic in one
     * place and prevents partially initialised instances.</p>
     *
     * @param e the {@link CharacterNicknames} entity fetched from the database
     * @return a fully populated {@link CharacterNicknamesDTO} ready to be serialised as JSON
     */
    public static CharacterNicknamesDTO fromEntity(CharacterNicknames e) {
        CharacterNicknamesDTO dto = new CharacterNicknamesDTO();
        dto.characterMalId = e.getCharacterMalId();
        dto.nickname       = e.getNickname();
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
     * Returns the nickname.
     * No {@code @JsonProperty} needed — {@code "nickname"} is already snake_case.
     *
     * @return nickname string
     */
    public String getNickname() { return nickname; }
}
