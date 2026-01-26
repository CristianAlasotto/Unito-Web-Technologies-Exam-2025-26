const express = require('express');
const generalTestController = require('../controllers/generalTestController');
const router = express.Router();

/* router.get('/', generalTestController.favs.list);
router.get('/', generalTestController.ragtings.list);
router.get('/', generalTestController.stats.list);

router.get('/:id', generalTestController.favs.list);
router.get('/:id', generalTestController.ragtings.list);
router.get('/:id', generalTestController.stats.list); */

// UNICO HANDLER (provvisorio)
router.get('/', generalTestController.overview);

module.exports = router;
