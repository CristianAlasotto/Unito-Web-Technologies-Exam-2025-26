const express = require('express');
const animeController = require('../controllers/animeController');
const router = express.Router();

router.get('/', animeController.list);

router.get('/:id', animeController.detail);

router.get('/:id/recommendations', animeController.reccomendations);

module.exports = router;