var express = require('express');
var { MOCK_ANIME_LIST, MOCK_ANIME_DETAIL } = require('../lib/mockDb');
var router = express.Router();

router.get('/', function(req, res) {
  res.render('anime/anime_list', { 
    title: 'Anime', 
    animes: MOCK_ANIME_LIST, 
    currentPage: 'anime'
  });
});

router.get('/:id', function(req, res, next) {
  const anime = MOCK_ANIME_DETAIL[req.params.id];
  if (!anime) return next();
  res.render('anime/detail', { title: anime.title, anime, currentPage: 'anime' });
});

module.exports = router;