package com.example.dataserverspringboot.entities.profiles;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Data Transfer Object for the Profiles entity.
 *
 * PURPOSE:
 *   Decouples the internal JPA entity (Profiles.java) from the data exposed
 *   through the REST API — the same pattern used by DetailsDTO for the Details entity.
 *   The raw Profiles entity NEVER leaves the service layer; only ProfilesDTO is
 *   returned to the controller and serialised to JSON.
 *
 * DESIGN:
 *   - Private constructor — prevents accidental instantiation.
 *   - Static factory method fromEntity(Profiles p) — the only way to build a DTO.
 *   - Getters only — DTO is read-only once built (no setters).
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

    // ── Private constructor — use fromEntity() ────────────────────────────────
    private ProfilesDTO() {}

    /**
     * Static factory method: converts a Profiles JPA entity into a ProfilesDTO.
     * This is the only way to build a DTO — keeps construction logic in one place.
     *
     * @param p the Profiles entity fetched from the database
     * @return a fully populated ProfilesDTO ready to be serialised as JSON
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

    // ── Getters (no setters — DTO is read-only once built) ───────────────────
    // @JsonProperty ensures Jackson serialises each field with the correct
    // snake_case key automatically — no manual Map conversion needed.
    public String    getUsername()    { return username; }
    public String    getGender()      { return gender; }
    public LocalDate getBirthday()    { return birthday; }
    public String    getLocation()    { return location; }
    public LocalDate getJoined()      { return joined; }
    public Integer   getWatching()    { return watching; }
    public Integer   getCompleted()   { return completed; }
    @JsonProperty("on_hold")
    public Integer   getOnHold()      { return onHold; }
    public Integer   getDropped()     { return dropped; }
    @JsonProperty("plan_to_watch")
    public Integer   getPlanToWatch() { return planToWatch; }
}
