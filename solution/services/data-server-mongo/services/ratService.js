const Ratings = require('../models/Ratings');
const Stats = require('../models/Stats');

/**
 * Service to handle business logic for Ratings.
 * Includes fetching ratings with advanced filtering (ranges) and updating aggregated statistics.
 *
 * @module services/ratService
 */

/**
 * Fetches ratings from the database with support for range queries and pagination.
 *
 * @async
 * @function fetchRatings
 * @param {Object} params - The query parameters.
 * @param {string} [params.fields] - Comma-separated fields to select.
 * @param {string} [params.sort] - Comma-separated sort string.
 * @param {number} [params.limit] - Limit per page.
 * @param {number} [params.page] - Page number.
 * @param {number} [params.minScore] - Filter for scores >= this value.
 * @param {number} [params.maxScore] - Filter for scores <= this value.
 * @param {...Object} [params.filters] - Other direct match filters (e.g., anime_id).
 * @returns {Promise<Object>} Object containing the list of items, total count, and total pages.
 */
exports.fetchRatings = async (params) => {
    // Destructure minScore and maxScore out so they aren't treated as direct filters immediately
    let { fields, sort, limit, pageSize, offset, page, minScore, maxScore, ...filters } = params;

    // Convert numeric fields from strings to numbers to ensure database type matching
    const numericFields = ['anime_id', 'score', 'is_rewatching', 'num_watched_episodes'];
    numericFields.forEach(field => {
        if (filters[field] !== undefined) {
            const num = parseInt(filters[field]);
            if (!isNaN(num)) {
                filters[field] = num;
            }
        }
    });

    // Add Range Query for Score (MongoDB $gte and $lte operators)
    if (minScore !== undefined || maxScore !== undefined) {
        filters.score = {};
        if (minScore !== undefined) filters.score.$gte = parseInt(minScore);
        if (maxScore !== undefined) filters.score.$lte = parseInt(maxScore);
    }

    let query = Ratings.find(filters);

    if (fields) {
        query = query.select(fields.split(',').join(' '));
    }

    if (sort) {
        query = query.sort(sort.split(',').join(' '));
    }

    const finalLimit = parseInt(pageSize || limit || 20);
    const finalSkip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    query = query.limit(finalLimit).skip(finalSkip);

    // Execute the query
    const items = await query.lean().exec();

    let total = null;
    let totalPages = null;

    // Calculate total count only if filters are applied (optimization)
    if (Object.keys(filters).length > 0) {
        total = await Ratings.countDocuments(filters);
        totalPages = Math.ceil(total / finalLimit);
    }

    return {
        items: items,
        total: total,
        totalPages: totalPages
    };
};

/**
 * Creates a new rating and automatically triggers an update of the global anime statistics.
 *
 * @async
 * @function createRating
 * @param {Object} data - The rating data to insert.
 * @param {Number} data.anime_id - The ID of the anime.
 * @param {String} data.status - Watch status (e.g. 'completed').
 * @param {Number} data.score - User score (1-10).
 * @returns {Promise<Object>} An object containing the new weighted average score of the anime.
 */
exports.createRating = async (data) => {

    await Ratings.create(data);

    const avg = await updateStats(data.anime_id, data.status, data.score);

    return { new_average_score: avg };
};

/**
 * Updates the aggregated statistics for an anime when a new rating is added.
 * Increments vote counts, recalculates percentages for each score (1-10), and computes the weighted average.
 *
 * @async
 * @function updateStats
 * @private
 * @param {Number} mal_id - The anime ID to update.
 * @param {String} status - The status field to increment (e.g., 'watching', 'completed').
 * @param {Number} score - The score to register (1-10).
 * @returns {Promise<Number>} The newly calculated weighted average score.
 */
async function updateStats(mal_id, status, score) {
    const inc = { [status]: 1, total: 1 };
    if (score > 0) inc[`score_${score}_votes`] = 1;

    // 1. Atomic increment of the vote counts and status counts
    const stats = await Stats.findOneAndUpdate({ mal_id }, { $inc: inc }, { upsert: true, new: true });

    // 2. Calculate the weighted average score
    let totalVotes = 0, sum = 0;
    for (let i = 1; i <= 10; i++) {
        const v = stats[`score_${i}_votes`] || 0;
        totalVotes += v;
        sum += v * i;
    }

    // 3. Recalculate percentages for every score bucket (1-10)
    const percentages = {};
    for (let i = 1; i <= 10; i++) {
        percentages[`score_${i}_percentage`] = totalVotes ? parseFloat(((stats[`score_${i}_votes`] / totalVotes) * 100).toFixed(2)) : 0;
    }

    // 4. Save the new percentages to the database
    await Stats.updateOne({ mal_id }, { $set: percentages });

    // Return the weighted average
    return totalVotes ? parseFloat((sum / totalVotes).toFixed(2)) : 0;
}