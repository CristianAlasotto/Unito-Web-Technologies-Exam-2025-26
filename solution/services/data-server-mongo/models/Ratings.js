const mongoose = require('mongoose');

/**
 * Mongoose schema for the 'Ratings' collection.
 * Stores individual user ratings and watch status for anime.
 *
 * @module models/Ratings
 * @requires mongoose
 */
const RatingSchema = new mongoose.Schema({
    /**
     * The username of the person who submitted the rating.
     * @type {String}
     */
    username: String,

    /**
     * The MyAnimeList ID of the anime being rated.
     * @type {Number}
     */
    anime_id: Number,

    /**
     * The watch status of the anime.
     * Examples: 'completed', 'watching', 'plan_to_watch', 'dropped', 'on_hold'.
     * @type {String}
     */
    status: String,  // Changed from Number to String

    /**
     * The score given to the anime (1-10). 0 usually indicates no score.
     * @type {Number}
     */
    score: Number,

    /**
     * Flag indicating if the user is rewatching the anime (1 for yes, 0 for no).
     * @type {Number}
     */
    is_rewatching: Number,

    /**
     * The number of episodes the user has watched so far.
     * @type {Number}
     */
    num_watched_episodes: Number,
}, {
    collection: 'ratings'
});

module.exports = mongoose.model('Ratings', RatingSchema);