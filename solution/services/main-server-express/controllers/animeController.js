/**
 * Anime controller for the main Express server of the Anime Database project.
 *
 * Responsibilities:
 * - handles anime list and anime detail web routes
 * - validates and normalizes filter/pagination query parameters
 * - renders Handlebars views for list, detail, characters and recommendations
 * - coordinates data access between PostgreSQL-backed and MongoDB-backed services
 *
 * This controller keeps business logic lightweight and delegates heavy
 * data operations to dedicated backend data servers.
 */

const { apiMongo, apiPostgres } = require('./apiClients.js');
const {
  buildFiltersQuery,
  buildPagination,
  formatValue,
  normalizeList,
  withSelectedOptions
} = require('./controllerUtils.js');

const SORT_OPTIONS = [
	{ value: '', label: 'Default' },
	{ value: 'popularity', label: 'Most Popular' },
	{ value: '-popularity', label: 'Less Popular' },
	{ value: 'title', label: 'Name A-Z' },
	{ value: '-title', label: 'Name Z-A' }
];

const TYPE_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: 'CM', label: 'CM' },
  { value: 'Movie', label: 'Movie' },
  { value: 'Music', label: 'Music' },
  { value: 'CM', label: 'CM' },
  { value: 'ONA', label: 'ONA' },
  { value: 'OVA', label: 'OVA' },
  { value: 'PV', label: 'PV' },
  { value: 'Special', label: 'Special' },
  { value: 'TV', label: 'TV' },
  { value: 'TVSpecial', label: 'TVSpecial' },
  { value: '[Adventure]', label: 'Adventure' },
  { value: '[Mecha]', label: 'Mecha' }
];

const RATING_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: 'G - All Ages', label: 'G - All Ages' },
  { value: 'PG - Children', label: 'PG - Children' },
  { value: 'PG-13 - Teens 13 or older', label: 'PG-13 - Teens 13 or older' },
  { value: 'R - 17+ (violence & profanity)', label: 'R - 17+ (violence & profanity)' },
  { value: 'R+ - Mild Nudity', label: 'R+ - Mild Nudity' },
  { value: 'Rx - Hentai', label: 'Rx - Hentai' }
];

const GENRE_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: 'action', label: 'Action' },
  { value: 'adventure', label: 'Adventure' },
  { value: 'avant garde', label: 'Avant Garde' },
  { value: 'award winning', label: 'Award Winning' },
  { value: 'boys love', label: 'Boys Love' },
  { value: 'comedy', label: 'Comedy' },
  { value: 'drama', label: 'Drama' },
  { value: 'ecchi', label: 'Ecchi' },
  { value: 'erotica', label: 'Erotica' },
  { value: 'fantasy', label: 'Fantasy' },
  { value: 'girls love', label: 'Girls Love' },
  { value: 'gourmet', label: 'Gourmet' },
  { value: 'hentai', label: 'Hentai' },
  { value: 'horror', label: 'Horror' },
  { value: 'mystery', label: 'Mystery' },
  { value: 'romance', label: 'Romance' },
  { value: 'sci fi', label: 'Sci-Fi' },
  { value: 'slice of life', label: 'Slice of Life' },
  { value: 'sports', label: 'Sports' },
  { value: 'supernatural', label: 'Supernatural' },
  { value: 'suspense', label: 'Suspense' }
];

/**
 * Normalizes related character API payloads into a character array.
 *
 * @param {Array<Object>|Object|null|undefined} payload Raw API response payload.
 * @returns {Array<Object>} Normalized related character list.
 */
const normalizeRelatedCharactersPayload = (payload) => {
  if (!payload) {
    return [];
  }
  if (Array.isArray(payload)) {
    return payload;
  }
  return payload.related_characters || payload.characters || payload.items || [];
};

/**
 * Builds the UI filter model used by the anime list template.
 *
 * @param {Record<string, string|undefined>} query Incoming request query object.
 * @returns {Object} Filter model with selected options for each dropdown.
 */
const buildFiltersModel = (query) => {
  const activeSort = query.sort || '';
  const activeSearch = query.search || query.q || '';
  const activeType = query.type || '';
  const activeYear = query.year || '';
  const activeRating = query.rating || '';
  const activeGenres = query.genres || query.genre || '';

  return {
    search: activeSearch,
    year: activeYear,
    sortOptions: withSelectedOptions(SORT_OPTIONS, activeSort),
    typeOptions: withSelectedOptions(TYPE_OPTIONS, activeType),
    ratingOptions: withSelectedOptions(RATING_OPTIONS, activeRating),
    genreOptions: withSelectedOptions(GENRE_OPTIONS, activeGenres)
  };
};

