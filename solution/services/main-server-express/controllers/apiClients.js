/* 
	NB LEGGERE LE PORTE DA .env
*/

const axios = require('axios');

const apiMongo = axios.create({
  baseURL: process.env.DATA_EXPRESS_URL || 'http://localhost:3001'   // server Express per MongoDB
});

const apiPostgres = axios.create({
  baseURL: process.env.DATA_SPRING_URL || 'http://localhost:8080'   // server Spring Boot per Postgres
});

// Intercettore per verificare la connessione
apiMongo.interceptors.response.use(
	response => response,
	error => {
		console.error('Errore connessione MongoDB:', error.message);
		return Promise.reject(error);
	}
);

apiPostgres.interceptors.response.use(
	response => response,
	error => {
		console.error('Errore connessione Postgres:', error.message);
		return Promise.reject(error);
	}
);

module.exports = { apiMongo, apiPostgres };
