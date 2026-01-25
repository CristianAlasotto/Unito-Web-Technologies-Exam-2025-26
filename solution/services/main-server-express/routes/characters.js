var express = require('express');
var { MOCK_CHARACTERS_LIST, MOCK_CHARACTERS_DETAIL } = require('../lib/mockDb');
var router = express.Router();

router.get('/', function(req, res) {
  res.render('characters/list', { title: 'Characters', characters: MOCK_CHARACTERS_LIST, currentPage: 'characters' });
});

router.get('/:id', function(req, res, next) {
  const character = MOCK_CHARACTERS_DETAIL[req.params.id];
  if (!character) return next();
  res.render('characters/detail', { title: character.name, character, currentPage: 'characters' });
});

module.exports = router;