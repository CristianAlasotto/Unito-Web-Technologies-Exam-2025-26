var express = require('express');
var router = express.Router();
const profileController = require('../controllers/profileController');

/**
 * @swagger
 * path:
 *   /profile/{username}:
 *     get:
 *       summary: Mostra il profilo utente.
 *       description: Renderizza la pagina profilo per lo username specificato.
 *       tags:
 *         - Profile
 *       parameters:
 *         - in: path
 *           name: username
 *           required: true
 *           schema:
 *             type: string
 *           description: Username dell’utente.
 *       responses:
 *         200:
 *           description: Pagina profilo renderizzata.
 *         404:
 *           description: Utente non trovato.
 */
router.get('/:username', profileController.showProfile);

/**
 * @swagger
 * path:
 *   /profile:
 *     get:
 *       summary: Reindirizza al login.
 *       description: Reindirizza alla pagina di login utenti.
 *       tags:
 *         - Profile
 *       responses:
 *         302:
 *           description: Reindirizzamento a /users/login.
 */
router.get('/', function(req, res) {
    res.redirect('/users/login');
});

module.exports = router;
