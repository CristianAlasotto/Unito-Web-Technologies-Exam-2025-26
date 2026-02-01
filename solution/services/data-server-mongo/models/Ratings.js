const mongoose = require('mongoose');

const RatingSchema = new mongoose.Schema({
    username: String,
    anime_id: Number,
    status: String,  // Changed from Number to String
    score: Number,
    is_rewatching: Number,
    num_watched_episodes: Number,
}, {
    collection: 'ratings'
});

module.exports = mongoose.model('Ratings', RatingSchema);