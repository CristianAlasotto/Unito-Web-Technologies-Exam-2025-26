package com.example.dataserverspringboot.entities.profiles;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Data Transfer Object for the {@link Profiles} entity.
 *
 * <p>Decouples the internal JPA entity from the data exposed through the REST API.
 * The raw {@link Profiles} entity is never serialised to JSON — only this DTO
 * leaves the service layer.</p>
 *
 * <p>Design principles:</p>
 * <ul>
 *   <li>Private constructor — prevents accidental instantiation outside the factory.</li>
 *   <li>{@link #fromEntity(Profiles)} is the only way to build a DTO; construction
 *       logic is centralised in one place.</li>
 *   <li>Getters only — the DTO is read-only once constructed (no setters).</li>
 *   <li>{@code @JsonProperty} annotations on camelCase getters tell Jackson to
 *       use snake_case JSON keys automatically, without any manual {@code Map}
 *       construction in the controller.</li>
 * </ul>
 */
@Schema(description = "User profile data transfer object")
public class ProfilesDTO {

    @Schema(description = "User unique username (Primary Key)", example = "Xinil")
    private String username;

    @Schema(description = "User gender", example = "Male")
    private String gender;

    @Schema(description = "User birthday", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "User location", example = "California")
    private String location;

    @Schema(description = "Date user joined the site", example = "2007-05-23")
    private LocalDate joined;

    @Schema(description = "Number of anime currently watching", example = "5")
    private Integer watching;

    @Schema(description = "Number of anime completed", example = "100")
    private Integer completed;

    @Schema(description = "Number of anime on hold", example = "2")
    private Integer onHold;

    @Schema(description = "Number of anime dropped", example = "1")
    private Integer dropped;

    @Schema(description = "Number of anime planned to watch", example = "50")
    private Integer planToWatch;

    /** Private constructor — use {@link #fromEntity(Profiles)}. */
    private ProfilesDTO() {}

    /**
     * Static factory method: converts a {@link Profiles} JPA entity into a
     * {@link ProfilesDTO}.
     *
     * <p>This is the only way to build a DTO — keeps construction logic in one
     * place and prevents partially initialised instances.</p>
     *
     * @param p the {@link Profiles} entity fetched from the database
     * @return a fully populated {@link ProfilesDTO} ready to be serialised as JSON
     */
    public static ProfilesDTO fromEntity(Profiles p) {
        ProfilesDTO dto = new ProfilesDTO();
        dto.username    = p.getUsername();
        dto.gender      = p.getGender();
        dto.birthday    = p.getBirthday();
        dto.location    = p.getLocation();
        dto.joined      = p.getJoined();
        dto.watching    = p.getWatching();
        dto.completed   = p.getCompleted();
        dto.onHold      = p.getOnHold();
        dto.dropped     = p.getDropped();
        dto.planToWatch = p.getPlanToWatch();
        return dto;
    }

    /** @return unique username */
    public String    getUsername()    { return username; }

    /** @return user gender, or {@code null} if not set */
    public String    getGender()      { return gender; }

    /** @return user date of birth, or {@code null} if not set */
    public LocalDate getBirthday()    { return birthday; }

    /** @return user location, or {@code null} if not set */
    public String    getLocation()    { return location; }

    /** @return registration date */
    public LocalDate getJoined()      { return joined; }

    /** @return anime currently watching count */
    public Integer   getWatching()    { return watching; }

    /** @return anime completed count */
    public Integer   getCompleted()   { return completed; }

    /**
     * Returns the number of anime the user has put on hold.
     * {@code @JsonProperty} maps the camelCase getter to the snake_case
     * JSON key {@code "on_hold"}.
     *
     * @return anime on-hold count
     */
    @JsonProperty("on_hold")
    public Integer   getOnHold()      { return onHold; }

    /** @return anime dropped count */
    public Integer   getDropped()     { return dropped; }

    /**
     * Returns the number of anime the user plans to watch.
     * {@code @JsonProperty} maps the camelCase getter to the snake_case
     * JSON key {@code "plan_to_watch"}.
     *
     * @return anime planned-to-watch count
     */
    @JsonProperty("plan_to_watch")
    public Integer   getPlanToWatch() { return planToWatch; }
}
