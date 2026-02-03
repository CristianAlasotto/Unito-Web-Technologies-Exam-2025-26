const statService = require('../services/statService');

/**
 * Controller to handle HTTP requests for Anime Statistics.
 *
 * @module controllers/statController
 */

/**
 * GET /api/statistics
 * Retrieves a list of statistics based on query filters.
 *
 * @async
 * @function getStats
 * @param {import('express').Request} req - The Express request object.
 * @param {import('express').Response} res - The Express response object.
 * @returns {Promise<void>} JSON response with the list of stats or an error.
 */
exports.getStats = async (req, res) => {
    try {
        const params = req.query;
        const data = await statService.fetchStats(params);

        if (!data || data.length === 0) {
            return res.status(404).json({ message: "No statistics found" });
        }

        return res.json(data);
    } catch (error) {
        console.error("Error fetching stats:", error);
        return res.status(500).json({ error: "Internal Server Error" });
    }
};

/**
 * GET /api/statistics/:id
 * Retrieves statistics for a specific anime by its MyAnimeList ID.
 *
 * @async
 * @function getStatById
 * @param {import('express').Request} req - The Express request object.
 * @param {string} req.params.id - The 'mal_id' of the anime to retrieve.
 * @param {import('express').Response} res - The Express response object.
 * @returns {Promise<void>} JSON response with the single stat object or an error.
 */
exports.getStatById = async (req, res) => {
    try {
        const { id } = req.params;

        // Reuse the generic fetch service filtering by mal_id
        const data = await statService.fetchStats({ mal_id: id });

        if (!data || data.length === 0) {
            return res.status(404).json({ message: "Statistic not found" });
        }

        // Return the first (and only) result directly
        return res.json(data[0]);
    } catch (error) {
        console.error("Error fetching stat by ID:", error);
        return res.status(500).json({ error: "Internal Server Error" });
    }
};