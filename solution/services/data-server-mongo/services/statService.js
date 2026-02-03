const Stats = require('../models/Stats');

/**
 * Service to handle business logic for retrieving Anime Statistics.
 * Provides generic query capabilities including filtering, sorting, and pagination.
 *
 * @module services/statService
 */

/**
 * Fetches statistics from the database based on dynamic query parameters.
 *
 * @async
 * @function fetchStats
 * @param {Object} params - The query parameters from the HTTP request.
 * @param {string} [params.fields] - Comma-separated list of fields to select (e.g., "mal_id,watching").
 * @param {string} [params.sort] - Comma-separated list of fields to sort by (e.g., "-total").
 * @param {string|number} [params.limit] - Max items per page.
 * @param {string|number} [params.pageSize] - Alias for 'limit'.
 * @param {string|number} [params.page] - Page number (1-based).
 * @param {string|number} [params.offset] - Skip count (ignored if 'page' is used).
 * @param {...Object} [params.filters] - Any other keys are used as direct filters (e.g., { mal_id: 55 }).
 * @returns {Promise<Array<Object>>} A promise that resolves to an array of statistic objects (lean).
 */
exports.fetchStats = async (params) => {
    // Extract generic query options, leaving the rest as filters
    let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

    // Initialize query with filters
    let query = Stats.find(filters);

    // Apply projection (select specific columns)
    if (fields) {
        query = query.select(fields.split(',').join(' '));
    }

    // Apply sorting
    if (sort) {
        query = query.sort(sort.split(',').join(' '));
    }

    // Calculate pagination parameters
    const finalLimit = parseInt(pageSize || limit || 20);
    const finalSkip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    // Apply pagination
    query = query.limit(finalLimit).skip(finalSkip);

    // Execute query returning plain objects for performance
    return await query.lean().exec();
};