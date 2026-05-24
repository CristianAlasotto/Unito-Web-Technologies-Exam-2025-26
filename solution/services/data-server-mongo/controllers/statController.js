// Direct Access: Connect the controller layer directly to the target Stats collection schema
const Stats = require('../models/Stats');
const NodeCache = require('node-cache');

const statCache = new NodeCache({ stdTTL: 60 });

const invalidateCache = () => {
    console.log('[CACHE INVALIDATED] statCache flushed');
    statCache.flushAll();
};
exports.invalidateCache = invalidateCache;

/**
 * Controller to handle HTTP requests for Anime Statistics.
 * Interacts directly with the Mongoose model (Model-Controller style layout).
 *
 * @module controllers/statController
 */

/**
 * GET /api/statistics
 * Retrieves a list of statistics based on query filters.
 *
 * @async
 * @function getStats
 * @param {Object} req - The Express request object.
 * @param {Object} res - The Express response object.
 * @returns {Promise<void>} JSON response with the list of stats or an error.
 */
exports.getStats = async (req, res) => {
    try {
        const cacheKey = req.originalUrl;
        const cachedData = statCache.get(cacheKey);

        if (cachedData) {
            console.log(`[CACHE HIT] ${cacheKey}`);
            return res.json(cachedData);
        }

        const params = req.query;
        let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

        let query = Stats.find(filters);

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

        const data = await query.lean().exec();

        if (!data || data.length === 0) {
            return res.status(404).json({ message: "No statistics found" });
        }

        console.log(`[CACHE MISS] ${cacheKey}`);
        statCache.set(cacheKey, data);

        return res.json(data);
    } catch (error) {
        console.error("Error fetching stats directly in controller:", error);
        return res.status(500).json({ error: "Internal Server Error", message: error.message });
    }
};

/**
 * GET /api/statistics/:id
 * Retrieves statistics for a specific anime by its MyAnimeList ID.
 *
 * @async
 * @function getStatById
 * @param {Object} req - The Express request object.
 * @param {string} req.params.id - The 'mal_id' of the anime to retrieve.
 * @param {Object} res - The Express response object.
 * @returns {Promise<void>} JSON response with the single stat object or an error.
 */
exports.getStatById = async (req, res) => {
    try {
        const { id } = req.params;
        const cacheKey = req.originalUrl;
        const cachedData = statCache.get(cacheKey);

        if (cachedData) {
            console.log(`[CACHE HIT] ${cacheKey}`);
            return res.json(cachedData);
        }

        const data = await Stats.find({ mal_id: id }).select('-__v').lean().exec();

        if (!data || data.length === 0) {
            return res.status(404).json({ message: "Statistic not found" });
        }

        console.log(`[CACHE MISS] ${cacheKey}`);
        statCache.set(cacheKey, data[0]);

        return res.json(data[0]);
    } catch (error) {
        console.error("Error fetching stat by ID directly in controller:", error);
        return res.status(500).json({ error: "Internal Server Error", message: error.message });
    }
};

/**
 * Updates statistics for a given anime after a new rating is created.
 * Increments parameters atomically, recalculates score percentages, and returns the new average.
 *
 * @async
 * @function updateStats
 * @param {Number} mal_id - The anime ID to update.
 * @param {String} status - The status field to increment (e.g., 'watching', 'completed').
 * @param {Number} score - The score to register (1-10).
 * @returns {Promise<Number>} The newly calculated weighted average score.
 */
exports.updateStats = async (mal_id, status, score) => {
    const inc = { [status]: 1, total: 1 };
    if (score > 0) inc[`score_${score}_votes`] = 1;

    const stats = await Stats.findOneAndUpdate({ mal_id }, { $inc: inc }, { upsert: true, new: true });

    let totalVotes = 0, sum = 0;
    for (let i = 1; i <= 10; i++) {
        const v = stats[`score_${i}_votes`] || 0;
        totalVotes += v;
        sum += v * i;
    }

    const percentages = {};
    for (let i = 1; i <= 10; i++) {
        percentages[`score_${i}_percentage`] = totalVotes ? parseFloat(((stats[`score_${i}_votes`] / totalVotes) * 100).toFixed(2)) : 0;
    }

    await Stats.updateOne({ mal_id }, { $set: percentages });

    invalidateCache();

    return totalVotes ? parseFloat((sum / totalVotes).toFixed(2)) : 0;
};