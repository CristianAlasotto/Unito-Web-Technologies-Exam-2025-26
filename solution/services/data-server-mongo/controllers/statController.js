const statService = require('../services/statService');

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

exports.getStatById = async (req, res) => {
    try {
        const { id } = req.params;

        const data = await statService.fetchStats({ mal_id: id });

        if (!data || data.length === 0) {
            return res.status(404).json({ message: "Statistic not found" });
        }

        return res.json(data[0]);
    } catch (error) {
        console.error("Error fetching stat by ID:", error);
        return res.status(500).json({ error: "Internal Server Error" });
    }
};