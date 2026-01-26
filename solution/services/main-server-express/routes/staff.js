const express = require('express');
const staffController = require('../controllers/staffController.js');
const router = express.Router();

router.get('/', staffController.list);
router.get('/:id', staffController.detail);

module.exports = router;
