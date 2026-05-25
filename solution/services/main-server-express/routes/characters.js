const express = require('express');
const charactersController = require('../controllers/charactersController');
const router = express.Router();

/**
 * @swagger
 * /characters:
 *   get:
 *       summary: Returns the character list.
 *       description: Retrieves the character list with optional filters.
 *       tags:
 *         - Characters
 *       parameters:
 *         - in: query
 *           name: search
 *           required: false
 *           schema:
 *             type: string
 *           description: Text to search for in character names.
 *       responses:
 *         200:
 *           description: Character list.
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
 *       summary: Returns character details.
 *       description: Retrieves detailed information for the character identified by {id}.
 *       tags:
 *         - Characters
 *       parameters:
 *         - in: path
 *           name: id
 *           required: true
 *           schema:
 *             type: string
 *           description: Character ID.
 *       responses:
 *         200:
 *           description: Character details.
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
 *           description: Character not found.
 */
router.get('/:id', charactersController.detail);

module.exports = router;
