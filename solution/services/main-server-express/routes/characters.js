const express = require('express');
const charactersController = require('../controllers/charactersController');
const router = express.Router();

/**
 * @swagger
 * /characters:
 *   get:
 *       summary: Restituisce la lista dei personaggi.
 *       description: Recupera l’elenco dei personaggi con filtri opzionali.
 *       tags:
 *         - Characters
 *       parameters:
 *         - in: query
 *           name: search
 *           required: false
 *           schema:
 *             type: string
 *           description: Testo da ricercare nei nomi dei personaggi.
 *       responses:
 *         200:
 *           description: Lista di personaggi.
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
 *                     anime_id:
 *                       type: string
 */
router.get('/', charactersController.list);

/**
 * @swagger
 * /characters/{id}:
 *   get:
 *       summary: Restituisce i dettagli di un personaggio.
 *       description: Recupera le informazioni dettagliate del personaggio identificato da {id}.
 *       tags:
 *         - Characters
 *       parameters:
 *         - in: path
 *           name: id
 *           required: true
 *           schema:
 *             type: string
 *           description: ID del personaggio.
 *       responses:
 *         200:
 *           description: Dettaglio del personaggio.
 *           content:
 *             application/json:
 *               schema:
 *                 type: object
 *                 properties:
 *                   id:
 *                     type: string
 *                   name:
 *                     type: string
 *                   anime_id:
 *                     type: string
 *                   description:
 *                     type: string
 *         404:
 *           description: Personaggio non trovato.
 */
router.get('/:id', charactersController.detail);

module.exports = router;
