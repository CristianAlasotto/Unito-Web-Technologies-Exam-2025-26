const { apiMongo, apiPostgres } = require('./apiClients.js');

const SORT_OPTIONS = [
	{ value: '', label: 'Default' },
	{ value: '-popularity', label: 'Più popolari' },
	{ value: 'popularity', label: 'Meno popolari' },
	{ value: 'title', label: 'Nome A-Z' },
	{ value: '-title', label: 'Nome Z-A' }
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

const STATUS_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: 'Currently Airing', label: 'Currently Airing' },
  { value: 'Finished Airing', label: 'Finished Airing' },
  { value: 'Not yet aired', label: 'Not yet aired' },
  { value: '[Shin-Ei Animation]', label: 'Shin-Ei Animation' }
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

const SOURCE_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: '4-koma manga', label: '4-koma manga' },
  { value: 'Book', label: 'Book' },
  { value: 'Card game', label: 'Card game' },
  { value: 'Game', label: 'Game' },
  { value: 'Light novel', label: 'Light novel' },
  { value: 'Manga', label: 'Manga' },
  { value: 'Mixed media', label: 'Mixed media' },
  { value: 'Music', label: 'Music' },
  { value: 'Novel', label: 'Novel' },
  { value: 'Original', label: 'Original' },
  { value: 'Other', label: 'Other' },
  { value: 'Picture book', label: 'Picture book' },
  { value: 'Radio', label: 'Radio' },
  { value: 'Unknown', label: 'Unknown' },
  { value: 'Visual novel', label: 'Visual novel' },
  { value: 'Web manga', label: 'Web manga' },
  { value: 'Web novel', label: 'Web novel' },
];

const GENRE_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: 'action', label: 'Action' },
  { value: 'adventure', label: 'Adventure' },
  { value: 'avant-garde', label: 'Avant Garde' },
  { value: 'award-winning', label: 'Award Winning' },
  { value: 'boys-love', label: 'Boys Love' },
  { value: 'comedy', label: 'Comedy' },
  { value: 'drama', label: 'Drama' },
  { value: 'ecchi', label: 'Ecchi' },
  { value: 'erotica', label: 'Erotica' },
  { value: 'fantasy', label: 'Fantasy' },
  { value: 'girls-love', label: 'Girls Love' },
  { value: 'gourmet', label: 'Gourmet' },
  { value: 'hentai', label: 'Hentai' },
  { value: 'horror', label: 'Horror' },
  { value: 'mystery', label: 'Mystery' },
  { value: 'romance', label: 'Romance' },
  { value: 'sci-fi', label: 'Sci-Fi' },
  { value: 'slice-of-life', label: 'Slice of Life' },
  { value: 'sports', label: 'Sports' },
  { value: 'supernatural', label: 'Supernatural' },
  { value: 'suspense', label: 'Suspense' }
];

const buildFiltersModel = (query) => {
  const activeSort = query.sort || '';
  const activeSearch = query.search || query.q || '';
  const activeType = query.type || '';
  const activeYear = query.year || '';
  const activeStatus = query.status || '';
  const activeRating = query.rating || '';
  const activeSource = query.source || '';
  const activeEpisodes = query.episodes || '';
  const activeGenres = query.genres || query.genre || '';

  return {
    search: activeSearch,
    year: activeYear,
    episodes: activeEpisodes,
    sortOptions: SORT_OPTIONS.map((option) => ({
      ...option,
      selected: option.value === activeSort
    })),
    typeOptions: TYPE_OPTIONS.map((option) => ({
      ...option,
      selected: option.value === activeType
    })),
    statusOptions: STATUS_OPTIONS.map((option) => ({
      ...option,
      selected: option.value === activeStatus
    })),
    ratingOptions: RATING_OPTIONS.map((option) => ({
      ...option,
      selected: option.value === activeRating
    })),
    genreOptions: GENRE_OPTIONS.map((option) => ({
      ...option,
      selected: option.value === activeGenres
    })),
    sourceOptions: SOURCE_OPTIONS.map((option) => ({
      ...option,
      selected: option.value === activeSource
    }))
  };
};

