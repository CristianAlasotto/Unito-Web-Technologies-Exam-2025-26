package com.example.dataserverspringboot.entities.characters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Schema(description = "Anime character entity")
@Entity
@Table(name = "characters")
public class Characters {
    
    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;
    
    @Column(name = "url")
    private String url;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "name_kanji")
    private String nameKanji;
    
    @Column(name = "image")
    private String image;
    
    @Column(name = "favorites")
    private Integer favorites;
    
    @Column(name = "about")
    private String about;

    // Constructors
    public Characters() {
    }

    public Characters(Integer characterMalId, String url, String name, String nameKanji, 
                      String image, Integer favorites, String about) {
        this.characterMalId = characterMalId;
        this.url = url;
        this.name = name;
        this.nameKanji = nameKanji;
        this.image = image;
        this.favorites = favorites;
        this.about = about;
    }

    // Getters and Setters
    public Integer getCharacterMalId() {
        return characterMalId;
    }

    public void setCharacterMalId(Integer characterMalId) {
        this.characterMalId = characterMalId;
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

    public String getNameKanji() {
        return nameKanji;
    }

    public void setNameKanji(String nameKanji) {
        this.nameKanji = nameKanji;
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
                "character_mal_id=" + characterMalId +
                ", name='" + name + '\'' +
                ", name_kanji='" + nameKanji + '\'' +
                ", favorites=" + favorites +
                '}';
    }
}
