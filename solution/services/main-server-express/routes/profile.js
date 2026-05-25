var express = require('express');
var router = express.Router();
const profileController = require('../controllers/profileController');


router.get('/:username/ratings-json', profileController.getRatingsJson);

/**
 * @swagger
 * /profile/{username}:
 *   get:
 *       summary: Shows the user profile.
 *       description: Renders the profile page for the specified username.
 *       tags:
 *         - Profile
 *       parameters:
 *         - in: path
 *           name: username
 *           required: true
 *           schema:
 *             type: string
 *           description: User username.
 *       responses:
 *         200:
 *           description: Profile page rendered.
 *         404:
 *           description: User not found.
 */
router.get('/:username', profileController.showProfile);

/**
 * @swagger
 * /profile:
 *   get:
 *       summary: Redirects to login.
 *       description: Redirects to the user login page.
 *       tags:
 *         - Profile
 *       responses:
 *         302:
 *           description: Redirect to /users/login.
 */
/**
 * Redirects bare profile requests to the login page.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @returns {void}
 */
router.get('/', function(req, res) {
    res.redirect('/users/login');
});

module.exports = router;
