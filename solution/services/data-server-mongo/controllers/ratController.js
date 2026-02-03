const ratService = require("../services/ratService");

/**
 * Controller to handle HTTP requests for Ratings.
 *
 * @module controllers/ratController
 */

/**
 * GET /api/ratings
 * Retrieves ratings based on query parameters.
 *
 * @async
 * @function getRats
 * @param {import('express').Request} req - Express request object.
 * @param {import('express').Response} res - Express response object.
 * @returns {Promise<void>} JSON response with rating data or error.
 */
exports.getRats = async (req, res) => {
    try {
        const params = req.query;
        const data = await ratService.fetchRatings(params);

        if (!data || !data.items || data.items.length === 0) {
            return res.status(404).json({ message: "No ratings found" });
        }

        return res.json(data);
    } catch (error) {
        console.error("Error fetching ratings:", error);
        return res.status(500).json({ error: "Internal Server Error" });
    }
};

/**
 * POST /api/ratings
 * Creates a new rating and updates relevant statistics.
 * Expects a JSON body with anime_id, status, and score.
 *
 * @async
 * @function createRating
 * @param {import('express').Request} req - Express request object.
 * @param {Object} req.body - The payload containing rating details.
 * @param {import('express').Response} res - Express response object.
 * @returns {Promise<void>} JSON response with the new average score.
 */
exports.createRating = async (req, res) => {
    try {
        const ratingData = req.body;

        // Delegates to service to update stats (and optionally save rating)
        const newRating = await ratService.createRating(ratingData);

        return res.status(201).json(newRating);
    } catch (error) {
        console.error("Error creating rating:", error);
        return res.status(500).json({ error: "Internal Server Error" });
    }
};