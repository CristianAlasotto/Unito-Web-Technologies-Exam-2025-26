const Rating = require('../models/Ratings');
const axios = require('axios');

exports.getRatings = async (req, res) => {
    try {
        const ratings = await Rating.find().limit(10);

        res.json(ratings);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};