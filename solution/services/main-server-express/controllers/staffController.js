const { apiMongo, apiPostgres } = require('./apiClients.js');

const POSITIONS_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: '2nd Key Animation', label: '2nd Key Animation' },
  { value: 'ADR Director', label: 'ADR Director' },
  { value: 'Animation Check', label: 'Animation Check' },
  { value: 'Animation Director', label: 'Animation Director' },
  { value: 'Art Director', label: 'Art Director' },
  { value: 'Assistant Animation Director', label: 'Assistant Animation Director' },
  { value: 'Assistant Director', label: 'Assistant Director' },
  { value: 'Assistant Engineer', label: 'Assistant Engineer' },
  { value: 'Assistant Producer', label: 'Assistant Producer' },
  { value: 'Assistant Production Coordinat', label: 'Assistant Production Coordinat' },
  { value: 'Associate Casting Director', label: 'Associate Casting Director' },
  { value: 'Associate Producer', label: 'Associate Producer' },
  { value: 'Background Art', label: 'Background Art' },
  { value: 'Casting Director', label: 'Casting Director' },
  { value: 'Character Design', label: 'Character Design' },
  { value: 'Chief Animation Director', label: 'Chief Animation Director' },
  { value: 'Chief Producer', label: 'Chief Producer' },
  { value: 'Co-Director', label: 'Co-Director' },
  { value: 'Co-Producer', label: 'Co-Producer' },
  { value: 'Color Design', label: 'Color Design' },
  { value: 'Color Setting', label: 'Color Setting' },
  { value: 'Creator', label: 'Creator' },
  { value: 'Dialogue Editing', label: 'Dialogue Editing' },
  { value: 'Digital Paint', label: 'Digital Paint' },
  { value: 'Director', label: 'Director' },
  { value: 'Director of Photography', label: 'Director of Photography' },
  { value: 'Editing', label: 'Editing' },
  { value: 'Episode Director', label: 'Episode Director' },
  { value: 'Executive Producer', label: 'Executive Producer' },
  { value: 'In-Between Animation', label: 'In-Between Animation' },
  { value: 'Inserted Song Performance', label: 'Inserted Song Performance' },
  { value: 'Key Animation', label: 'Key Animation' },
  { value: 'Layout', label: 'Layout' },
  { value: 'Mechanical Design', label: 'Mechanical Design' },
  { value: 'Music', label: 'Music' },
  { value: 'Online Editing Supervision', label: 'Online Editing Supervision' },
  { value: 'Online Editor', label: 'Online Editor' },
  { value: 'Original Character Design', label: 'Original Character Design' },
  { value: 'Original Creator', label: 'Original Creator' },
  { value: 'Planning', label: 'Planning' },
  { value: 'Planning Producer', label: 'Planning Producer' },
  { value: 'Post-Production Assistant', label: 'Post-Production Assistant' },
  { value: 'Principle Drawing', label: 'Principle Drawing' },
  { value: 'Producer', label: 'Producer' },
  { value: 'Production Assistant', label: 'Production Assistant' },
  { value: 'Production Coordination', label: 'Production Coordination' },
  { value: 'Production Manager', label: 'Production Manager' },
  { value: 'Publicity', label: 'Publicity' },
  { value: 'Re-Recording Mixing', label: 'Re-Recording Mixing' },
  { value: 'Recording', label: 'Recording' },
  { value: 'Recording Assistant', label: 'Recording Assistant' },
  { value: 'Recording Engineer', label: 'Recording Engineer' },
  { value: 'Screenplay', label: 'Screenplay' },
  { value: 'Script', label: 'Script' },
  { value: 'Series Composition', label: 'Series Composition' },
  { value: 'Series Production Director', label: 'Series Production Director' },
  { value: 'Setting', label: 'Setting' },
  { value: 'Setting Manager', label: 'Setting Manager' },
  { value: 'Sound Director', label: 'Sound Director' },
  { value: 'Sound Effects', label: 'Sound Effects' },
  { value: 'Sound Manager', label: 'Sound Manager' },
  { value: 'Sound Supervisor', label: 'Sound Supervisor' },
  { value: 'Special Effects', label: 'Special Effects' },
  { value: 'Spotting', label: 'Spotting' },
  { value: 'Storyboard', label: 'Storyboard' },
  { value: 'Theme Song Arrangement', label: 'Theme Song Arrangement' },
  { value: 'Theme Song Composition', label: 'Theme Song Composition' },
  { value: 'Theme Song Lyrics', label: 'Theme Song Lyrics' },
  { value: 'Theme Song Performance', label: 'Theme Song Performance' },
];

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
  { value: '-favorites', label: 'Più popolari' },
  { value: 'favorites', label: 'Meno popolari' },
  { value: 'name', label: 'Nome A-Z' },
  { value: '-name', label: 'Nome Z-A' }
];

