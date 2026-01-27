const { apiPostgres } = require('./apiClients');

// NB anime = "details"

// details?page for anime
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
    // Se c'è un errore (es. il server non risponde), puoi mostrare un messaggio di errore
    res.render('anime/anime_list', {
      title: 'Anime',
      animes: [],
      currentPage: 'anime',
      error: 'Impossibile caricare i dati degli anime. Il server potrebbe non essere disponibile.'
    });
  }
};

// Dettaglio di un anime
exports.detail = async (req, res, next) => {
  try {
    const { id } = req.params;
    const response = await apiPostgres.get(`/api/details/${id}`);
    res.render('anime/anime_detail', {
      title: response.data.title,
      anime: response.data,
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
