const Favs = require("../models/Favs");
const axios = require('axios');

exports.getFavs = async (req, res) => {
    try {
        const favs = await Favs.find().limit(10);

        res.json(favs);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};