const mongoose = require('mongoose');

const RatingSchema = new mongoose.Schema({
    username: String,
    anime_id: Number,
    status: Number,
    score: Number,
    is_rewatching: Number,
    num_watched_episodes: Number,

    createdAt: { type: Date, default: Date.now }
}, {
    collection: 'ratings'
});

module.exports = mongoose.model('Ratings', RatingSchema);