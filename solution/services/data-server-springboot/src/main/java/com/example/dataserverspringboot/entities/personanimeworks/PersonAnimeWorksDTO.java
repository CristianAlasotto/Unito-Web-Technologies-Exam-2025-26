package com.example.dataserverspringboot.entities.personanimeworks;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for the {@link PersonAnimeWorks} entity.
 *
 * <p>Decouples the internal JPA entity from the data exposed through the REST API.
 * The raw {@link PersonAnimeWorks} entity is never serialised to JSON — only this
 * DTO leaves the service layer.</p>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Private constructor — prevents accidental instantiation outside the factory.</li>
 *   <li>{@link #fromEntity(PersonAnimeWorks)} is the only way to build a DTO;
 *       construction logic is centralised in one place.</li>
 *   <li>Getters only — the DTO is read-only once constructed (no setters).</li>
 *   <li>{@code @JsonProperty} on camelCase getters tells Jackson to use snake_case
 *       JSON keys automatically. The {@code position} field needs no annotation
 *       because its Java name is already identical to the desired JSON key.</li>
 * </ul>
 */
@Schema(description = "Person anime works data transfer object")
public class PersonAnimeWorksDTO {

    @Schema(description = "Person MyAnimeList ID (Composite Key)", example = "1")
    private Integer personMalId;

    @Schema(description = "Staff position/role (Composite Key)", example = "Director")
    private String position;

    @Schema(description = "Anime MyAnimeList ID (Composite Key)", example = "1")
    private Integer animeMalId;

    /** Private constructor — use {@link #fromEntity(PersonAnimeWorks)}. */
    private PersonAnimeWorksDTO() {}

    /**
     * Static factory method: converts a {@link PersonAnimeWorks} JPA entity
     * into a {@link PersonAnimeWorksDTO}.
     *
     * <p>This is the only way to build a DTO — keeps construction logic in
     * one place and prevents partially initialised instances.</p>
     *
     * @param e the {@link PersonAnimeWorks} entity fetched from the database
     * @return a fully populated {@link PersonAnimeWorksDTO} ready to be serialised as JSON
     */
    public static PersonAnimeWorksDTO fromEntity(PersonAnimeWorks e) {
        PersonAnimeWorksDTO dto = new PersonAnimeWorksDTO();
        dto.personMalId = e.getPersonMalId();
        dto.position    = e.getPosition();
        dto.animeMalId  = e.getAnimeMalId();
        return dto;
    }

    /**
     * Returns the person MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to {@code "person_mal_id"}.
     *
     * @return person MAL ID
     */
    @JsonProperty("person_mal_id")
    public Integer getPersonMalId() { return personMalId; }

    /**
     * Returns the staff production role.
     * No {@code @JsonProperty} needed — {@code "position"} is already snake_case.
     *
     * @return staff production role, e.g. Director
     */
    public String getPosition() { return position; }

    /**
     * Returns the anime MAL ID.
     * {@code @JsonProperty} maps this camelCase getter to {@code "anime_mal_id"}.
     *
     * @return anime MAL ID
     */
    @JsonProperty("anime_mal_id")
    public Integer getAnimeMalId() { return animeMalId; }
}
