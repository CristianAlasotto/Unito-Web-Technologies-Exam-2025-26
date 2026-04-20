package com.example.dataserverspringboot.entities.profiles;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;

@Schema(description = "Website user profile entity")
@Entity
@Table(name = "profiles")
public class Profiles {
    
    @Schema(description = "User unique username (Primary Key)", example = "Xinil")
    @Id
    @Column(name = "username")
    private String username;

    @Schema(description = "User gender", example = "Male")
    @Column(name = "gender")
    private String gender;

    @Schema(description = "User birthday", example = "1990-01-01")
    @Column(name = "birthday")
    private LocalDate birthday;

    @Schema(description = "User location", example = "California")
    @Column(name = "location")
    private String location;

    @Schema(description = "Date user joined the site", example = "2007-05-23")
    @Column(name = "joined")
    private LocalDate joined;

    @Schema(description = "Number of anime currently watching", example = "5")
    @Column(name = "watching")
    private Integer watching;

    @Schema(description = "Number of anime completed", example = "100")
    @Column(name = "completed")
    private Integer completed;

    @Schema(description = "Number of anime on hold", example = "2")
    @Column(name = "on_hold")
    private Integer onHold;

    @Schema(description = "Number of anime dropped", example = "1")
    @Column(name = "dropped")
    private Integer dropped;

    @Schema(description = "Number of anime planned to watch", example = "50")
    @Column(name = "plan_to_watch")
    private Integer planToWatch;

    // Constructors
    public Profiles() {
    }

    public Profiles(String username, String gender, LocalDate birthday, String location,
                    LocalDate joined, Integer watching, Integer completed, Integer onHold,
                    Integer dropped, Integer planToWatch) {
        this.username = username;
        this.gender = gender;
        this.birthday = birthday;
        this.location = location;
        this.joined = joined;
        this.watching = watching;
        this.completed = completed;
        this.onHold = onHold;
        this.dropped = dropped;
        this.planToWatch = planToWatch;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getJoined() {
        return joined;
    }

    public void setJoined(LocalDate joined) {
        this.joined = joined;
    }

    public Integer getWatching() {
        return watching;
    }

    public void setWatching(Integer watching) {
        this.watching = watching;
    }

    public Integer getCompleted() {
        return completed;
    }

    public void setCompleted(Integer completed) {
        this.completed = completed;
    }

    public Integer getOnHold() {
        return onHold;
    }

    public void setOnHold(Integer onHold) {
        this.onHold = onHold;
    }

    public Integer getDropped() {
        return dropped;
    }

    public void setDropped(Integer dropped) {
        this.dropped = dropped;
    }

    public Integer getPlanToWatch() {
        return planToWatch;
    }

    public void setPlanToWatch(Integer planToWatch) {
        this.planToWatch = planToWatch;
    }

    @Override
    public String toString() {
        return "Profiles{" +
                "username='" + username + '\'' +
                ", gender='" + gender + '\'' +
                ", location='" + location + '\'' +
                ", watching=" + watching +
                ", completed=" + completed +
                ", on_hold=" + onHold +
                ", dropped=" + dropped +
                ", plan_to_watch=" + planToWatch +
                '}';
    }
}