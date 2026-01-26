const { apiPostgres } = require('../controllers/apiClients');

exports.preview = async (req, res, next) => {
  try {
    // Carica i dati in parallelo per efficienza
    const [animeRes, charactersRes, staffRes] = await Promise.allSettled([
      apiPostgres.get('/api/details'),
      apiPostgres.get('/api/characters'),
      apiPostgres.get('/api/staff')
    ]);

    // Estrai i dati o imposta array vuoti in caso di errore
    const animes = animeRes.status === 'fulfilled' ? animeRes.value.data : [];
    const characters = charactersRes.status === 'fulfilled' ? charactersRes.value.data : [];
    const staff = staffRes.status === 'fulfilled' ? staffRes.value.data : [];

    // Controlla se qualche chiamata è fallita per mostrare un avviso
    const warning = [animeRes, charactersRes, staffRes].some(res => res.status === 'rejected')
      ? 'Uno o più servizi non sono disponibili. I dati potrebbero essere incompleti.'
      : null;

    res.render('index', {
      title: 'Home',
      animes,
      characters,
      staff,
      warning,
      currentPage: 'home'
    });

  } catch (err) {
    // Errore generico se tutte le chiamate falliscono
    res.render('index', {
      title: 'Home',
      error: 'Impossibile caricare i dati per l\'anteprima.',
      currentPage: 'home'
    });
  }
};

