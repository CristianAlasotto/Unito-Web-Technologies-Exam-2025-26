const Favs = require('../models/Favs'); 
const NodeCache = require('node-cache');

const favCache = new NodeCache({ stdTTL: 60 });

/**
 * Invalidates all cache entries. Called after any write operation
 * (create, update, delete) to prevent stale data being served.
 */
const invalidateCache = () => {
    console.log('[CACHE INVALIDATED] favCache flushed');
    favCache.flushAll();
};

/**
 * Controller to handle HTTP requests for Favorites.
 * Acts as the direct interface between the API routes and the Mongoose database layer.
 *
 * @module controllers/favController
 */

/**
 * Handles the HTTP GET request to retrieve favorite items.
 * Extracts query parameters from the request, dynamically builds the Mongoose query, 
 * optimizes the query footprint, and sends a highly performant JSON response.
 *
 * @async
 * @function getFavs
 * @param {Object} req - The Express request object.
 * @param {Object} req.query - The query string parameters (filters, pagination, sorting).
 * @param {Object} res - The Express response object.
 * @returns {Promise<void>} Sends a JSON response with the data or an error status.
 *
 * @example
 * // Request: GET /api/favorites?username=Otaku123&sort=-id
 * // Response: 200 JSON Array
 */
exports.getFavs = async (req, res) => {
    try {
        const cacheKey = req.originalUrl;
        const cachedData = favCache.get(cacheKey);

        if (cachedData) {
            console.log(`[CACHE HIT] ${cacheKey}`);
            return res.json(cachedData);
        }

        const params = req.query;
        let { fields, sort, limit, pageSize, offset, page, ...filters } = params;

        let query = Favs.find(filters);

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
            return res.status(404).json({ message: "No favorites found" });
        }

        console.log(`[CACHE MISS] ${cacheKey}`);
        favCache.set(cacheKey, data);

        return res.json(data);
    } catch (error) {
        console.error("Error fetching favorites directly within controller:", error);
        
        return res.status(500).json({ 
            error: "Internal Server Error", 
            message: error.message 
        });
    }
};

/**
 * Handles the HTTP POST request to create a new favorite item.
 * Invalidates the cache after a successful write.
 *
 * @async
 * @function createFav
 * @param {Object} req - The Express request object.
 * @param {Object} req.body - The favorite item data.
 * @param {Object} res - The Express response object.
 * @returns {Promise<void>} Sends a 201 JSON response with the created item or an error status.
 *
 * @example
 * // Request: POST /api/favorites
 * // Body: { username: "Otaku123", itemId: "abc123" }
 * // Response: 201 JSON Object
 */
exports.createFav = async (req, res) => {
    try {
        const newFav = new Favs(req.body);
        const saved = await newFav.save();

        invalidateCache();

        return res.status(201).json(saved);
    } catch (error) {
        console.error("Error creating favorite:", error);

        return res.status(500).json({ 
            error: "Internal Server Error", 
            message: error.message 
        });
    }
};

/**
 * Handles the HTTP PUT request to update an existing favorite item by ID.
 * Invalidates the cache after a successful write.
 *
 * @async
 * @function updateFav
 * @param {Object} req - The Express request object.
 * @param {Object} req.params.id - The ID of the favorite to update.
 * @param {Object} req.body - The updated data.
 * @param {Object} res - The Express response object.
 * @returns {Promise<void>} Sends a JSON response with the updated item or an error status.
 *
 * @example
 * // Request: PUT /api/favorites/64abc123
 * // Body: { itemId: "newItem456" }
 * // Response: 200 JSON Object
 */
exports.updateFav = async (req, res) => {
    try {
        const updated = await Favs.findByIdAndUpdate(
            req.params.id,
            req.body,
            { new: true, runValidators: true }
        );

        if (!updated) {
            return res.status(404).json({ message: "Favorite not found" });
        }

        invalidateCache();

        return res.json(updated);
    } catch (error) {
        console.error("Error updating favorite:", error);

        return res.status(500).json({ 
            error: "Internal Server Error", 
            message: error.message 
        });
    }
};

/**
 * Handles the HTTP DELETE request to remove a favorite item by ID.
 * Invalidates the cache after a successful write.
 *
 * @async
 * @function deleteFav
 * @param {Object} req - The Express request object.
 * @param {Object} req.params.id - The ID of the favorite to delete.
 * @param {Object} res - The Express response object.
 * @returns {Promise<void>} Sends a 204 response or an error status.
 *
 * @example
 * // Request: DELETE /api/favorites/64abc123
 * // Response: 204 No Content
 */
exports.deleteFav = async (req, res) => {
    try {
        const deleted = await Favs.findByIdAndDelete(req.params.id);

        if (!deleted) {
            return res.status(404).json({ message: "Favorite not found" });
        }

        invalidateCache();

        return res.status(204).send();
    } catch (error) {
        console.error("Error deleting favorite:", error);

        return res.status(500).json({ 
            error: "Internal Server Error", 
            message: error.message 
        });
    }
};
