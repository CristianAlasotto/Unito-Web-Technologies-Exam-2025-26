const { apiMongo, apiPostgres } = require('./apiClients.js');

const SORT_OPTIONS = [
	{ value: '', label: 'Default' },
	{ value: '-favorites', label: 'Più popolari' },
	{ value: 'favorites', label: 'Meno popolari' },
	{ value: 'name', label: 'Nome A-Z' },
	{ value: '-name', label: 'Nome Z-A' }
];

const buildFiltersModel = (query, favMaxLimit) => {
	const favMinValue = Number.isFinite(Number(query.favMin)) ? Number(query.favMin) : 0;
	const activeSort = query.sort || '';
	const activeMonth = query.birthMonth || '';
	const activeSearch = query.character_mal_id || query.q || '';

	return {
		character_mal_id: activeSearch,
		favMin: favMinValue,
		favMaxLimit,
		hasAbout: query.hasAbout === '1',
		hasImage: query.hasImage === '1',
		sortOptions: SORT_OPTIONS.map((option) => ({
			...option,
			selected: option.value === activeSort
		}))
	};
};

exports.list = async (req, res, next) => {
	try {
		const page = parseInt(req.query.page || '1', 10);
		const pageSize = parseInt(req.query.pageSize || '45', 10);
		const params = new URLSearchParams();

		const searchTerm = req.query.character_mal_id || req.query.q;
		if (searchTerm) params.set('search', searchTerm);
		if (req.query.favMin) params.set('favMin', req.query.favMin);
		if (req.query.hasImage === '1') params.set('hasImage', '1');
		if (req.query.birthMonth) params.set('birthMonth', req.query.birthMonth);
		if (req.query.sort) params.set('sort', req.query.sort);

		params.set('page', String(page));
		params.set('pageSize', String(pageSize));

		const response = await apiMongo.get(`/api/characters?${params.toString()}`);
		const characters = response.data.items || [];
		const totalPages = response.data.totalPages || 1;
		const favMaxLimit = response.data.favMaxLimit || response.data.maxFavorites || 50000;
		const filters = buildFiltersModel(req.query, favMaxLimit);

		const paginationQuery = new URLSearchParams();
		Object.entries(req.query).forEach(([key, value]) => {
			if (!value) return;
			if (key === 'page') return;
			paginationQuery.set(key, value);
		});
		const filtersQuery = paginationQuery.toString() ? `&${paginationQuery.toString()}` : '';

		res.render('characters/characters_list', {
			title: 'Personaggi',
			characters: characters,
			filters,
			filtersQuery,
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
			filters: buildFiltersModel({}, 50000),
			filtersQuery: '',
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
