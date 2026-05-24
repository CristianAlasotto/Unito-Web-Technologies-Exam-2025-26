// Direct Access: Import the database models directly into the controller file
const Ratings = require('../models/Ratings');
const NodeCache = require('node-cache');
const { updateStats } = require('./statController');

const ratCache = new NodeCache({ stdTTL: 60 });

const invalidateCache = () => {
    console.log('[CACHE INVALIDATED] ratCache flushed');
    ratCache.flushAll();
};
exports.invalidateCache = invalidateCache;

/**
 * Controller to handle HTTP requests for Ratings.
 * Interacts directly with Mongoose models (Model-Controller style layout).
 *
 * @module controllers/ratController
 */

/**
 * GET /api/ratings
 * Retrieves ratings based on query parameters.
 * Handles parameters, parsing, ranges, and explicit pagination boundaries.
 *
 * @async
 * @function getRats
 * @param {Object} req - Express request object.
 * @param {Object} res - Express response object.
 * @returns {Promise<void>} JSON response with rating data or error.
 */
exports.getRats = async (req, res) => {
    try {
        const cacheKey = req.originalUrl;
        const cachedData = ratCache.get(cacheKey);

        if (cachedData) {
            console.log(`[CACHE HIT] ${cacheKey}`);
            return res.json(cachedData);
        }

        const params = req.query;
        
        let { fields, sort, limit, pageSize, offset, page, minScore, maxScore, ...filters } = params;

        const numericFields = ['anime_id', 'score', 'is_rewatching', 'num_watched_episodes'];
        numericFields.forEach(field => {
            if (filters[field] !== undefined) {
                const num = parseInt(filters[field]);
                if (!isNaN(num)) {
                    filters[field] = num;
                }
            }
        });

        if (minScore !== undefined || maxScore !== undefined) {
            filters.score = {};
            if (minScore !== undefined) filters.score.$gte = parseInt(minScore);
            if (maxScore !== undefined) filters.score.$lte = parseInt(maxScore);
        }

        let query = Ratings.find(filters);

        if (fields) {
            query = query.select(fields.split(',').join(' '));
        } else {
            query = query.select('-__v');
        }

        if (sort) {
            query = query.sort(sort.split(',').join(' '));
        }

        const finalLimit = parseInt(pageSize || limit || 20);
        const finalSkip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

        query = query.limit(finalLimit).skip(finalSkip);

        const items = await query.lean().exec();

        if (!items || items.length === 0) {
            return res.status(404).json({ message: "No ratings found" });
        }

        let total = null;
        let totalPages = null;

        if (Object.keys(filters).length > 0) {
            total = await Ratings.countDocuments(filters);
            totalPages = Math.ceil(total / finalLimit);
        }

        const result = {
            items: items,
            total: total,
            totalPages: totalPages
        };

        console.log(`[CACHE MISS] ${cacheKey}`);
        ratCache.set(cacheKey, result);

        return res.json(result);
    } catch (error) {
        console.error("Error fetching ratings directly in controller:", error);
        return res.status(500).json({ error: "Internal Server Error", message: error.message });
    }
};

/**
 * POST /api/ratings
 * Creates a new rating and automatically triggers an update of the global anime statistics.
 * Expects a JSON body with anime_id, status, and score.
 *
 * @async
 * @function createRating
 * @param {Object} req - Express request object.
 * @param {Object} res - Express response object.
 * @returns {Promise<void>} JSON response with the new average score.
 */
exports.createRating = async (req, res) => {
    try {
        const ratingData = req.body;

        await Ratings.create(ratingData);

        invalidateCache();

        const avg = await updateStats(ratingData.anime_id, ratingData.status, ratingData.score);

        return res.status(201).json({ new_average_score: avg });
    } catch (error) {
        console.error("Error creating rating directly in controller:", error);
        return res.status(500).json({ error: "Internal Server Error", message: error.message });
    }
};