exports.list = async (req, res, next) => {
  try {
    const page = parseInt(req.query.page || '1', 10);
    const pageSize = parseInt(req.query.pageSize || '45', 10);
    const params = new URLSearchParams();

    const searchTerm = req.query.search || req.query.q;
    if (searchTerm) params.set('search', searchTerm);
    if (req.query.type) params.set('type', req.query.type);
    if (req.query.year) params.set('year', req.query.year);
    if (req.query.status) params.set('status', req.query.status);
    if (req.query.rating) params.set('rating', req.query.rating);
    if (req.query.genres) params.set('genres', req.query.genres);
    if (!req.query.genres && req.query.genre) params.set('genres', req.query.genre);
    if (req.query.source) params.set('source', req.query.source);
    if (req.query.sort) params.set('sort', req.query.sort);
    if (req.query.episodes) {
      const episodes = parseInt(req.query.episodes, 10);
      if (!Number.isNaN(episodes)) {
        const normalized = Math.min(Math.max(episodes, 1), 3000);
        params.set('episodes', String(normalized));
      }
    }

    params.set('page', String(page));
    params.set('pageSize', String(pageSize));

    const response = await apiPostgres.get(`/api/details?${params.toString()}`);
    const animes = response.data.items;
    const totalPages = response.data.totalPages;
    const filters = buildFiltersModel(req.query);

    const paginationQuery = new URLSearchParams();
    Object.entries(req.query).forEach(([key, value]) => {
      if (!value) return;
      if (key === 'page') return;
      if (key === 'genre') {
        if (!req.query.genres) paginationQuery.set('genres', value);
        return;
      }
      paginationQuery.set(key, value);
    });
    const filtersQuery = paginationQuery.toString() ? `&${paginationQuery.toString()}` : '';

    res.render('anime/anime_list', {
      title: 'Anime',
      animes: animes,
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
      warning: !animes || animes.length === 0 ? 'No anime found in database.' : null
    });
  } catch (err) {
    res.render('anime/anime_list', {
      title: 'Anime',
      animes: [],
      filters: buildFiltersModel({}),
      filtersQuery: '',
      currentPage: 'anime',
      error: 'Impossibile caricare i dati degli anime. Il server potrebbe non essere disponibile.'
    });
  }
};

exports.detail = async (req, res, next) => {
  try {
    const { id } = req.params;
    const response = await apiPostgres.get(`/api/details/${id}`);
    const charactersResponse = await apiMongo.get('/api/characters?page=1&pageSize=12');
    const recommendationsResponse = await apiPostgres.get(
      `/api/details/${id}/recommendations`
    );
    const raw = response.data || {};
    const charactersData = charactersResponse?.data;
    const relatedCharacters = Array.isArray(charactersData)
      ? charactersData
      : charactersData?.items || charactersData?.related_characters || [];
    const recommendationsData = recommendationsResponse?.data;
    const recommendations = Array.isArray(recommendationsData)
      ? recommendationsData
      : recommendationsData?.recommendations || [];

    const normalizeList = (value) => {
      if (Array.isArray(value)) {
        return value.filter((item) => item !== null && item !== undefined && item !== '');
      }
      if (value === null || value === undefined) {
        return [];
      }
      if (typeof value === 'string') {
        const trimmed = value.trim();
        if (trimmed === '' || trimmed === '[]') {
          return [];
        }
        if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
          const normalized = trimmed.replace(/'/g, '"');
          try {
            const parsed = JSON.parse(normalized);
            if (Array.isArray(parsed)) {
              return parsed.filter((item) => item !== null && item !== undefined && item !== '');
            }
          } catch (err) {
            // Fallback to splitting below.
          }
          const items = trimmed
            .slice(1, -1)
            .split(',')
            .map((item) => item.trim().replace(/^["']|["']$/g, ''))
            .filter((item) => item !== '');
          return items;
        }
        return [trimmed];
      }
      return [String(value)];
    };

    const formatValue = (value) =>
      value === null || value === undefined || value === '' ? 'N/A' : value;

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
      currentPage: 'anime'
    });
  } catch (err) {
    res.render('anime/anime_detail', {
      title: 'Anime Detail',
      anime: null,
      error: 'Unable to load anime details.'
    });
  }
};

exports.characters = async (req, res) => {
  try {
    const { id } = req.params;
    const response = await apiPostgres.get(`/api/details/${id}/characters`);
    res.render('anime/related_characters', {
      title: `Recommendations for ${response.data.title}`,
      related_characters: response.data.related_characters,
      currentPage: 'anime'
    });
  } catch (err) {
    res.render('related_characters', {
      title: 'Characters',
      recommendations: null,
      error: 'Impossibile caricare i personaggi dell\'anime. Il server potrebbe non essere disponibile.'
    });
  }
};

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
