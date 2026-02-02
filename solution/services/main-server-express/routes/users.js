var express = require('express');
var router = express.Router();
const { apiPostgres } = require('../controllers/apiClients');

router.get('/login', function(req, res, next) {
  res.render('profile/login', {
    title: 'Login',
    currentPage: 'profile'
  });
});

router.post('/login', async function(req, res, next) {
  const { username } = req.body;

  try {
    await apiPostgres.get(`/api/profiles/${username}`);
    res.redirect(`/profile/${username}`);
  } catch (err) {
    res.render('profile/login', {
      title: 'Login',
      currentPage: 'profile',
      error: 'User not found. Please check your username.',
      username: username
    });
  }
});

module.exports = router;