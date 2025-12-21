const express = require('express');
const path = require('path');
const axios = require('axios');
const { engine } = require('express-handlebars');
const app = express();

// Configurazione Handlebars - VERSIONE CORRETTA
app.engine('hbs', engine({
  extname: '.hbs',
  layoutsDir: path.join(__dirname, 'views/layout'),
  defaultLayout: 'main',
  partialsDir: path.join(__dirname, 'views/partials')
}));

app.set('view engine', 'hbs');
app.set('views', path.join(__dirname, 'views'));

// Middleware
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Variabili d'ambiente
const DATA_EXPRESS_URL = process.env.DATA_EXPRESS_URL || 'http://localhost:3001';
const DATA_SPRING_URL = process.env.DATA_SPRING_URL || 'http://localhost:8080';

// Configurazione axios con timeout
const apiClient = axios.create({
  timeout: 5000
});

// Route pagina home - VERSIONE FALLBACK
app.get('/', async (req, res) => {
  try {
    // Tenta di caricare statistiche
    try {
      const stats = await apiClient.get(`${DATA_SPRING_URL}/api/anime/stats`);
      return res.render('anime/index', { 
        title: 'Anime Database',
        stats: stats.data 
      });
    } catch (statsError) {
      console.warn('API stats non disponibile, caricamento dati di fallback');
      // Dati fallback se API non disponibile
      const fallbackStats = {
        totalAnime: 'N/A',
        totalCharacters: 'N/A',
        totalUsers: 'N/A'
      };
      return res.render('anime/index', { 
        title: 'Anime Database',
        stats: fallbackStats,
        warning: 'API non disponibile al momento'
      });
    }
  } catch (error) {
    console.error('Errore home:', error.message);
    res.render('anime/index', { 
      title: 'Anime Database', 
      error: 'Errore caricamento pagina' 
    });
  }
});

// Route anime list
app.get('/anime', async (req, res) => {
  try {
    const anime = await apiClient.get(`${DATA_SPRING_URL}/api/anime`);
    res.render('anime/list', { 
      title: 'Anime List',
      animes: anime.data || []
    });
  } catch (error) {
    console.error('Errore anime list:', error.message);
    res.render('anime/list', { 
      title: 'Anime List', 
      error: 'Impossibile caricare la lista anime',
      animes: []
    });
  }
});

// Route anime detail
app.get('/anime/:id', async (req, res) => {
  try {
    const anime = await apiClient.get(`${DATA_SPRING_URL}/api/anime/${req.params.id}`);
    res.render('anime/detail', { 
      title: anime.data.title || 'Dettagli Anime',
      anime: anime.data 
    });
  } catch (error) {
    console.error('Errore anime detail:', error.message);
    res.status(404).render('error', { message: 'Anime non trovato' });
  }
});

// Route personaggi
app.get('/characters', async (req, res) => {
  try {
    const characters = await apiClient.get(`${DATA_EXPRESS_URL}/api/characters`);
    res.render('characters/list', { 
      title: 'Characters',
      characters: characters.data || []
    });
  } catch (error) {
    console.error('Errore characters:', error.message);
    res.render('characters/list', { 
      title: 'Characters', 
      error: 'Impossibile caricare i personaggi',
      characters: []
    });
  }
});

// Route profilo utente
app.get('/profile/:username', async (req, res) => {
  try {
    const profile = await apiClient.get(`${DATA_EXPRESS_URL}/api/users/${req.params.username}`);
    res.render('profile/user', { 
      title: `Profilo - ${req.params.username}`,
      profile: profile.data 
    });
  } catch (error) {
    console.error('Errore profile:', error.message);
    res.status(404).render('error', { message: 'Profilo non trovato' });
  }
});

// 404 Handler
app.use((req, res) => {
  res.status(404).render('error', { message: 'Pagina non trovata' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`✅ Server in ascolto su porta ${PORT}`));