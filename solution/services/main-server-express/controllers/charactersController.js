const { apiPostgres } = require('./apiClients.js');

exports.list = async (req, res, next) => {
	try {
		const page = req.query.page || 1;
		const pageSize = req.query.pageSize || 45;
		const response = await apiPostgres.get(`/api/characters?page=${page}&pageSize=${pageSize}`);
		const characters = response.data.items;
		const totalPages = response.data.totalPages;

		res.render('characters/characters_list', {
			title: 'Personaggi',
			characters: characters,
			pagination: {
				currentPage: page,
				totalPages: totalPages,
				hasPrev: page > 1,
				prevPage: page - 1,
				hasNext: page < totalPages,
				nextPage: parseInt(page) + 1
			},
			warning: !characters || characters.length === 0 ? 'Nessun personaggio trovato nel database.' : null
		});
	} catch (err) {
		res.render('characters/characters_list', {
			title: 'Personaggi',
			characters: [],
			currentPage: 'characters',
			error: 'Impossibile caricare i dati dei personaggi. Il server potrebbe non essere disponibile.'
		});
	}
};

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
