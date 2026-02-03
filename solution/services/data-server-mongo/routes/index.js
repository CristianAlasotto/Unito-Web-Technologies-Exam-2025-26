const express = require('express');
const router = express.Router();

const favorites = require('../controllers/favController');
const statistics = require('../controllers/statController');
const ratings = require('../controllers/ratController');

/**
 * @swagger
 * components:
 *   schemas:
 *     Favorite:
 *       type: object
 *       properties:
 *         _id:
 *           type: string
 *           description: The MongoDB ObjectId
 *         username:
 *           type: string
 *           description: The user who favorited the item
 *           example: "SakuraFan99"
 *         fav_type:
 *           type: string
 *           description: Type of favorite (e.g. Anime, Character)
 *           example: "Anime"
 *         id:
 *           type: integer
 *           description: External ID of the favorited item
 *           example: 5114
 *     Statistic:
 *       type: object
 *       properties:
 *         mal_id:
 *           type: integer
 *           description: MyAnimeList ID
 *         watching:
 *           type: integer
 *           description: Number of users currently watching
 *         completed:
 *           type: integer
 *           description: Number of users who completed it
 *         total:
 *           type: integer
 *           description: Total interactions recorded
 *         score_10_votes:
 *           type: integer
 *           description: Number of 10/10 votes
 *         score_10_percentage:
 *           type: number
 *           format: float
 *           description: Percentage of 10/10 votes
 *     Rating:
 *       type: object
 *       properties:
 *         username:
 *           type: string
 *         anime_id:
 *           type: integer
 *         status:
 *           type: string
 *           description: Watch status (e.g., "completed", "watching")
 *         score:
 *           type: integer
 *           description: Score given (1-10)
 *         is_rewatching:
 *           type: integer
 *           description: 1 if rewatching, 0 otherwise
 *         num_watched_episodes:
 *           type: integer
 *     RatingResponse:
 *       type: object
 *       properties:
 *         items:
 *           type: array
 *           items:
 *             $ref: '#/components/schemas/Rating'
 *         total:
 *           type: integer
 *           description: Total count matching filters
 *         totalPages:
 *           type: integer
 *           description: Total pages available
 */

/**
 * @swagger
 * /api/favorites:
 *   get:
 *     summary: Retrieve favorites
 *     description: Fetch favorites with support for pagination, sorting, and field selection.
 *     tags: [Favorites]
 *     parameters:
 *       - in: query
 *         name: username
 *         schema:
 *           type: string
 *         description: Filter by username
 *       - in: query
 *         name: fav_type
 *         schema:
 *           type: string
 *         description: Filter by type (e.g., "Anime")
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           default: 1
 *         description: Page number for pagination
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 20
 *         description: Items per page
 *       - in: query
 *         name: sort
 *         schema:
 *           type: string
 *           example: "-id"
 *         description: Fields to sort by (comma-separated). Use minus (-) for descending.
 *       - in: query
 *         name: fields
 *         schema:
 *           type: string
 *           example: "username,id"
 *         description: Comma-separated fields to return
 *     responses:
 *       200:
 *         description: List of favorites matching the criteria
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/Favorite'
 *       404:
 *         description: No favorites found
 */
router.get('/favorites', favorites.getFavs);

/**
 * @swagger
 * /api/statistics:
 *   get:
 *     summary: Retrieve anime statistics
 *     description: Get aggregated stats for animes (watching, completed, scores, etc.).
 *     tags: [Statistics]
 *     parameters:
 *       - in: query
 *         name: mal_id
 *         schema:
 *           type: integer
 *         description: Filter by MyAnimeList ID
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           default: 1
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 20
 *       - in: query
 *         name: sort
 *         schema:
 *           type: string
 *           example: "-total"
 *         description: Sort by total votes, watching count, etc.
 *     responses:
 *       200:
 *         description: List of statistics
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/Statistic'
 *       404:
 *         description: No statistics found
 */
router.get('/statistics', statistics.getStats);

/**
 * @swagger
 * /api/statistics/{id}:
 *   get:
 *     summary: Retrieve specific anime statistics
 *     tags: [Statistics]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: The MyAnimeList ID (mal_id)
 *     responses:
 *       200:
 *         description: Single statistic object
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/Statistic'
 *       404:
 *         description: Statistic not found
 */
router.get('/statistics/:id', statistics.getStatById);

/**
 * @swagger
 * /api/ratings:
 *   get:
 *     summary: Retrieve and search ratings
 *     description: Advanced search for ratings including score ranges and numeric filters.
 *     tags: [Ratings]
 *     parameters:
 *       - in: query
 *         name: anime_id
 *         schema:
 *           type: integer
 *         description: Filter by anime ID
 *       - in: query
 *         name: score
 *         schema:
 *           type: integer
 *         description: Filter by exact score
 *       - in: query
 *         name: minScore
 *         schema:
 *           type: integer
 *         description: Filter ratings greater than or equal to this value
 *       - in: query
 *         name: maxScore
 *         schema:
 *           type: integer
 *         description: Filter ratings less than or equal to this value
 *       - in: query
 *         name: page
 *         schema:
 *           type: integer
 *           default: 1
 *       - in: query
 *         name: limit
 *         schema:
 *           type: integer
 *           default: 20
 *     responses:
 *       200:
 *         description: Paginated ratings list
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/RatingResponse'
 *       404:
 *         description: No ratings found
 *   post:
 *     summary: Create a new rating
 *     description: Submit a new rating. This automatically updates the aggregate statistics.
 *     tags: [Ratings]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - anime_id
 *               - status
 *               - score
 *             properties:
 *               anime_id:
 *                 type: integer
 *               status:
 *                 type: string
 *                 description: e.g. "completed"
 *               score:
 *                 type: integer
 *                 description: Rating 1-10
 *     responses:
 *       201:
 *         description: Rating created and stats updated
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 new_average_score:
 *                   type: number
 *                   description: The updated average score for the anime
 *       500:
 *         description: Internal Server Error
 */
router.get('/ratings', ratings.getRats);
router.post('/ratings', ratings.createRating);

module.exports = router;