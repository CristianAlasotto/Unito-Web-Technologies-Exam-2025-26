const { apiPostgres, apiMongo } = require('./apiClients');

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
      warning: !animes || animes.length === 0 ? 'No anime found in database.' : null
    });
  } catch (err) {
    res.render('anime/anime_list', {
      title: 'Anime',
      animes: [],
      error: 'Unable to load anime data.'
    });
  }
};

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

    const hasScoreFilter = filters.minScore !== null || filters.maxScore !== null;

    const detailResponse = await apiPostgres.get(`/api/details/${id}`);

    let ratings = [];
    let totalPages = 1;
    let allRatings = [];

    try {
      const params = new URLSearchParams({
        anime_id: id
      });

      if (filters.status) {
        params.append('status', filters.status);
      }

      if (filters.rewatching) {
        params.append('is_rewatching', filters.rewatching === 'true' ? 1 : 0);
      }

      if (filters.sortBy) {
        const sortPrefix = filters.sortOrder === 'desc' ? '-' : '';
        params.append('sort', sortPrefix + filters.sortBy);
      }

      if (hasScoreFilter) {
        params.append('limit', 10000);
        params.append('page', 1);

        const ratingsResponse = await apiMongo.get(`/api/ratings?${params.toString()}`);

        if (ratingsResponse.data.items) {
          allRatings = ratingsResponse.data.items;
        } else if (Array.isArray(ratingsResponse.data)) {
          allRatings = ratingsResponse.data;
        }

        allRatings = allRatings.filter(rating => {
          const score = rating.score;
          const minOk = filters.minScore === null || score >= filters.minScore;
          const maxOk = filters.maxScore === null || score <= filters.maxScore;
          return minOk && maxOk;
        });

        const totalFiltered = allRatings.length;
        totalPages = Math.ceil(totalFiltered / limit);
        const startIndex = (page - 1) * limit;
        const endIndex = startIndex + limit;
        ratings = allRatings.slice(startIndex, endIndex);

      } else {
        params.append('page', page);
        params.append('limit', limit);

        const ratingsResponse = await apiMongo.get(`/api/ratings?${params.toString()}`);

        if (ratingsResponse.data.items) {
          ratings = ratingsResponse.data.items;
          totalPages = ratingsResponse.data.totalPages || 1;
        } else if (Array.isArray(ratingsResponse.data)) {
          ratings = ratingsResponse.data;
        }
      }

    } catch (error) {
      console.warn(`Ratings fetch failed for anime ${id}:`, error.message);
    }

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

    res.render('anime/anime_detail', {
      title: detailResponse.data.title,
      anime: detailResponse.data,
      ratings: ratings,
      filters: filters,

      ratingsPagination: {
        currentPage: page,
        totalPages: totalPages,
        hasPrev: page > 1,
        prevPage: page - 1,
        hasNext: page < totalPages,
        nextPage: page + 1,
        prevUrl: buildQueryString(page - 1),
        nextUrl: buildQueryString(page + 1)
      },
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

exports.getRatingsJson = async (req, res) => {
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

    const hasScoreFilter = filters.minScore !== null || filters.maxScore !== null;

    let ratings = [];
    let totalPages = 1;

    const params = new URLSearchParams({
      anime_id: id
    });

    if (filters.status) {
      params.append('status', filters.status);
    }

    if (filters.rewatching) {
      params.append('is_rewatching', filters.rewatching === 'true' ? 1 : 0);
    }

    if (filters.sortBy) {
      const sortPrefix = filters.sortOrder === 'desc' ? '-' : '';
      params.append('sort', sortPrefix + filters.sortBy);
    }

    if (hasScoreFilter) {
      params.append('limit', 10000);
      params.append('page', 1);

      const ratingsResponse = await apiMongo.get(`/api/ratings?${params.toString()}`);
      let allRatings = ratingsResponse.data.items || ratingsResponse.data || [];

      allRatings = allRatings.filter(rating => {
        const score = rating.score;
        const minOk = filters.minScore === null || score >= filters.minScore;
        const maxOk = filters.maxScore === null || score <= filters.maxScore;
        return minOk && maxOk;
      });

      const totalFiltered = allRatings.length;
      totalPages = Math.ceil(totalFiltered / limit);
      const startIndex = (page - 1) * limit;
      const endIndex = startIndex + limit;
      ratings = allRatings.slice(startIndex, endIndex);

    } else {
      params.append('page', page);
      params.append('limit', limit);

      const ratingsResponse = await apiMongo.get(`/api/ratings?${params.toString()}`);
      ratings = ratingsResponse.data.items || ratingsResponse.data || [];
      totalPages = ratingsResponse.data.totalPages || 1;
    }

    res.json({
      ratings: ratings,
      pagination: {
        currentPage: page,
        totalPages: totalPages,
        hasPrev: page > 1,
        hasNext: page < totalPages
      }
    });

  } catch (error) {
    console.error('Error fetching ratings:', error);
    res.status(500).json({ error: 'Failed to fetch ratings' });
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