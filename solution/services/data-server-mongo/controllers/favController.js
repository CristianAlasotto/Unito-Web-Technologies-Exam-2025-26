const favService = require("../services/favService");

/**
 * Controller to handle HTTP requests for Favorites.
 * Acts as an interface between the API routes and the business logic service.
 *
 * @module controllers/favController
 */

/**
 * Handles the HTTP GET request to retrieve favorite items.
 * Extracts query parameters from the request, calls the service, and sends the response.
 *
 * @async
 * @function getFavs
 * @param {import('express').Request} req - The Express request object.
 * @param {Object} req.query - The query string parameters (filters, pagination, sorting).
 * @param {import('express').Response} res - The Express response object.
 * @returns {Promise<void>} Sends a JSON response with the data or an error status.
 *
 * @example
 * // Request: GET /api/favorites?username=Otaku123&sort=-id
 * // Response: 200 JSON Array
 */
exports.getFavs = async (req, res) => {
    try {
        // Extract parameters from the query string (e.g. ?page=1&limit=10)
        const params = req.query;

        // Delegate logic to the service layer
        const data = await favService.fetchFavorites(params);

        // Handle case where no data is found
        if (!data || data.length === 0) {
            return res.status(404).json({ message: "No favorites found" });
        }

        // Send the JSON response back to the browser with HTTP 200 OK
        return res.json(data);
    } catch (error) {
        // Log the internal error for debugging
        console.error("Error fetching favorites:", error);
        // Return a generic 500 Internal Server Error to the client
        return res.status(500).json({ error: "Internal Server Error" });
    }
};