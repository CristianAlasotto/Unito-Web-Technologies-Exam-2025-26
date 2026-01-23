const favService = require("../services/favService");

exports.getFavs = async (req, res) => {
    try {
        const data = await favService.getAll(req.query);
        res.json(data);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getUserFavorites = async (req, res) => {
    try {
        const data = await favService.getByUser(req.params.username);
        res.json(data);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getAnimeFavCount = async (req, res) => {
    try {
        const count = await favService.getCountByAnime(req.params.animeId);
        res.json({ anime_id: req.params.animeId, favorites_count: count });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getPopularAnime = async (req, res) => {
    try {
        const data = await favService.getPopular(req.query.n);
        res.json(data);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};