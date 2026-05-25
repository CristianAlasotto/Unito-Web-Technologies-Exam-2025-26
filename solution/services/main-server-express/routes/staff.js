const express = require('express');
const staffController = require('../controllers/staffController.js');
const router = express.Router();

/**
 * @swagger
 * /staff:
 *   get:
 *       summary: Returns the staff list.
 *       description: Retrieves the staff list with optional filters.
 *       tags:
 *         - Staff
 *       parameters:
 *         - in: query
 *           name: search
 *           required: false
 *           schema:
 *             type: string
 *           description: Text to search for in staff names.
 *       responses:
 *         200:
 *           description: Staff list.
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
 * /staff/{id}:
 *   get:
 *       summary: Returns staff member details.
 *       description: Retrieves detailed information for the staff member identified by {id}.
 *       tags:
 *         - Staff
 *       parameters:
 *         - in: path
 *           name: id
 *           required: true
 *           schema:
 *             type: string
 *           description: Staff member ID.
 *       responses:
 *         200:
 *           description: Staff details.
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
 *           description: Staff member not found.
 */
router.get('/:id', staffController.detail);

module.exports = router;
