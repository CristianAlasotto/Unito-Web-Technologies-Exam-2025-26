const express = require('express');
const router = express.Router();
const ratingController = require('../controllers/ratingController');
const statController = require("../controllers/statController");
const favController = require("../controllers/favController");

router.get('/getratings', ratingController.getRatings);
router.get('/getstats', statController.getStats);
router.get('/getfavs', favController.getFavs);

module.exports = router;