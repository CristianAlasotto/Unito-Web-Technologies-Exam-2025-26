/**
 * Shared API clients for the main Express server.
 *
 * Responsibilities:
 * - creates Axios instances for MongoDB and PostgreSQL data services
 *
 * Business logic is not implemented here. This module only exposes
 * configured HTTP clients used by route controllers.
 */

const axios = require('axios');

/**
 * Axios client for the MongoDB-backed data server.
 *
 * @type {Object}
 */
const apiMongo = axios.create({
  baseURL: process.env.DATA_EXPRESS_URL || 'http://localhost:3001'   // server MongoDB REST
});

/**
 * Axios client for the Spring/PostgreSQL-backed data server.
 *
 * @type {Object}
 */
const apiPostgres = axios.create({
    baseURL: process.env.DATA_SPRING_URL || 'http://localhost:8080'   // server Spring Boot per Postgres
});

module.exports = { apiMongo, apiPostgres };
