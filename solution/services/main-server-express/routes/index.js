const express = require('express');
const router = express.Router();
const homeController = require('../controllers/homeController');

/**
 * @swagger
 * path:
 *   /:
 *     get:
 *       summary: Mostra la pagina di anteprima.
 *       description: Renderizza la home/preview del servizio.
 *       tags:
 *         - Home
 *       responses:
 *         200:
 *           description: Pagina di anteprima renderizzata.
 */
router.get('/', homeController.preview);

module.exports = router;
