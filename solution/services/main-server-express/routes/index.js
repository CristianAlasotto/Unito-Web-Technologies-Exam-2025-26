const express = require('express');
const router = express.Router();
const homeController = require('../controllers/homeController');

/**
 * @swagger
 * /:
 *   get:
 *       summary: Shows the preview page.
 *       description: Renders the service home/preview page.
 *       tags:
 *         - Home
 *       responses:
 *         200:
 *           description: Preview page rendered.
 */
router.get('/', homeController.preview);

module.exports = router;
