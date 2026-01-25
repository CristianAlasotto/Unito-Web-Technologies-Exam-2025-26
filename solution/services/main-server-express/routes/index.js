var express = require('express');
var { MOCK_ANIME_HOME } = require('../lib/mockDb');

var router = express.Router();

// Home shows the same featured anime grid as the Anime landing page
router.get('/', function(req, res) {
  res.render('index', {
    title: 'Home',
    currentPage: 'anime',
    animes: MOCK_ANIME_HOME,
    layout: 'layout/main'
  });
});

module.exports = router;