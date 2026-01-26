const { apiPostgres } = require('./apiClients.js');

// Lista di tutti i personaggi
exports.list = async (req, res, next) => {
	try {
		const response = await apiPostgres.get('/api/characters');
		const characters = response.data;
		res.render('characters/list', {
			title: 'Personaggi',
			characters: characters,
			currentPage: 'characters',
			warning: !characters || characters.length === 0 ? 'Nessun personaggio trovato nel database.' : null
		});
	} catch (err) {
		res.render('characters/list', {
			title: 'Personaggi',
			characters: [],
			currentPage: 'characters',
			error: 'Impossibile caricare i dati dei personaggi. Il server potrebbe non essere disponibile.'
		});
	}
};

// Dettaglio di un personaggio
exports.detail = async (req, res, next) => {
	try {
		const { id } = req.params;
		const response = await apiPostgres.get(`/api/characters/${id}`);
		res.render('characters/detail', {
			title: response.data.name,
			character: response.data,
			currentPage: 'characters'
		});
	} catch (err) {
		next(err);
	}
};