/**
 * Renders the paginated anime list, applying query filters and sort options.
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
    if (req.query.type) params.set('type', req.query.type);
    if (req.query.year) params.set('year', req.query.year);
    if (req.query.rating) params.set('rating', req.query.rating);
    if (req.query.genres) params.set('genres', req.query.genres);
    if (!req.query.genres && req.query.genre) params.set('genres', req.query.genre);
    if (req.query.sort) params.set('sort', req.query.sort);

    params.set('page', String(page));
    params.set('pageSize', String(pageSize));

    const response = await apiPostgres.get(`/api/details?${params.toString()}`);
    const animes = response.data.items;
    const totalPages = response.data.totalPages;
    const filters = buildFiltersModel(req.query);

    const filtersQuery = buildFiltersQuery(req.query, {
      exclude: ['status', 'source', 'episode', 'episodes'],
      aliases: { genre: 'genres' }
    });

    res.render('anime/anime_list', {
      title: 'Anime',
      animes: animes,
      filters,
      filtersQuery,
      pagination: buildPagination(page, totalPages),
      warning: !animes || animes.length === 0 ? 'No anime found in database.' : null
    });
  } catch (err) {
    res.render('anime/anime_list', {
      title: 'Anime',
      animes: [],
      filters: buildFiltersModel(req.query),
      filtersQuery: '',
      currentPage: 'anime',
      error: 'Impossibile caricare i dati degli anime. Il server potrebbe non essere disponibile.'
    });
  }
};

/**
 * Renders the anime detail page with related characters and recommendations.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @param {Function} next Express next middleware function.
 * @returns {Promise<void>} Resolves when the response is rendered.
 */
exports.detail = async (req, res, next) => {
  try {
    const { id } = req.params;
    const page = parseInt(req.query.page) || 1;
    const limit = 20;

    const filters = {
      minScore: req.query.minScore ? parseInt(req.query.minScore) : null,
      maxScore: req.query.maxScore ? parseInt(req.query.maxScore) : null,
      status: req.query.status || null,
      rewatching: req.query.rewatching || null,
      sortBy: req.query.sortBy || 'score',
      sortOrder: req.query.sortOrder || 'desc'
    };

    const response = await apiPostgres.get(`/api/details/${id}`);
    const charactersResponse = await apiPostgres.get(`/api/details/${id}/characters`);
    const recommendationsResponse = await apiPostgres.get(
        `/api/details/${id}/recommendations`
    );
    const raw = response.data || {};
    const relatedCharacters = normalizeRelatedCharactersPayload(charactersResponse?.data);
    const recommendationsData = recommendationsResponse?.data;
    const recommendations = Array.isArray(recommendationsData)
        ? recommendationsData
        : recommendationsData?.recommendations || [];

    // Ratings are loaded asynchronously on the client after page load.
    let ratings = null;
    let totalPages = 1;

    /**
     * Builds a query string preserving current rating filters for pagination links.
     *
     * @param {number|string} pageNum Destination page number.
     * @returns {string} Query string starting with '?'.
     */
    const buildQueryString = (pageNum) => {
      const params = new URLSearchParams({ page: pageNum });
      if (filters.minScore !== null) params.append('minScore', filters.minScore);
      if (filters.maxScore !== null) params.append('maxScore', filters.maxScore);
      if (filters.status) params.append('status', filters.status);
      if (filters.rewatching) params.append('rewatching', filters.rewatching);
      if (filters.sortBy) params.append('sortBy', filters.sortBy);
      if (filters.sortOrder) params.append('sortOrder', filters.sortOrder);
      return '?' + params.toString();
    };

    const genres = normalizeList(raw.genres);
    const themes = normalizeList(raw.themes);
    const studios = normalizeList(raw.studios);
    const demographics = normalizeList(raw.demographics);
    const producers = normalizeList(raw.producers);
    const licensors = normalizeList(raw.licensors);
    const streaming = normalizeList(raw.streaming);
    const explicitGenres = normalizeList(raw.explicit_genres);

    const anime = {
      ...raw,
      genres,
      themes,
      studios,
      demographics,
      producers,
      licensors,
      streaming,
      explicit_genres: explicitGenres,
      score_display: formatValue(raw.score),
      scored_by_display: formatValue(raw.scored_by),
      type_display: formatValue(raw.type),
      status_display: formatValue(raw.status),
      episodes_display: formatValue(raw.episodes),
      episodes_text:
          raw.episodes === null || raw.episodes === undefined || raw.episodes === ''
              ? 'N/A'
              : `${raw.episodes} episodes`,
      season_display: formatValue(raw.season),
      year_display: formatValue(raw.year),
      rating_display: formatValue(raw.rating),
      source_display: formatValue(raw.source),
      start_date_display: formatValue(raw.start_date),
      end_date_display: formatValue(raw.end_date),
      rank_display: formatValue(raw.rank),
      popularity_display: formatValue(raw.popularity),
      members_display: formatValue(raw.members),
      favorites_display: formatValue(raw.favorites),
      mal_id_display: formatValue(raw.mal_id),
      has_genres: genres.length > 0,
      has_themes: themes.length > 0,
      has_studios: studios.length > 0,
      has_demographics: demographics.length > 0,
      has_producers: producers.length > 0,
      has_licensors: licensors.length > 0,
      has_streaming: streaming.length > 0,
      has_explicit_genres: explicitGenres.length > 0,
      related_characters: relatedCharacters,
      has_related_characters: relatedCharacters.length > 0,
      recommendations,
      hasRelatedAnimes: recommendations.length > 0
    };

    res.render('anime/anime_detail', {
      title: raw.title,
      anime,
      ratings: ratings,
      filters: filters,
      ratingsPagination: {
        ...buildPagination(page, totalPages),
        prevUrl: buildQueryString(page - 1),
        nextUrl: buildQueryString(page + 1)
      },
      currentPage: 'anime'
    });
  } catch (err) {
    console.error('Error in detail controller:', err);
    res.render('anime/anime_detail', {
      title: 'Anime Detail',
      anime: null,
      error: 'Unable to load anime details.'
    });
  }
};

