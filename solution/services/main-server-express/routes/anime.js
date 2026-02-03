const express = require('express');
const animeController = require('../controllers/animeController');
const { apiPostgres, apiMongo } = require('../controllers/apiClients');
const router = express.Router();

router.get('/', animeController.list);

router.get('/:id/ratings-json', animeController.getRatingsJson);

// POST route for submitting ratings with username validation and stats update
router.post('/:id/ratings', async function(req, res) {
    try {
        const ratingData = req.body;

        // Validate required fields
        if (!ratingData.username || !ratingData.anime_id || !ratingData.score || !ratingData.status || ratingData.num_watched_episodes === undefined) {
            return res.status(400).json({
                error: "Missing required fields",
                message: "username, anime_id, score, status, and num_watched_episodes are required"
            });
        }

        // 1. Validate that username exists in Spring Boot API (port 8080)
        try {
            await apiPostgres.get(`/api/profiles/${ratingData.username}`);
            console.log(`Username validated: ${ratingData.username}`);
        } catch (profileError) {
            return res.status(404).json({
                error: "User not found",
                message: `The username "${ratingData.username}" does not exist.`
            });
        }

        // 2. Forward the request to MongoDB API (port 3001)
        // This returns the pre-calculated new_average_score
        let mongoResponse;
        try {
            mongoResponse = await apiMongo.post('/api/ratings', ratingData);
            console.log("Rating created in Mongo. New average received:", mongoResponse.data.new_average_score);
        } catch (mongoError) {
            return res.status(500).json({
                error: "Failed to save rating",
                message: mongoError.response?.data?.message || mongoError.message
            });
        }

        // 3. Send the NEW average score to Postgres (port 8080)
        try {
            const calculatedScore = mongoResponse.data.new_average_score;

            await apiPostgres.post('/api/details/update_score', {
                mal_id: ratingData.anime_id,
                score: calculatedScore // Forwarding the pre-calculated average from Mongo
            });

            console.log(`Postgres updated with new average: ${calculatedScore}`);
        } catch (postgresError) {
            console.error("Warning: Could not sync average to Postgres:", postgresError.message);
        }

        // Return the full mongo response to the frontend
        return res.status(201).json(mongoResponse.data);

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