var express = require('express');
var { MOCK_FAVOURITES } = require('../lib/mockDb');
var router = express.Router();

router.get('/', function(req, res, next) {
  const username = req.query.user || 'cristian';
  const favData = MOCK_FAVOURITES[username];
  if (!favData) return next();
  res.render('favourites/list', { title: 'Favourites', favourites: favData.items, currentPage: 'favourites' });
});

module.exports = router;