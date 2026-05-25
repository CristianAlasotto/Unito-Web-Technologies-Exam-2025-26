var express = require('express');
var router = express.Router();
const { apiPostgres } = require('../controllers/apiClients');

/**
 * @swagger
 * /users/login:
 *   get:
 *       summary: Shows the login page.
 *       description: Renders the user login form.
 *       tags:
 *         - Users
 *       responses:
 *         200:
 *           description: Login page rendered.
 */
/**
 * Renders the user login form.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @param {Function} next Express next middleware.
 * @returns {void}
 */
router.get('/login', function(req, res, next) {
  res.render('profile/login', {
    title: 'Login',
    currentPage: 'profile'
  });
});

/**
 * @swagger
 * /users/login:
 *   post:
 *       summary: Logs the user in.
 *       description: Checks whether the username exists and redirects to the profile.
 *       tags:
 *         - Users
 *       requestBody:
 *         required: true
 *         content:
 *           application/x-www-form-urlencoded:
 *             schema:
 *               type: object
 *               properties:
 *                 username:
 *                   type: string
 *       responses:
 *         302:
 *           description: Redirect to the user profile.
 *         200:
 *           description: Login failed and the form was rendered with an error.
 */
/**
 * Validates a submitted username and redirects to the matching profile.
 *
 * @param {Object} req Express request containing username.
 * @param {Object} res Express response.
 * @param {Function} next Express next middleware.
 * @returns {Promise<void>} Resolves when the redirect or rendered response is sent.
 */
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
