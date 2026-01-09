const express = require('express');
const router = express.Router();
const ratingController = require('../controllers/ratingController');

router.get('/ratings', ratingController.getRatings);

module.exports = router;