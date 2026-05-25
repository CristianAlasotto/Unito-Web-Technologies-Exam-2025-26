/**
 * Characters controller for list and detail pages.
 *
 * Responsibilities:
 * - validates and forwards character filters/pagination to backend services
 * - renders list and detail templates for characters
 * - enriches detail pages with related anime and voice actor information
 */

const { apiPostgres } = require('./apiClients.js');
const {
	buildFiltersQuery,
	buildPagination,
	formatValue,
	withSelectedOptions
} = require('./controllerUtils.js');

const SORT_OPTIONS = [
	{ value: '', label: 'Default' },
	{ value: '-favorites', label: 'Most Popular' },
	{ value: 'favorites', label: 'Less Popular' },
	{ value: 'name', label: 'Name A-Z' },
	{ value: '-name', label: 'Name Z-A' }
];

/**
 * Builds UI filter state for the character list template.
 *
 * @param {Record<string, string|undefined>} query Request query object.
 * @param {number} favMaxLimit Maximum favorites value for range controls.
 * @returns {Object} Filter model consumed by the view.
 */
const buildFiltersModel = (query, favMaxLimit) => {
	const favMinValue = Number.isFinite(Number(query.favMin)) ? Number(query.favMin) : 0;
	const activeSort = query.sort || '';
	const activeSearch = query.character_mal_id || query.q || '';

	return {
		character_mal_id: activeSearch,
		favMin: favMinValue,
		favMaxLimit,
		hasAbout: query.hasAbout === '1',
		hasImage: query.hasImage === '1',
		sortOptions: withSelectedOptions(SORT_OPTIONS, activeSort)
	};
};

/**
 * Renders the paginated characters list.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @param {Function} next Express next middleware function.
 * @returns {Promise<void>} Resolves when the response is rendered.
 */
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

		const response = await apiPostgres.get(`/api/characters?${params.toString()}`);
		const characters = response.data.items || [];
		const totalPages = response.data.totalPages || 1;
		const favMaxLimit = response.data.favMaxLimit || response.data.maxFavorites || 50000;
		const filters = buildFiltersModel(req.query, favMaxLimit);

		const filtersQuery = buildFiltersQuery(req.query);

		res.render('characters/characters_list', {
			title: 'Personaggi',
			characters: characters,
			filters,
			filtersQuery,
			pagination: buildPagination(page, totalPages),
			warning: !characters || characters.length === 0 ? 'Nessun personaggio trovato nel database.' : null
		});
	} catch (err) {
		res.render('characters/characters_list', {
			title: 'Personaggi',
			characters: [],
			filters: buildFiltersModel(req.query, 50000),
			filtersQuery: '',
			currentPage: 'characters',
			error: 'Impossibile caricare i dati dei personaggi. Il server potrebbe non essere disponibile.'
		});
	}
};

/**
 * Renders the character detail page with related anime and voice actors.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @param {Function} next Express next middleware function.
 * @returns {Promise<void>} Resolves when the response is rendered.
 */
exports.detail = async (req, res, next) => {
	try {
		const { id } = req.params;
		const [characterResponse, animeResponse, voiceActorsResponse] = await Promise.all([
			apiPostgres.get(`/api/characters/${id}`),
			apiPostgres.get(`/api/characters/${id}/details`),
			apiPostgres.get(`/api/characters/${id}/voice_actors`)
		]);
		const raw = characterResponse.data || {};
		const animePayload = animeResponse.data || {};
		const voiceActorsPayload = voiceActorsResponse.data || {};

		const characterInfo = [
			{ label: 'Kanji name', value: formatValue(raw.name_kanji) },
			{ label: 'Favorites', value: formatValue(raw.favorites) },
			{
				label: 'MyAnimeList',
				value: formatValue(raw.url),
				isLink: Boolean(raw.url),
				href: raw.url
			},
		];
		const animeList = Array.isArray(animePayload.anime) ? animePayload.anime : [];
		const voiceActorsList = Array.isArray(voiceActorsPayload.voice_actors)
			? voiceActorsPayload.voice_actors
			: [];
		const voiceActors = voiceActorsList.map((actor) => ({
			...actor,
			name_display: formatValue(actor.name),
			given_name_display: formatValue(actor.given_name),
			family_name_display: formatValue(actor.family_name),
			birthday_display: formatValue(actor.birthday),
			favorites_display: formatValue(actor.favorites)
		}));
		res.render('characters/detail', {
			title: raw.name || 'Character Detail',
			character: {
				...raw,
				name_display: formatValue(raw.name),
				name_kanji_display: formatValue(raw.name_kanji),
				favorites_display: formatValue(raw.favorites),
				about_display: formatValue(raw.about)
			},
			anime: {
				hasRelatedAnimes: animeList.length > 0,
				recommendations: animeList
			},
			voiceActors: {
				hasVoiceActors: voiceActors.length > 0,
				items: voiceActors
			},
			characterInfo,
			currentPage: 'characters'
		});
	} catch (err) {
		next(err);
	}
};
