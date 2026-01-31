const mongoose = require('mongoose');

const FavSchema = new mongoose.Schema({
    username: String,
    fav_type: String,
    id: Number,
}, {
    collection: 'favs'
});

FavSchema.index({ username: "text", fav_type: "text" });

module.exports = mongoose.model('Favs', FavSchema);