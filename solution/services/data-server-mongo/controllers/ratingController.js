const Rating = require('../models/Ratings');
const axios = require('axios'); // Required by Lecture 3

exports.getRatings = async (req, res) => {
    try {
        // 1. Get data from DB
        const ratings = await Rating.find().limit(10);

        // 2. Send response
        res.json(ratings);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};