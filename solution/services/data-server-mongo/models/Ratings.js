const mongoose = require('mongoose');

const RatingSchema = new mongoose.Schema({
    user_id: Number,
    anime_id: Number,
    rating: Number
}, {
    collection: 'ratings'
});

module.exports = mongoose.model('Ratings', RatingSchema);