const Favs = require('../models/Favs'); // Import the correct model

/**
 * Service to handle business logic for retrieving favorites.
 * Supports advanced query features like pagination, sorting, field selection, and filtering.
 *
 * @module services/favService
 */

/**
 * Fetches favorite items from the MongoDB database based on dynamic query parameters.
 *
 * @async
 * @function fetchFavorites
 * @param {Object} params - The query parameters object from the HTTP request.
 * @param {string} [params.fields] - Comma-separated list of fields to include in the result (e.g., "username,id").
 * @param {string} [params.sort] - Comma-separated list of fields to sort by. Use '-' for descending order (e.g., "-id").
 * @param {string|number} [params.limit] - The maximum number of items to return (default: 20).
 * @param {string|number} [params.pageSize] - Alias for 'limit'.
 * @param {string|number} [params.page] - The current page number (1-based index).
 * @param {string|number} [params.offset] - The number of items to skip (ignored if 'page' is provided).
 * @param {...Object} [params.filters] - Any other properties in 'params' are treated as direct filters on the database fields (e.g., username="John").
 * @returns {Promise<Array<Object>>} A promise that resolves to an array of favorite objects (plain JS objects, lean).
 */
exports.fetchFavorites = async (params) => {
    // Extract special control parameters, leaving the rest as filter criteria
    let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

    // Initialize the Mongoose query with the filters
    let query = Favs.find(filters);

    // Apply field selection (Projection)
    if (fields) {
        query = query.select(fields.split(',').join(' '));
    }

    // Apply sorting
    if (sort) {
        query = query.sort(sort.split(',').join(' '));
    }

    // Calculate Pagination logic
    const finalLimit = parseInt(pageSize || limit || 20);
    const finalSkip = page ? (parseInt(page) - 1) * finalLimit : parseInt(offset || 0);

    // Apply limit and skip to the query
    query = query.limit(finalLimit).skip(finalSkip);

    // Execute query and return plain JavaScript objects (lean) for performance
    return await query.lean().exec();
};