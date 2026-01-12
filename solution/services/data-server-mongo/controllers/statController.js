const Stats = require("../models/Stats");
const axios = require('axios');

exports.getStats = async (req, res) => {
    try {
        // CHANGE: Use Stats.find() instead of Rating.find()
        const stats = await Stats.find().limit(10);

        res.json(stats);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};