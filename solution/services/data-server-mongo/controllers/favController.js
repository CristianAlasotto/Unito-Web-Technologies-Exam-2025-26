const favService = require("../services/favService");

exports.getFavs = async (req, res) => {
    try {
        // Extract parameters from the query string (e.g. ?page=1&limit=10)
        const params = req.query;

        const data = await favService.fetchFavorites(params);

        if (!data || data.length === 0) {
            return res.status(404).json({ message: "No favorites found" });
        }

        // Send the JSON response back to the browser
        return res.json(data);
    } catch (error) {
        console.error("Error fetching favorites:", error);
        return res.status(500).json({ error: "Internal Server Error" });
    }
};