const ratService = require("../services/ratService");

exports.getRats = async (req, res) => {
    try {
        const params = req.query;
        const data = await ratService.fetchRatings(params);

        if (!data || !data.items || data.items.length === 0) {
            return res.status(404).json({ message: "No ratings found" });
        }

        return res.json(data);
    } catch (error) {
        console.error("Error fetching ratings:", error);
        return res.status(500).json({ error: "Internal Server Error" });
    }
};

exports.createRating = async (req, res) => {
    try {
        const ratingData = req.body;

        const newRating = await ratService.createRating(ratingData);

        return res.status(201).json(newRating);
    } catch (error) {
        console.error("Error creating rating:", error);
        return res.status(500).json({ error: "Internal Server Error" });
    }
};