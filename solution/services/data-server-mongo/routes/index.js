const express = require('express');
const router = express.Router();
const fCtrl = require('../controllers/favController');

// Standard REST paths
router.get('/favorites', fCtrl.getFavs);


module.exports = router;