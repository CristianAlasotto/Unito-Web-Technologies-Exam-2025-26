var express = require('express');
var { MOCK_PROFILE } = require('../lib/mockDb');
var router = express.Router();

router.get('/', function(req, res, next) {
  const username = req.query.user || 'cristian';
  const profile = MOCK_PROFILE[username];
  if (!profile) return next();
  res.render('profile/profile', { title: 'Profile', profile, currentPage: 'profile' });
});

module.exports = router;