const buildFiltersModel = (query) => {
  const activeSearch = query.search || query.q || '';
  const activePosition = query.position || '';
  const activeCity = query.relevant_location || '';
  const activeSort = query.sort || '';

  return {
    search: activeSearch,
    positionOptions: POSITIONS_OPTIONS.map((option) => ({
      ...option,
      selected: option.value === activePosition
    })),
    cityOptions: CITIES_OPTIONS.map((option) => ({
      ...option,
      selected: option.value === activeCity
    })),
    sortOptions: SORT_OPTIONS.map((option) => ({
      ...option,
      selected: option.value === activeSort
    }))
  };
};

// Lista di tutto lo staff
exports.list = async (req, res, next) => {
	try {
		const page = parseInt(req.query.page || '1', 10);
		const pageSize = parseInt(req.query.pageSize || '45', 10);
		const params = new URLSearchParams();

		const searchTerm = req.query.search || req.query.q;
		if (searchTerm) params.set('search', searchTerm);
		if (req.query.position) params.set('position', req.query.position);
		if (req.query.relevant_location) params.set('relevant_location', req.query.relevant_location);
		if (req.query.sort) params.set('sort', req.query.sort);
		params.set('page', String(page));
		params.set('pageSize', String(pageSize));

		const response = await apiPostgres.get(`/api/person_details?${params.toString()}`);
		const staff = response.data.items;
		const totalPages = response.data.totalPages;
		const filters = buildFiltersModel(req.query);

		const paginationQuery = new URLSearchParams();
		Object.entries(req.query).forEach(([key, value]) => {
			if (!value) return;
			if (key === 'page') return;
			paginationQuery.set(key, value);
		});
		const filtersQuery = paginationQuery.toString() ? `&${paginationQuery.toString()}` : '';

		res.render('staff/staff_list', {
			title: 'Staff',
			staff: staff,
			filters,
			filtersQuery,
			pagination: {
				currentPage: page,
				totalPages: totalPages,
				hasPrev: page > 1,
				prevPage: page - 1,
				hasNext: page < totalPages,
				nextPage: page + 1
			},
			warning: !staff || staff.length === 0 ? 'Nessuno staff trovato nel database.' : null
		});
	} catch (err) {
		res.render('staff/staff_list', {
			title: 'Staff',
			staff: [],
			filters: buildFiltersModel({}),
			filtersQuery: '',
			currentPage: 'staff',
			error: 'Impossibile caricare i dati dello staff. Il server potrebbe non essere disponibile.'
		});
	}
};

// Dettaglio di uno staff
exports.detail = async (req, res, next) => {
	try {
		const { id } = req.params;
		const response = await apiPostgres.get(`/api/person_details/${id}`);
		const raw = response.data || {};
		const formatValue = (value) =>
			value === null || value === undefined || value === '' ? 'N/A' : value;
		const staffInfo = [
			{ label: 'Given name', value: formatValue(raw.given_name) },
			{ label: 'Family name', value: formatValue(raw.family_name) },
			{ label: 'Birthday', value: formatValue(raw.birthday) },
			{ label: 'Favorites', value: formatValue(raw.favorites) },
			{ label: 'Relevant location', value: formatValue(raw.relevant_location) },
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
		res.render('staff/detail', {
			title: raw.name || 'Staff Detail',
			staff: {
				...raw,
				name_display: formatValue(raw.name),
				given_name_display: formatValue(raw.given_name),
				family_name_display: formatValue(raw.family_name),
				birthday_display: formatValue(raw.birthday),
				favorites_display: formatValue(raw.favorites),
				relevant_location_display: formatValue(raw.relevant_location)
			},
			staffInfo,
			currentPage: 'staff'
		});
	} catch (err) {
		next(err);
	}
};
