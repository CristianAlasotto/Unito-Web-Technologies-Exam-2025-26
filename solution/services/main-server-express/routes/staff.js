const express = require('express');
const staffController = require('../controllers/staffController.js');
const router = express.Router();

/**
 * @swagger
 * path:
 *   /staff:
 *     get:
 *       summary: Restituisce la lista dello staff.
 *       description: Recupera l’elenco dello staff con filtri opzionali.
 *       tags:
 *         - Staff
 *       parameters:
 *         - in: query
 *           name: search
 *           required: false
 *           schema:
 *             type: string
 *           description: Testo da ricercare nei nomi dello staff.
 *       responses:
 *         200:
 *           description: Lista dello staff.
 *           content:
 *             application/json:
 *               schema:
 *                 type: array
 *                 items:
 *                   type: object
 *                   properties:
 *                     id:
 *                       type: string
 *                     name:
 *                       type: string
 *                     role:
 *                       type: string
 */
router.get('/', staffController.list);

/**
 * @swagger
 * path:
 *   /staff/{id}:
 *     get:
 *       summary: Restituisce i dettagli di un membro dello staff.
 *       description: Recupera le informazioni dettagliate del membro dello staff identificato da {id}.
 *       tags:
 *         - Staff
 *       parameters:
 *         - in: path
 *           name: id
 *           required: true
 *           schema:
 *             type: string
 *           description: ID del membro dello staff.
 *       responses:
 *         200:
 *           description: Dettaglio dello staff.
 *           content:
 *             application/json:
 *               schema:
 *                 type: object
 *                 properties:
 *                   id:
 *                     type: string
 *                   name:
 *                     type: string
 *                   role:
 *                     type: string
 *                   biography:
 *                     type: string
 *         404:
 *           description: Membro dello staff non trovato.
 */
router.get('/:id', staffController.detail);

module.exports = router;
