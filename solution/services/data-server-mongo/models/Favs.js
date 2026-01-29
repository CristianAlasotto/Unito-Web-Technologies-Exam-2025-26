const mongoose = require('mongoose');

const FavSchema = new mongoose.Schema({
    username: String,
    fav_type: String,
    id: Number,

    createdAt: {
        type: Date,
        default: Date.now
    }
}, {
    collection: 'favs'
});

FavSchema.index({ username: "text", fav_type: "text" });

module.exports = mongoose.model('Favs', FavSchema);