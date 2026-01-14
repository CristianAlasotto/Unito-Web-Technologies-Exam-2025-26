import axios from 'axios';

// Toggle API logs via LOG_API_ENABLED environment variable (default: true)
const LOG_API_ENABLED = (process.env.LOG_API_ENABLED || 'true').toLowerCase() === 'true';

/**
 * Attaches request and response interceptors to log API calls
 * @param {Object} client - axios instance
 * @returns {Object} axios instance with interceptors attached
 */
const attachInterceptors = (client) => {
  if (!LOG_API_ENABLED) {
    return client;
  }

  // Log outgoing requests
  client.interceptors.request.use((config) => {
    console.log(`[OUT] ${config.method?.toUpperCase()} ${config.baseURL || ""}${config.url}`);
    return config;
  });

  // Log incoming responses and errors
  client.interceptors.response.use(
    // Success response handler
    (res) => {
      console.log(`[IN ] ${res.status} ${res.config.method?.toUpperCase()} ${res.config.baseURL || ""}${res.config.url}`);
      return res;
    },
    // Error response handler
    (err) => {
      const status = err.response?.status;
      console.log(`[ERR] ${status || "NO_STATUS"} ${err.config?.method?.toUpperCase()} ${err.config?.baseURL || ""}${err.config?.url}`);
      throw err;
    }
  );

  return client;
};

// Generic API client with 10s timeout
const api = attachInterceptors(axios.create({ timeout: 10000 }));

// Data Express API client (Express backend service)
const dataExpressApi = attachInterceptors(
  axios.create({
    baseURL: process.env.DATA_EXPRESS_URL || 'http://localhost:3001',
    timeout: 10000,
  })
);

// Data Spring API client (Spring backend service)
const dataSpringApi = attachInterceptors(
  axios.create({
    baseURL: process.env.DATA_SPRING_URL || 'http://localhost:8080',
    timeout: 10000,
  })
);

export { api, dataExpressApi, dataSpringApi };