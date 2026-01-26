const statService = require('../services/statService');

exports.getStats = async (req, res) => {
    try {
        const data = await statService.fetchStats(req.query);
        res.status(200).json({
            status: "success",
            result: data.length,
            data: data
        });
    } catch (err) {
        res.status(500).json({status: "error", message: err.message});
    }
}