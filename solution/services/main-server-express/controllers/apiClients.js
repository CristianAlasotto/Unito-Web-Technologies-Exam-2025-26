/**
 * Shared API clients for the main Express server.
 *
 * Responsibilities:
 * - creates Axios instances for MongoDB and PostgreSQL data services
 * - configures request/response logging for debugging and timing
 * - normalizes connection error logging for upstream failures
 *
 * Business logic is not implemented here. This module only exposes
 * configured HTTP clients used by route controllers.
 */

const axios = require('axios');

/**
 * Attaches request/response logging interceptors to an Axios instance.
 *
 * @param {string} clientName Human-readable client name for log prefixes.
 * @param {import('axios').AxiosInstance} instance Axios instance to configure.
 * @returns {void}
 */
function setupLogging(clientName, instance) {

    // REQUEST
    instance.interceptors.request.use(config => {
        config.metadata = { startTime: Date.now() };

        console.log(`[${clientName}] → REQUEST`);
        console.log(`  ${config.method.toUpperCase()} ${config.baseURL}${config.url}`);

        if (config.data) {
            console.log(`  Body:`, JSON.stringify(config.data).slice(0, 500));
        }

        return config;
    });

    // RESPONSE
    instance.interceptors.response.use(
        response => {
            const duration = Date.now() - response.config.metadata.startTime;

            console.log(`[${clientName}] ← RESPONSE`);
            console.log(`  ${response.status} ${response.config.url}`);
            console.log(`  Time: ${duration} ms`);

            return response;
        },
        error => {
            const config = error.config || {};
            const duration = config.metadata
                ? Date.now() - config.metadata.startTime
                : 'N/A';

            console.error(`[${clientName}] ✖ ERROR`);
            console.error(`  ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
            console.error(`  Time: ${duration} ms`);
            console.error(`  Message: ${error.message}`);

            if (error.response) {
                console.error(`  Status: ${error.response.status}`);
                console.error(`  Body:`, error.response.data);
            }

            return Promise.reject(error);
        }
    );
}

const apiMongo = axios.create({
  baseURL: process.env.DATA_EXPRESS_URL || 'http://localhost:3001'   // server MongoDB REST
});

const apiPostgres = axios.create({
    baseURL: process.env.DATA_SPRING_URL || 'http://localhost:8080'   // server Spring Boot per Postgres
});

setupLogging('Mongo-API', apiMongo);
setupLogging('Postgres-API', apiPostgres);

apiMongo.interceptors.response.use(
    response => response,
    error => {
        console.error('main-server-express: Connection Error MongoDB:', error.message);
        return Promise.reject(error);
    }
);

apiPostgres.interceptors.response.use(
    response => response,
    error => {
        console.error('main-server-express: Connection Error Postgres:', error.message);
        return Promise.reject(error);
    }
);

module.exports = { apiMongo, apiPostgres };