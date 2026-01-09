package com.example.dataserverspringboot.entities.characters;

import jakarta.persistence.*;

@Entity
@Table(name = "characters")
public class Characters {
    
    @Id
    @Column(name = "character_mal_id")
    private Integer character_mal_id;
    
    @Column(name = "url")
    private String url;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "name_kanji")
    private String name_kanji;
    
    @Column(name = "image")
    private String image;
    
    @Column(name = "favorites")
    private Integer favorites;
    
    @Column(name = "about")
    private String about;

    // Constructors
    public Characters() {
    }

    public Characters(Integer character_mal_id, String url, String name, String name_kanji, 
                      String image, Integer favorites, String about) {
        this.character_mal_id = character_mal_id;
        this.url = url;
        this.name = name;
        this.name_kanji = name_kanji;
        this.image = image;
        this.favorites = favorites;
        this.about = about;
    }

    // Getters and Setters
    public Integer getCharacter_mal_id() {
        return character_mal_id;
    }

    public void setCharacter_mal_id(Integer character_mal_id) {
        this.character_mal_id = character_mal_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_kanji() {
        return name_kanji;
    }

    public void setName_kanji(String name_kanji) {
        this.name_kanji = name_kanji;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getFavorites() {
        return favorites;
    }

    public void setFavorites(Integer favorites) {
        this.favorites = favorites;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    @Override
    public String toString() {
        return "Characters{" +
                "character_mal_id=" + character_mal_id +
                ", name='" + name + '\'' +
                ", name_kanji='" + name_kanji + '\'' +
                ", favorites=" + favorites +
                '}';
    }
}
