const express = require('express');
const router = express.Router();
const favController = require('../controllers/favController');

// Standard fetch with filters/pagination
router.get('/favorites', favController.getFavs);

// Changed getFavById to getUserFavorites to match your controller
router.get('/favorites/user/:username', favController.getUserFavorites);

// Statistics and analysis
router.get('/favorites/anime/:animeId/count', favController.getAnimeFavCount);
router.get('/favorites/popular', favController.getPopularAnime);

module.exports = router;