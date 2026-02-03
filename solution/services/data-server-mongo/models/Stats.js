const mongoose = require('mongoose');

/**
 * Mongoose schema for the 'Stats' collection.
 * Stores aggregated statistics for each anime, including watch status counts
 * and a detailed breakdown of user voting scores (1-10).
 *
 * @module models/Stats
 * @requires mongoose
 */
const StatSchema = new mongoose.Schema({
    /**
     * The unique MyAnimeList ID associated with these statistics.
     * @type {Number}
     */
    mal_id: Number,

    /**
     * Number of users currently watching this anime.
     * @type {Number}
     */
    watching: Number,

    /**
     * Number of users who have completed this anime.
     * @type {Number}
     */
    completed: Number,

    /**
     * Number of users who have put this anime on hold.
     * @type {Number}
     */
    on_hold: Number,

    /**
     * Number of users who have dropped this anime.
     * @type {Number}
     */
    dropped: Number,

    /**
     * Number of users planning to watch this anime.
     * @type {Number}
     */
    plan_to_watch: Number,

    /**
     * Total number of interactions/votes recorded.
     * @type {Number}
     */
    total: Number,

    // Score Votes (Raw counts)
    score_1_votes: Number,
    score_2_votes: Number,
    score_3_votes: Number,
    score_4_votes: Number,
    score_5_votes: Number,
    score_6_votes: Number,
    score_7_votes: Number,
    score_8_votes: Number,
    score_9_votes: Number,
    score_10_votes: Number,

    // Score Percentages (Calculated fields)
    score_1_percentage: Number,
    score_2_percentage: Number,
    score_3_percentage: Number,
    score_4_percentage: Number,
    score_5_percentage: Number,
    score_6_percentage: Number,
    score_7_percentage: Number,
    score_8_percentage: Number,
    score_9_percentage: Number,
    score_10_percentage: Number,

}, {
    collection: 'stats'
});

module.exports = mongoose.model('Stats', StatSchema);