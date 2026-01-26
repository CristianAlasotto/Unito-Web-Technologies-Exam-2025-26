const express = require('express');
const router = express.Router();
const homeController = require('./homeController');

router.get('/', homeController.preview);

module.exports = router;