const express = require('express');
const router = express.Router();

const favorites = require('../controllers/favController');
const statistics = require('../controllers/statController');
const ratings = require('../controllers/ratController');

router.get('/favorites', favorites.getFavs);
router.get('/statistics', statistics.getStats);
router.get('/statistics/:id', statistics.getStatById);
router.get('/ratings', ratings.getRats);
router.post('/ratings', ratings.createRating);

module.exports = router;