package com.example.dataserverspringboot.entities.characters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

/**
 * JPA entity representing an anime character.
 *
 * <p>Maps to the {@code characters} table in PostgreSQL. Each row represents
 * one character that appears in one or more anime. Characters are linked to
 * anime via the {@code character_anime_works} junction table, and to voice
 * actors via the {@code person_voice_works} junction table.</p>
 *
 * <p>The primary key is {@code character_mal_id} (a natural {@link Integer}
 * key imported from the MyAnimeList CSV dataset — no {@code @GeneratedValue}
 * because the value is assigned by MyAnimeList).</p>
 *
 * <p>Nullable fields: {@code name_kanji}, {@code image}, {@code favorites},
 * {@code about}. These are filtered via IS NULL / IS NOT NULL in
 * {@link CharactersService}.</p>
 */
@Schema(description = "Anime character entity")
@Entity
@Table(name = "characters")
public class Characters {

    /** Character MAL ID — natural primary key, imported from the MyAnimeList dataset. */
    @Schema(description = "Character MyAnimeList ID (primary key)", example = "1")
    @Id
    @Column(name = "character_mal_id")
    private Integer characterMalId;

    /** MyAnimeList character profile URL. */
    @Schema(description = "MyAnimeList character URL", example = "https://myanimelist.net/character/1")
    @Column(name = "url")
    private String url;

    /** Character name in romaji. */
    @Schema(description = "Character name", example = "Spike Spiegel")
    @Column(name = "name")
    private String name;

    /** Character name written in kanji/kana — nullable. */
    @Schema(description = "Character name in Japanese/Kanji", example = "スパイク・スピーゲル")
    @Column(name = "name_kanji")
    private String nameKanji;

    /** Character profile image URL — nullable. */
    @Schema(description = "Character image URL",
            example = "https://cdn.myanimelist.net/images/characters/1/1.jpg")
    @Column(name = "image")
    private String image;

    /** Number of users who have this character in their favourites — nullable. */
    @Schema(description = "Number of users who favorited this character", example = "50000")
    @Column(name = "favorites")
    private Integer favorites;

    /** Character biography or description — nullable. */
    @Schema(description = "Character biography/description")
    @Column(name = "about")
    private String about;

    /** Required no-args constructor for JPA. */
    public Characters() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param characterMalId character MAL ID
     * @param url            MAL profile URL
     * @param name           character name in romaji
     * @param nameKanji      character name in kanji (nullable)
     * @param image          profile image URL (nullable)
     * @param favorites      favourites count (nullable)
     * @param about          biography (nullable)
     */
    public Characters(Integer characterMalId, String url, String name, String nameKanji,
                      String image, Integer favorites, String about) {
        this.characterMalId = characterMalId;
        this.url            = url;
        this.name           = name;
        this.nameKanji      = nameKanji;
        this.image          = image;
        this.favorites      = favorites;
        this.about          = about;
    }

    /** @return character MAL ID */
    public Integer getCharacterMalId()              { return characterMalId; }
    /** @param characterMalId character MAL ID */
    public void setCharacterMalId(Integer characterMalId) { this.characterMalId = characterMalId; }

    /** @return MAL profile URL */
    public String getUrl()                          { return url; }
    /** @param url MAL profile URL */
    public void setUrl(String url)                  { this.url = url; }

    /** @return character name in romaji */
    public String getName()                         { return name; }
    /** @param name character name in romaji */
    public void setName(String name)                { this.name = name; }

    /** @return character name in kanji, or {@code null} if not set */
    public String getNameKanji()                    { return nameKanji; }
    /** @param nameKanji character name in kanji */
    public void setNameKanji(String nameKanji)      { this.nameKanji = nameKanji; }

    /** @return profile image URL, or {@code null} if not set */
    public String getImage()                        { return image; }
    /** @param image profile image URL */
    public void setImage(String image)              { this.image = image; }

    /** @return favourites count, or {@code null} if not set */
    public Integer getFavorites()                   { return favorites; }
    /** @param favorites favourites count */
    public void setFavorites(Integer favorites)     { this.favorites = favorites; }

    /** @return biography, or {@code null} if not set */
    public String getAbout()                        { return about; }
    /** @param about biography */
    public void setAbout(String about)              { this.about = about; }

    @Override
    public String toString() {
        return "Characters{character_mal_id=" + characterMalId
                + ", name='" + name + "', name_kanji='" + nameKanji
                + "', favorites=" + favorites + '}';
    }
}
