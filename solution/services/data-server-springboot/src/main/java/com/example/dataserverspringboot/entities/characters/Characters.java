package com.example.dataserverspringboot.entities.characters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Schema(description = "Anime character entity")
@Entity
@Table(name = "characters")
public class Characters {

    @Schema(description = "Character MyAnimeList ID (primary key)", example = "1")
    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;

    @Schema(description = "MyAnimeList character URL", example = "https://myanimelist.net/character/1")
    @Column(name = "url")
    private String url;

    @Schema(description = "Character name", example = "Spike Spiegel")
    @Column(name = "name")
    private String name;

    @Schema(description = "Character name in Japanese/Kanji", example = "スパイク・スピーゲル")
    @Column(name = "name_kanji")
    private String nameKanji;

    @Schema(description = "Character image URL", example = "https://cdn.myanimelist.net/images/characters/1/1.jpg")
    @Column(name = "image")
    private String image;

    @Schema(description = "Number of users who favorited this character", example = "50000")
    @Column(name = "favorites")
    private Integer favorites;

    @Schema(description = "Character biography/description")
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
