const express = require('express');
const router = express.Router();
const axios = require('axios'); 

const favorites = require('../controllers/favController');
const statistics = require('../controllers/statController');
const ratings = require('../controllers/ratController');

const EXTERNAL_SERVICE_URL = 'http://localhost:3001';
const STALE_MS = 60 * 1000; 

// ... (Favorites and Statistics routes omitted for brevity if unchanged) ...

router.get('/ratings', async (req, res) => {
    try {
        // 1. Try Local DB
        const cached = await ratings.getRats(req.query, STALE_MS);
        if (cached) return res.json(cached);

        // 2. External API Fallback
        // Note: Ensure EXTERNAL_SERVICE_URL points to the correct upstream source
        const response = await axios.get(`${EXTERNAL_SERVICE_URL}/ratings`, { params: req.query });
        const freshData = response.data;

        await ratings.saveRats(freshData);

        // 3. Re-fetch from Local DB to get correct Pagination Object structure
        const savedData = await ratings.getRats(req.query, STALE_MS);

        res.json(savedData || freshData);

    } catch (err) {
        res.status(500).json({ error: "Server Error", details: err.message });
    }
});

module.exports = router;