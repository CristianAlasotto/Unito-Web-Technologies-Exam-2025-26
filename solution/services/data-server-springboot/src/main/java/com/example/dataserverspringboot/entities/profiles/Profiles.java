package com.anime.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "profiles")
public class Profiles {
    
    @Id
    @Column(name = "username")
    private String username;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "birthday")
    private LocalDate birthday;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "joined")
    private LocalDate joined;
    
    @Column(name = "watching")
    private Integer watching;
    
    @Column(name = "completed")
    private Integer completed;
    
    @Column(name = "on_hold")
    private Integer on_hold;
    
    @Column(name = "dropped")
    private Integer dropped;
    
    @Column(name = "plan_to_watch")
    private Integer plan_to_watch;

    // Constructors
    public Profiles() {
    }

    public Profiles(String username, String gender, LocalDate birthday, String location, 
                    LocalDate joined, Integer watching, Integer completed, Integer on_hold, 
                    Integer dropped, Integer plan_to_watch) {
        this.username = username;
        this.gender = gender;
        this.birthday = birthday;
        this.location = location;
        this.joined = joined;
        this.watching = watching;
        this.completed = completed;
        this.on_hold = on_hold;
        this.dropped = dropped;
        this.plan_to_watch = plan_to_watch;
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

    public Integer getOn_hold() {
        return on_hold;
    }

    public void setOn_hold(Integer on_hold) {
        this.on_hold = on_hold;
    }

    public Integer getDropped() {
        return dropped;
    }

    public void setDropped(Integer dropped) {
        this.dropped = dropped;
    }

    public Integer getPlan_to_watch() {
        return plan_to_watch;
    }

    public void setPlan_to_watch(Integer plan_to_watch) {
        this.plan_to_watch = plan_to_watch;
    }

    @Override
    public String toString() {
        return "Profiles{" +
                "username='" + username + '\'' +
                ", gender='" + gender + '\'' +
                ", location='" + location + '\'' +
                ", watching=" + watching +
                ", completed=" + completed +
                ", on_hold=" + on_hold +
                ", dropped=" + dropped +
                ", plan_to_watch=" + plan_to_watch +
                '}';
    }
}
