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

// --- ROUTE: Favorites ---
router.get('/favorites', async (req, res) => {
    try {
        // 1. Check Local Cache (MongoDB)
        // You must update favorites.getFavs to RETURN data (promise), not res.json()
        const cached = await favorites.getFavs(req.query, STALE_MS);

        if (cached) {
            console.log('[CACHE HIT] Returning Favorites from MongoDB');
            return res.json(cached);
        }

        // 2. Fetch from External Service (Cache Miss)
        console.log('[CACHE MISS] Fetching Favorites from External API');
        const response = await axios.get(`${EXTERNAL_SERVICE_URL}/favorites`, { params: req.query });
        const freshData = response.data;

        // 3. Save to Local Cache
        // You must add a 'saveFavs' function to your controller
        await favorites.saveFavs(freshData);

        // 4. Return Data
        res.json(freshData);

    } catch (err) {
        res.status(500).json({ error: "Server Error", details: err.message });
    }
});

// --- ROUTE: Statistics ---
router.get('/statistics', async (req, res) => {
    try {
        // 1. Check Cache
        const cached = await statistics.getStats(req.query, STALE_MS);
        if (cached) return res.json(cached);

        // 2. Fetch External
        const response = await axios.get(`${EXTERNAL_SERVICE_URL}/statistics`, { params: req.query });
        const freshData = response.data;

        // 3. Save Cache
        await statistics.saveStats(freshData);

        // 4. Return
        res.json(freshData);

    } catch (err) {
        res.status(500).json({ error: "Server Error", details: err.message });
    }
});

// --- ROUTE: Ratings ---
router.get('/ratings', async (req, res) => {
    try {
        // 1. Check Cache
        const cached = await ratings.getRats(req.query, STALE_MS);
        if (cached) return res.json(cached);

        // 2. Fetch External
        const response = await axios.get(`${EXTERNAL_SERVICE_URL}/ratings`, { params: req.query });
        const freshData = response.data;

        // 3. Save Cache
        await ratings.saveRats(freshData);

        // 4. Return
        res.json(freshData);

    } catch (err) {
        res.status(500).json({ error: "Server Error", details: err.message });
    }
});

module.exports = router;