package com.example.dataserverspringboot.entities.profiles;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * JPA entity representing a user profile.
 *
 * <p>Maps to the {@code profiles} table in PostgreSQL. Each row represents
 * one registered user of the MyAnimeList platform, storing their demographic
 * data and watching statistics.</p>
 *
 * <p>The primary key is {@code username} (a natural {@link String} key —
 * no {@code @GeneratedValue} because the username is chosen by the user
 * and imported from the CSV dataset).</p>
 *
 * <p>Fields {@code gender}, {@code birthday}, and {@code location} are
 * nullable and are filtered via IS NULL / IS NOT NULL in
 * {@link ProfilesService}.</p>
 */
@Schema(description = "Website user profile entity")
@Entity
@Table(name = "profiles")
public class Profiles {

    /** Unique username — natural primary key, imported from the CSV dataset. */
    @Schema(description = "User unique username (Primary Key)", example = "Xinil")
    @Id
    @Column(name = "username")
    private String username;

    /** User gender — nullable field. */
    @Schema(description = "User gender", example = "Male")
    @Column(name = "gender")
    private String gender;

    /** User date of birth — nullable field. */
    @Schema(description = "User birthday", example = "1990-01-01")
    @Column(name = "birthday")
    private LocalDate birthday;

    /** User location string — nullable field. */
    @Schema(description = "User location", example = "California")
    @Column(name = "location")
    private String location;

    /** Date the user registered on the platform. */
    @Schema(description = "Date user joined the site", example = "2007-05-23")
    @Column(name = "joined")
    private LocalDate joined;

    /** Number of anime the user is currently watching. */
    @Schema(description = "Number of anime currently watching", example = "5")
    @Column(name = "watching")
    private Integer watching;

    /** Number of anime the user has completed. */
    @Schema(description = "Number of anime completed", example = "100")
    @Column(name = "completed")
    private Integer completed;

    /** Number of anime the user has put on hold. */
    @Schema(description = "Number of anime on hold", example = "2")
    @Column(name = "on_hold")
    private Integer onHold;

    /** Number of anime the user has dropped. */
    @Schema(description = "Number of anime dropped", example = "1")
    @Column(name = "dropped")
    private Integer dropped;

    /** Number of anime the user plans to watch. */
    @Schema(description = "Number of anime planned to watch", example = "50")
    @Column(name = "plan_to_watch")
    private Integer planToWatch;

    /** Required no-args constructor for JPA. */
    public Profiles() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param username    unique username
     * @param gender      user gender (nullable)
     * @param birthday    user date of birth (nullable)
     * @param location    user location string (nullable)
     * @param joined      registration date
     * @param watching    anime currently watching count
     * @param completed   anime completed count
     * @param onHold      anime on-hold count
     * @param dropped     anime dropped count
     * @param planToWatch anime planned count
     */
    public Profiles(String username, String gender, LocalDate birthday, String location,
                    LocalDate joined, Integer watching, Integer completed, Integer onHold,
                    Integer dropped, Integer planToWatch) {
        this.username    = username;
        this.gender      = gender;
        this.birthday    = birthday;
        this.location    = location;
        this.joined      = joined;
        this.watching    = watching;
        this.completed   = completed;
        this.onHold      = onHold;
        this.dropped     = dropped;
        this.planToWatch = planToWatch;
    }

    /** @return unique username */
    public String getUsername()   { return username; }
    /** @param username unique username */
    public void setUsername(String username) { this.username = username; }

    /** @return user gender, or {@code null} if not set */
    public String getGender()     { return gender; }
    /** @param gender user gender */
    public void setGender(String gender) { this.gender = gender; }

    /** @return user date of birth, or {@code null} if not set */
    public LocalDate getBirthday() { return birthday; }
    /** @param birthday user date of birth */
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    /** @return user location, or {@code null} if not set */
    public String getLocation()   { return location; }
    /** @param location user location */
    public void setLocation(String location) { this.location = location; }

    /** @return registration date */
    public LocalDate getJoined()  { return joined; }
    /** @param joined registration date */
    public void setJoined(LocalDate joined) { this.joined = joined; }

    /** @return anime currently watching count */
    public Integer getWatching()  { return watching; }
    /** @param watching anime currently watching count */
    public void setWatching(Integer watching) { this.watching = watching; }

    /** @return anime completed count */
    public Integer getCompleted() { return completed; }
    /** @param completed anime completed count */
    public void setCompleted(Integer completed) { this.completed = completed; }

    /** @return anime on-hold count */
    public Integer getOnHold()    { return onHold; }
    /** @param onHold anime on-hold count */
    public void setOnHold(Integer onHold) { this.onHold = onHold; }

    /** @return anime dropped count */
    public Integer getDropped()   { return dropped; }
    /** @param dropped anime dropped count */
    public void setDropped(Integer dropped) { this.dropped = dropped; }

    /** @return anime planned-to-watch count */
    public Integer getPlanToWatch() { return planToWatch; }
    /** @param planToWatch anime planned-to-watch count */
    public void setPlanToWatch(Integer planToWatch) { this.planToWatch = planToWatch; }

    @Override
    public String toString() {
        return "Profiles{username='" + username + "', gender='" + gender
                + "', location='" + location + "', watching=" + watching
                + ", completed=" + completed + ", on_hold=" + onHold
                + ", dropped=" + dropped + ", plan_to_watch=" + planToWatch + '}';
    }
}
