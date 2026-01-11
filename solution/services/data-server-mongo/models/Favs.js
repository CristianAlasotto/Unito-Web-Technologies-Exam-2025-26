const mongoose = require('mongoose');

const FavSchema = new mongoose.Schema({
    username: String,
    fav_type: String,
    id: Number
}, {
    collection: 'favs'
});

module.exports = mongoose.model('Favs', FavSchema);