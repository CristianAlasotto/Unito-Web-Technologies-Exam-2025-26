const { apiPostgres } = require('./apiClients');

// NB anime = "details"
// Lista di tutti gli anime
exports.list = async (req, res, next) => {
  try {
    const response = await apiPostgres.get('/api/details');
    const animes = response.data;

    res.render('anime/anime_list', {
      title: 'Anime',
      animes: animes,
      currentPage: 'anime',
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
    res.render('anime/detail', {
      title: response.data.title,
      anime: response.data,
      currentPage: 'anime'
    });
  } catch (err) {
    next(err);
  }
};
