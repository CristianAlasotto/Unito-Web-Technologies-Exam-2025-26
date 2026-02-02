var express = require('express');
var router = express.Router();
const profileController = require('../controllers/profileController');

router.get('/:username', profileController.showProfile);

router.get('/', function(req, res) {
    res.redirect('/users/login');
});

module.exports = router;