const { apiPostgres, apiMongo } = require('./apiClients');

exports.list = async (req, res, next) => {
  // ... (keep existing list logic)
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
    // Get page from query, default to 1
    const page = parseInt(req.query.page) || 1;
    const limit = 20; // Items per page

    // 1. Fetch Anime Details (Postgres)
    const detailResponse = await apiPostgres.get(`/api/details/${id}`);

    // 2. Fetch Ratings (Mongo) with Pagination
    let ratings = [];
    let totalPages = 1;

    try {
      // Update API call to include page and limit
      const ratingsResponse = await apiMongo.get(`/api/ratings?anime_id=${id}&page=${page}&limit=${limit}`);

      // Handle response format (assuming API returns items and totalPages like the characters endpoint)
      if (ratingsResponse.data.items) {
        ratings = ratingsResponse.data.items;
        totalPages = ratingsResponse.data.totalPages || 1;
      } else if (Array.isArray(ratingsResponse.data)) {
        // Fallback if API returns a flat array (no pagination metadata)
        ratings = ratingsResponse.data;
      }

    } catch (error) {
      console.warn(`Ratings fetch failed for anime ${id}:`, error.message);
    }

    res.render('anime/anime_detail', {
      title: detailResponse.data.title,
      anime: detailResponse.data,
      ratings: ratings,
      // Create pagination object
      ratingsPagination: {
        currentPage: page,
        totalPages: totalPages,
        hasPrev: page > 1,
        prevPage: page - 1,
        hasNext: page < totalPages,
        nextPage: page + 1
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

exports.reccomendations = async (req, res) => {
  // ... (keep existing recommendations logic)
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