/**
 * Returns characters related to a specific anime as JSON.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @returns {Promise<void>} Resolves when the JSON response is sent.
 */
exports.characters = async (req, res) => {
  try {
    const { id } = req.params;
    const params = new URLSearchParams();
    if (req.query.page) params.set('page', req.query.page);
    if (req.query.pageSize) params.set('pageSize', req.query.pageSize);

    const queryString = params.toString();
    const response = await apiPostgres.get(
        `/api/details/${id}/characters${queryString ? `?${queryString}` : ''}`
    );
    const payload = response.data || {};
    const relatedCharacters = normalizeRelatedCharactersPayload(payload);
    const responsePayload = Array.isArray(payload) ? {} : payload;

    return res.json({
      ...responsePayload,
      characters: relatedCharacters,
      related_characters: responsePayload.related_characters || relatedCharacters
    });
  } catch (err) {
    return res.status(err.response?.status || 500).json({
      characters: [],
      related_characters: [],
      error: err.response?.data?.error || 'Unable to load characters'
    });
  }
};

/**
 * Renders the page with recommendations for a specific anime.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @returns {Promise<void>} Resolves when the response is rendered.
 */
exports.reccomendations = async (req, res) => {
  try {
    const { id } = req.params;
    const response = await apiPostgres.get(`/api/details/${id}/recommendations`);
    res.render('anime/anime_recommendations', {
      title: `Recommendations for ${response.data.title}`,
      recommendations: response.data.recommendations,
      currentPage: 'anime'
    });
  } catch (err) {
    res.render('anime/anime_recommendations', {
      title: 'Recommendations',
      recommendations: null,
      error: 'Unable to load recommendations.'
    });
  }
};

/**
 * Returns ratings data as JSON for asynchronous loading in the detail page.
 *
 * @param {Object} req Express request.
 * @param {Object} res Express response.
 * @returns {Promise<void>} Resolves when the JSON response is sent.
 */
exports.getRatingsJson = async (req, res) => {
  try {
    const { id } = req.params;
    const page = parseInt(req.query.page || '1', 10);
    const pageSize = parseInt(req.query.pageSize || '20', 10);

    // Get filter parameters
    const filters = {
      minScore: req.query.minScore ? parseInt(req.query.minScore) : null,
      maxScore: req.query.maxScore ? parseInt(req.query.maxScore) : null,
      status: req.query.status || null,
      rewatching: req.query.rewatching || null,
      sortBy: req.query.sortBy || 'score',
      sortOrder: req.query.sortOrder || 'desc'
    };

    const params = new URLSearchParams();
    params.set('anime_id', id);

    // Add filters to query params
    if (filters.status) {
      params.set('status', filters.status);
    }

    if (filters.rewatching) {
      params.set('is_rewatching', filters.rewatching === 'true' ? '1' : '0');
    }

    if (filters.minScore !== null) {
      params.set('minScore', String(filters.minScore));
    }

    if (filters.maxScore !== null) {
      params.set('maxScore', String(filters.maxScore));
    }

    // Build sort string for MongoDB
    if (filters.sortBy) {
      const sortPrefix = filters.sortOrder === 'desc' ? '-' : '';
      params.set('sort', sortPrefix + filters.sortBy);
    }

    // Use standard pagination via MongoDB
    params.set('page', String(page));
    params.set('pageSize', String(pageSize));

    const response = await apiMongo.get(`/api/ratings?${params.toString()}`);
    const data = response.data || {};
    const ratings = data.items || [];
    const totalPages = data.totalPages || 1;
    const total = data.total || 0;

    res.json({
      ratings: ratings,
      pagination: {
        currentPage: page,
        totalPages: totalPages,
        total: total
      }
    });
  } catch (err) {
    console.error('Error fetching ratings:', err.message);
    res.status(500).json({
      ratings: [],
      pagination: { currentPage: 1, totalPages: 1, total: 0 },
      error: 'Unable to load ratings'
    });
  }
};
