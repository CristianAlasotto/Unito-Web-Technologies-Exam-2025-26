const { apiPostgres } = require('./apiClients');

// NB anime = "details"

exports.list = async (req, res, next) => {
  try {
    const page = req.query.page || 1;
    const pageSize = req.query.pageSize || 45;
    const response = await apiPostgres.get(`/api/details?page=${page}&pageSize=${pageSize}`);
    const animes = response.data.items;
    const totalPages = response.data.totalPages;

    res.render('anime/anime_list', {
      title: 'Anime',
      animes: animes,
      pagination: {
        currentPage: page,
        totalPages: totalPages,
        hasPrev: page > 1,
        prevPage: page - 1,
        hasNext: page < totalPages,
        nextPage: parseInt(page) + 1
      },
      warning: !animes || animes.length === 0 ? 'Nessun anime trovato nel database.' : null
    });
  } catch (err) {
    res.render('anime/anime_list', {
      title: 'Anime',
      animes: [],
      currentPage: 'anime',
      error: 'Impossibile caricare i dati degli anime. Il server potrebbe non essere disponibile.'
    });
  }
};

exports.detail = async (req, res, next) => {
  try {
    const { id } = req.params;
    const response = await apiPostgres.get(`/api/details/${id}`);
    const raw = response.data || {};

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
      has_explicit_genres: explicitGenres.length > 0
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
      error: 'Impossibile caricare i dettagli dell\'anime. Il server potrebbe non essere disponibile.'
    });
  }
};

exports.reccomendations = async (req, res) => {
  try {
    const { id } = req.params;
    const response = await apiPostgres.get(`/api/details/${id}/recommendations`);
    //const response = await apiPostgres.get(`/api/details/${id}/recommendations`);
    res.render('anime/anime_recommendations', {
      title: `Recommendations for ${response.data.title}`,
      recommendations: response.data.recommendations,
      currentPage: 'anime'
    });
  } catch (err) {
    res.render('anime/anime_recommendations', {
      title: 'Recommendations',
      recommendations: null,
      error: 'Impossibile caricare i consigli dell\'anime. Il server potrebbe non essere disponibile.'
    });
  }
};
