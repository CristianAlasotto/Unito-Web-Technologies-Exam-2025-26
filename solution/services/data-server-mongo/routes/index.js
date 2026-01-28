const express = require('express');
const router = express.Router();
const axios = require('axios'); // Needed for the external call

// Your existing controllers
const favorites = require('../controllers/favController');
const statistics = require('../controllers/statController');
const ratings = require('../controllers/ratController');

// Configuration
const EXTERNAL_SERVICE_URL = 'http://localhost:3001';
const STALE_MS = 60 * 1000; // 1 minute cache (adjust as needed)

router.get('/favorites', async (req, res) => {
    try {

        const cached = await favorites.getFavs(req.query, STALE_MS);

        if (cached) {
            console.log('[CACHE HIT] Returning Favorites from MongoDB');
            return res.json(cached);
        }

        console.log('[CACHE MISS] Fetching Favorites from External API');
        const response = await axios.get(`${EXTERNAL_SERVICE_URL}/favorites`, { params: req.query });
        const freshData = response.data;

        await favorites.saveFavs(freshData);

        res.json(freshData);

    } catch (err) {
        res.status(500).json({ error: "Server Error", details: err.message });
    }
});

router.get('/statistics', async (req, res) => {
    try {

        const cached = await statistics.getStats(req.query, STALE_MS);
        if (cached) return res.json(cached);

        const response = await axios.get(`${EXTERNAL_SERVICE_URL}/statistics`, { params: req.query });
        const freshData = response.data;

        await statistics.saveStats(freshData);

        res.json(freshData);

    } catch (err) {
        res.status(500).json({ error: "Server Error", details: err.message });
    }
});

router.get('/statistics/:id', async (req, res) => {
    try {
        const { id } = req.params;

        const cached = await statistics.getStatById(id, STALE_MS);
        if (cached) {
            console.log(`[CACHE HIT] Returning Stat ${id} from MongoDB`);
            return res.json(cached);
        }

        console.log(`[CACHE MISS] Fetching Stat ${id} from External API`);
        const response = await axios.get(`${EXTERNAL_SERVICE_URL}/statistics/${id}`);
        const freshData = response.data;

        await statistics.saveStats(freshData);

        res.json(freshData);
    } catch (err) {
        res.status(500).json({ error: "Server Error", details: err.message });
    }
});

router.get('/ratings', async (req, res) => {
    try {

        const cached = await ratings.getRats(req.query, STALE_MS);
        if (cached) return res.json(cached);

        const response = await axios.get(`${EXTERNAL_SERVICE_URL}/ratings`, { params: req.query });
        const freshData = response.data;

        await ratings.saveRats(freshData);

        res.json(freshData);

    } catch (err) {
        res.status(500).json({ error: "Server Error", details: err.message });
    }
});

module.exports = router;