const express = require('express');
const animeController = require('../controllers/animeController');
const { apiPostgres, apiMongo } = require('../controllers/apiClients');
const router = express.Router();

router.get('/', animeController.list);

router.get('/:id/ratings-json', animeController.getRatingsJson);

// POST route for submitting ratings with username validation
router.post('/:id/ratings', async function(req, res) {
    try {
        const ratingData = req.body;

        console.log("Received rating data:", ratingData);

        // Validate required fields
        if (!ratingData.username || !ratingData.anime_id || !ratingData.score || !ratingData.status || ratingData.num_watched_episodes === undefined) {
            return res.status(400).json({
                error: "Missing required fields",
                message: "username, anime_id, score, status, and num_watched_episodes are required"
            });
        }

        // Validate that username exists in Spring Boot API (port 8080)
        try {
            await apiPostgres.get(`/api/profiles/${ratingData.username}`);
            console.log(`Username validated: ${ratingData.username}`);
        } catch (profileError) {
            console.log(`Username validation failed for: ${ratingData.username}`);
            return res.status(404).json({
                error: "User not found",
                message: `The username "${ratingData.username}" does not exist. Please check your username.`
            });
        }

        // Forward the request to MongoDB API (port 3001)
        try {
            const response = await apiMongo.post('/api/ratings', ratingData);
            console.log("Rating created successfully via MongoDB API");
            return res.status(201).json(response.data);
        } catch (mongoError) {
            console.error("Error creating rating in MongoDB:", mongoError.message);
            return res.status(500).json({
                error: "Failed to save rating",
                message: mongoError.response?.data?.message || mongoError.message
            });
        }

    } catch (error) {
        console.error("Error in rating creation:", error);
        return res.status(500).json({
            error: "Internal Server Error",
            message: error.message
        });
    }
});

router.get('/:id', animeController.detail);

router.get('/:id/recommendations', animeController.reccomendations);

module.exports = router;