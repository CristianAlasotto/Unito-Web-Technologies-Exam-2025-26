import axios from 'axios';

// Toggle API logs via LOG_API_ENABLED (default: true)
const LOG_API_ENABLED = (process.env.LOG_API_ENABLED || 'true').toLowerCase() === 'true';

const attachInterceptors = (client) => {
  if (!LOG_API_ENABLED) {
    return client;
  }

  client.interceptors.request.use((config) => {
    console.log(`[OUT] ${config.method?.toUpperCase()} ${config.baseURL || ""}${config.url}`);
    return config;
  });

  client.interceptors.response.use(
    (res) => {
      console.log(`[IN ] ${res.status} ${res.config.method?.toUpperCase()} ${res.config.baseURL || ""}${res.config.url}`);
      return res;
    },
    (err) => {
      const status = err.response?.status;
      console.log(`[ERR] ${status || "NO_STATUS"} ${err.config?.method?.toUpperCase()} ${err.config?.baseURL || ""}${err.config?.url}`);
      throw err;
    }
  );

  return client;
};

const api = attachInterceptors(axios.create({ timeout: 10000 }));

const dataExpressApi = attachInterceptors(
  axios.create({
    baseURL: process.env.DATA_EXPRESS_URL || 'http://localhost:3001',
    timeout: 10000,
  })
);

const dataSpringApi = attachInterceptors(
  axios.create({
    baseURL: process.env.DATA_SPRING_URL || 'http://localhost:8080',
    timeout: 10000,
  })
);

export { api, dataExpressApi, dataSpringApi };