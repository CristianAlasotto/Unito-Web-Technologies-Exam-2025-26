const ratService = require("../services/ratService");

exports.getRats = async (req, res) => {
    try {
        const data = await ratService.fetchRatings(req.query);
        res.status(200).json({
            status: "success",
            results: data.length,
            data: data
        });
    } catch (err) {
        res.status(500).json({ status: "error", message: err.message });
    }
};