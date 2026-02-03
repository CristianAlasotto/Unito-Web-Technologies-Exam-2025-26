const mongoose = require('mongoose');

/**
 * Mongoose schema for the 'Favs' collection.
 * This collection stores user favorites, linking a user to a specific item (Anime, Character, etc.).
 *
 * @module models/Favs
 * @requires mongoose
 */
const FavSchema = new mongoose.Schema({
    /**
     * The username of the user who marked the item as a favorite.
     * @type {String}
     */
    username: String,

    /**
     * The type/category of the favorite item.
     * Examples: 'Anime', 'Manga', 'Character', 'Person'.
     * @type {String}
     */
    fav_type: String,

    /**
     * The external ID of the favorited item (e.g., the MyAnimeList ID).
     * @type {Number}
     */
    id: Number,
}, {
    collection: 'favs'
});

/**
 * Text index on 'username' and 'fav_type' to support efficient text searching.
 */
FavSchema.index({ username: "text", fav_type: "text" });

module.exports = mongoose.model('Favs', FavSchema);