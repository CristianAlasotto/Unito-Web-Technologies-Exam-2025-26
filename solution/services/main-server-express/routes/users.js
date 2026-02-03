var express = require('express');
var router = express.Router();
const { apiPostgres } = require('../controllers/apiClients');

/**
 * @swagger
 * path:
 *   /users/login:
 *     get:
 *       summary: Mostra la pagina di login.
 *       description: Renderizza il form di login utenti.
 *       tags:
 *         - Users
 *       responses:
 *         200:
 *           description: Pagina di login renderizzata.
 */
router.get('/login', function(req, res, next) {
  res.render('profile/login', {
    title: 'Login',
    currentPage: 'profile'
  });
});

/**
 * @swagger
 * path:
 *   /users/login:
 *     post:
 *       summary: Effettua il login.
 *       description: Verifica l’esistenza dello username e reindirizza al profilo.
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
 *           description: Reindirizzamento al profilo utente.
 *         200:
 *           description: Login fallito e form renderizzato con errore.
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
