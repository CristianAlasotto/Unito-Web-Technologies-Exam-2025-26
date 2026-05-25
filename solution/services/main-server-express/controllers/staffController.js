/**
 * Staff controller for list and detail pages.
 *
 * Responsibilities:
 * - applies search, city and sorting filters for staff listing
 * - renders list and detail templates for staff members
 * - enriches detail pages with linked anime works
 */

const { apiPostgres } = require('./apiClients.js');
const {
	buildFiltersQuery,
	buildPagination,
	formatValue,
	withSelectedOptions
} = require('./controllerUtils.js');

const CITIES_OPTIONS = [
  { value: '', label: 'Tutte' },
  { value: 'Berlin', label: 'Berlin' },
  { value: 'Cape Town', label: 'Cape Town' },
  { value: 'Chicago', label: 'Chicago' },
  { value: 'Houston', label: 'Houston' },
  { value: 'London', label: 'London' },
  { value: 'Los Angeles', label: 'Los Angeles' },
  { value: 'Madrid', label: 'Madrid' },
  { value: 'Mexico City', label: 'Mexico City' },
  { value: 'Mumbai', label: 'Mumbai' },
  { value: 'Nagoya', label: 'Nagoya' },
  { value: 'New York', label: 'New York' },
  { value: 'Osaka', label: 'Osaka' },
  { value: 'Paris', label: 'Paris' },
  { value: 'Rome', label: 'Rome' },
  { value: 'San Francisco', label: 'San Francisco' },
  { value: 'Sapporo', label: 'Sapporo' },
  { value: 'Sydney', label: 'Sydney' },
  { value: 'São Paulo', label: 'São Paulo' },
  { value: 'Tokyo', label: 'Tokyo' },
  { value: 'Yokohama', label: 'Yokohama' },
];

const SORT_OPTIONS = [
  { value: '', label: 'Default' },
  { value: '-favorites', label: 'Most Popular' },
  { value: 'favorites', label: 'Less Popular' },
  { value: 'name', label: 'Name A-Z' },
  { value: '-name', label: 'Name Z-A' }
];

/**
 * Builds UI filter state for the staff list template.
 *
 * @param {Record<string, string|undefined>} query Request query object.
 * @returns {Object} Filter model consumed by the view.
 */
const buildFiltersModel = (query) => {
  const activeSearch = query.search || query.q || '';
  const activeCity = query.city || '';
  const activeSort = query.sort || '';

  return {
    search: activeSearch,
    cityOptions: withSelectedOptions(CITIES_OPTIONS, activeCity),
    sortOptions: withSelectedOptions(SORT_OPTIONS, activeSort)
  };
};

/**
 * Renders the paginated staff list.
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

		const searchTerm = req.query.search || req.query.q;
		if (searchTerm) params.set('search', searchTerm);
		if (req.query.position) params.set('position', req.query.position);
		if (req.query.city) params.set('city', req.query.city);
		if (req.query.sort) params.set('sort', req.query.sort);
		params.set('page', String(page));
		params.set('pageSize', String(pageSize));

		const response = await apiPostgres.get(`/api/person_details?${params.toString()}`);
		const staff = response.data.items;
		const totalPages = response.data.totalPages;
		const filters = buildFiltersModel(req.query);

		const filtersQuery = buildFiltersQuery(req.query);

		res.render('staff/staff_list', {
			title: 'Staff',
			staff: staff,
			filters,
			filtersQuery,
			pagination: buildPagination(page, totalPages),
			warning: !staff || staff.length === 0 ? 'Nessuno staff trovato nel database.' : null
		});
	} catch (err) {
		res.render('staff/staff_list', {
			title: 'Staff',
			staff: [],
			filters: buildFiltersModel(req.query),
			filtersQuery: '',
			currentPage: 'staff',
			error: 'Impossibile caricare i dati dello staff. Il server potrebbe non essere disponibile.'
		});
	}
};

/**
 * Renders the staff detail page with related anime works.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @param {Function} next Express next middleware function.
 * @returns {Promise<void>} Resolves when the response is rendered.
 */
exports.detail = async (req, res, next) => {
	try {
		const { id } = req.params;
		const [personResponse, worksResponse] = await Promise.all([
			apiPostgres.get(`/api/person_details/${id}`),
			apiPostgres.get(`/api/person_details/${id}/details`)
		]);
		const raw = personResponse.data || {};
		const worksPayload = worksResponse.data || {};
		const staffInfo = [
			{ label: 'Given name', value: formatValue(raw.given_name) },
			{ label: 'Family name', value: formatValue(raw.family_name) },
			{ label: 'Birthday', value: formatValue(raw.birthday) },
			{ label: 'Favorites', value: formatValue(raw.favorites) },
			{ label: 'City', value: formatValue(raw.city) },
			{
				label: 'Website',
				value: formatValue(raw.website_url),
				isLink: Boolean(raw.website_url),
				href: raw.website_url
			},
			{
				label: 'MyAnimeList',
				value: formatValue(raw.url),
				isLink: Boolean(raw.url),
				href: raw.url
			},
		];
		const animeList = Array.isArray(worksPayload.items) ? worksPayload.items : [];
		res.render('staff/detail', {
			title: raw.name || 'Staff Detail',
			staff: {
				...raw,
				name_display: formatValue(raw.name),
				given_name_display: formatValue(raw.given_name),
				family_name_display: formatValue(raw.family_name),
				birthday_display: formatValue(raw.birthday),
				favorites_display: formatValue(raw.favorites),
				city_display: formatValue(raw.city)
			},
			anime: {
				hasRelatedAnimes: animeList.length > 0,
				recommendations: animeList
			},
			staffInfo,
			currentPage: 'staff'
		});
	} catch (err) {
		next(err);
	}
};
