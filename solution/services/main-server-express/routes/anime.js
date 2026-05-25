const express = require('express');
const animeController = require('../controllers/animeController');
const { apiPostgres, apiMongo } = require('../controllers/apiClients');
const router = express.Router();

/**
 * @swagger
 * /anime:
 *   get:
 *       summary: Returns the anime list.
 *       description: Retrieves the anime list with optional filters for genre, year, rating, and more.
 *       tags:
 *         - Anime
 *       parameters:
 *         - in: query
 *           name: search
 *           required: false
 *           schema:
 *             type: string
 *           description: Text to search for in titles.
 *       responses:
 *         200:
 *           description: Anime list.
 *           content:
 *             application/json:
 *               schema:
 *                 type: array
 *                 items:
 *                   type: object
 *                   properties:
 *                     id:
 *                       type: string
 *                       description: Anime ID.
 *                     title:
 *                       type: string
 *                       description: Anime title.
 */
router.get('/', animeController.list);

/**
 * @swagger
 * /anime/{id}/ratings-json:
 *   get:
 *       summary: Retrieves ratings for an anime in JSON format.
 *       description: Returns the ratings associated with the anime identified by {id}.
 *       tags:
 *         - Ratings
 *       parameters:
 *         - in: path
 *           name: id
 *           required: true
 *           schema:
 *             type: string
 *           description: Anime ID.
 *       responses:
 *         200:
 *           description: Rating list for the anime.
 *           content:
 *             application/json:
 *               schema:
 *                 type: array
 *                 items:
 *                   type: object
 *                   properties:
 *                     username:
 *                       type: string
 *                     anime_id:
 *                       type: string
 *                     score:
 *                       type: number
 *                     status:
 *                       type: string
 *                     num_watched_episodes:
 *                       type: integer
 *         404:
 *           description: Anime not found.
 */
router.get('/:id/ratings-json', animeController.getRatingsJson);

/**
 * @swagger
 * /anime/{id}/ratings:
 *   post:
 *       summary: Records a rating for an anime.
 *       description: Adds a new rating for the anime identified by {id}.
 *       tags:
 *         - Ratings
 *       parameters:
 *         - in: path
 *           name: id
 *           required: true
 *           schema:
 *             type: string
 *           description: Anime ID.
 *       requestBody:
 *         required: true
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 username:
 *                   type: string
 *                 anime_id:
 *                   type: string
 *                 score:
 *                   type: number
 *                 status:
 *                   type: string
 *                 num_watched_episodes:
 *                   type: integer
 *       responses:
 *         201:
 *           description: Rating created successfully.
 */
/**
 * Validates and stores a user rating for an anime.
 *
 * @param {Object} req Express request with rating body.
 * @param {Object} res Express response.
 * @returns {Promise<void>} Resolves when the JSON response is sent.
 */
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

/**
 * @swagger
 * /anime/{id}/characters:
 *   get:
 *       summary: Returns the characters linked to an anime.
 *       description: Retrieves the characters linked to the anime through the main server.
 *       tags:
 *         - Anime
 *       parameters:
 *         - in: path
 *           name: id
 *           required: true
 *           schema:
 *             type: string
 *           description: Anime ID.
 *       responses:
 *         200:
 *           description: Linked character list.
 *         404:
 *           description: Anime not found.
 */
router.get('/:id/characters', animeController.characters);

/**
 * @swagger
 * /anime/{id}:
 *   get:
 *       summary: Returns anime details.
 *       description: Retrieves detailed information for the anime identified by {id}.
 *       tags:
 *         - Anime
 *       parameters:
 *         - in: path
 *           name: id
 *           required: true
 *           schema:
 *             type: string
 *           description: Anime ID.
 *       responses:
 *         200:
 *           description: Anime details.
 *           content:
 *             application/json:
 *               schema:
 *                 type: object
 *                 properties:
 *                   id:
 *                     type: string
 *                   title:
 *                     type: string
 *                   synopsis:
 *                     type: string
 *                   genres:
 *                     type: array
 *                     items:
 *                       type: string
 *         404:
 *           description: Anime not found.
 */
router.get('/:id', animeController.detail);

/**
 * @swagger
 * /anime/{id}/recommendations:
 *   get:
 *       summary: Returns recommendations for an anime.
 *       description: Retrieves a list of recommended anime based on the anime identified by {id}.
 *       tags:
 *         - Anime
 *       parameters:
 *         - in: path
 *           name: id
 *           required: true
 *           schema:
 *             type: string
 *           description: Anime ID.
 *       responses:
 *         200:
 *           description: Recommended anime list.
 *           content:
 *             application/json:
 *               schema:
 *                 type: array
 *                 items:
 *                   type: object
 *                   properties:
 *                     id:
 *                       type: string
 *                     title:
 *                       type: string
 *         404:
 *           description: Anime not found.
 */
router.get('/:id/recommendations', animeController.reccomendations);

module.exports = router;
