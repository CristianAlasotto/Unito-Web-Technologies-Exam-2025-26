const express = require('express');
const path = require('path');
const axios = require('axios');
const { engine } = require('express-handlebars');
const app = express();

// Handlebars configuration - CORRECT VERSION
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

// Environment variables
const DATA_EXPRESS_URL = process.env.DATA_EXPRESS_URL || 'http://localhost:3001';
const DATA_SPRING_URL = process.env.DATA_SPRING_URL || 'http://localhost:8080';

// Axios configuration with timeout
const apiClient = axios.create({
  timeout: 5000
});

// Home page route - VERSIONE CON MOCK DATA
app.get('/', async (req, res) => {
  try {
    console.log("RENDERING HOMEPAGE WITH ANIME LIST");
    
    // Mock data degli anime per la homepage
    const mockAnime = [
      {
        anime_id: 1,
        title: "Attack on Titan",
        image_url: "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
        score: 8.5,
        type: "TV",
        episodes: 25
      },
      {
        anime_id: 2,
        title: "Death Note",
        image_url: "https://cdn.myanimelist.net/images/anime/9/9453.jpg",
        score: 8.6,
        type: "TV",
        episodes: 37
      },
      {
        anime_id: 3,
        title: "One Piece",
        image_url: "https://cdn.myanimelist.net/images/anime/6/73245.jpg",
        score: 8.7,
        type: "TV",
        episodes: 1000
      },
      {
        anime_id: 4,
        title: "Fullmetal Alchemist: Brotherhood",
        image_url: "https://cdn.myanimelist.net/images/anime/1223/96541.jpg",
        score: 9.1,
        type: "TV",
        episodes: 64
      },
      {
        anime_id: 5,
        title: "Steins;Gate",
        image_url: "https://cdn.myanimelist.net/images/anime/5/73199.jpg",
        score: 9.0,
        type: "TV",
        episodes: 24
      },
      {
        anime_id: 6,
        title: "Hunter x Hunter",
        image_url: "https://cdn.myanimelist.net/images/anime/11/33657.jpg",
        score: 9.0,
        type: "TV",
        episodes: 148
      }
    ];

    res.render('anime/index', { 
      title: 'Anime Database',
      animes: mockAnime
    });
    
    // Versione originale con API (decommentare quando Spring è pronto)
    // try {
    //   const stats = await apiClient.get(`${DATA_SPRING_URL}/api/anime/stats`);
    //   const anime = await apiClient.get(`${DATA_SPRING_URL}/api/anime`);
    //   return res.render('anime/index', { 
    //     title: 'Anime Database',
    //     stats: stats.data,
    //     animes: anime.data
    //   });
    // } catch (apiError) {
    //   console.warn('API not available, using fallback data');
    // }
  } catch (error) {
    console.error('Home error:', error.message);
    res.render('anime/index', { 
      title: 'Anime Database', 
      error: 'Error loading page',
      animes: []
    });
  }
});

// Anime list route - VERSIONE PER TEST !
app.get('/anime', async (req, res) => {
  try {
    console.log("MOCK ANIME LIST RENDER");
    
    // Mock data per testing (da rimuovere quando il backend Spring è pronto)
    const mockAnime = [
      {
        anime_id: 1,
        title: "Attack on Titan",
        image_url: "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
        score: 8.5,
        type: "TV",
        episodes: 25
      },
      {
        anime_id: 2,
        title: "Death Note",
        image_url: "https://cdn.myanimelist.net/images/anime/9/9453.jpg",
        score: 8.6,
        type: "TV",
        episodes: 37
      }
    ];

    res.render('anime/anime_list', { 
      title: 'Anime List',
      animes: mockAnime
    });
    
    // Versione originale con API (decommentare quando Spring è pronto)
    // const anime = await apiClient.get(`${DATA_SPRING_URL}/api/anime`);
    // res.render('anime/list', { 
    //   title: 'Anime List',
    //   animes: anime.data || []
    // });
  } catch (error) {
    console.error('Anime list error:', error.message);
    res.render('anime/anime_list', { 
      title: 'Anime List', 
      error: 'Unable to load anime list',
      animes: []
    });
  }
});

// Anime detail route
app.get('/anime/:id', async (req, res) => {
  try {
    const anime = await apiClient.get(`${DATA_SPRING_URL}/api/anime/${req.params.id}`);
    res.render('anime/detail', { 
      title: anime.data.title || 'Anime Details',
      anime: anime.data 
    });
  } catch (error) {
    console.error('Anime detail error:', error.message);
    res.status(404).render('error', { message: 'Anime not found' });
  }
});

// Characters route
app.get('/characters', async (req, res) => {
  try {
    const characters = await apiClient.get(`${DATA_EXPRESS_URL}/api/characters`);
    res.render('characters/list', { 
      title: 'Characters',
      characters: characters.data || []
    });
  } catch (error) {
    console.error('Characters error:', error.message);
    res.render('characters/list', { 
      title: 'Characters', 
      error: 'Unable to load characters',
      characters: []
    });
  }
});

// Staff list route
app.get('/staff', async (req, res) => {
  try {
    const staff = await apiClient.get(`${DATA_SPRING_URL}/api/staff`);
    res.render('staff/list', { 
      title: 'Staff',
      staff: staff.data || []
    });
  } catch (error) {
    console.error('Staff error:', error.message);
    res.render('staff/list', { 
      title: 'Staff', 
      error: 'Unable to load staff',
      staff: []
    });
  }
});

// Staff detail route
app.get('/staff/:id', async (req, res) => {
  try {
    const staff = await apiClient.get(`${DATA_SPRING_URL}/api/staff/${req.params.id}`);
    res.render('staff/detail', { 
      title: staff.data.name || 'Staff Details',
      staff: staff.data 
    });
  } catch (error) {
    console.error('Staff detail error:', error.message);
    res.status(404).render('error', { message: 'Staff not found' });
  }
});

// User profile route
app.get('/profile/:username', async (req, res) => {
  // Check if the user is authenticated
  if (!req.isAuthenticated()) {
    console.log(`User ${req.params.username} attempted to access profile without authentication`);
    return res.render('error', { 
      message: 'Please log in to access your profile',
      clientLogJson: JSON.stringify(`Profile access blocked: user "${req.params.username}" not authenticated`)
    });
  }

  try {
    const profile = await apiClient.get(`${DATA_EXPRESS_URL}/api/users/${req.params.username}`);
    res.render('profile/user', { 
      title: `Profile - ${req.params.username}`,
      profile: profile.data 
    });
  } catch (error) {
    console.error('Profile error:', error.message);
    res.status(404).render('error', { message: 'Profile not found' });
  }
});

// Profile route without username (fallback)
app.get('/profile', (req, res) => {
  return res.status(400).render('error', {
    message: 'Invalid profile path. Use /profile/:username',
    clientLogJson: JSON.stringify('Username not found /profile/:username')
  });
});

// Favourites route
app.get('/favourites', async (req, res) => {
  try {
    const favourites = await apiClient.get(`${DATA_EXPRESS_URL}/api/favourites`);
    res.render('favourites/list', { 
      title: 'My Favourites',
      favourites: favourites.data || []
    });
  } catch (error) {
    console.error('Favourites error:', error.message);
    res.render('favourites/list', { 
      title: 'My Favourites', 
      error: 'Unable to load favourites',
      favourites: []
    });
  }
});

// Add to favourites route
app.post('/favourites/:id', async (req, res) => {
  try {
    await apiClient.post(`${DATA_EXPRESS_URL}/api/favourites/${req.params.id}`);
    res.json({ success: true, message: 'Added to favourites' });
  } catch (error) {
    console.error('Add favourite error:', error.message);
    res.status(400).json({ success: false, error: 'Unable to add to favourites' });
  }
});

// Remove from favourites route
app.delete('/favourites/:id', async (req, res) => {
  try {
    await apiClient.delete(`${DATA_EXPRESS_URL}/api/favourites/${req.params.id}`);
    res.json({ success: true, message: 'Removed from favourites' });
  } catch (error) {
    console.error('Remove favourite error:', error.message);
    res.status(400).json({ success: false, error: 'Unable to remove from favourites' });
  }
});

// 404 Handler
app.use((req, res) => {
  res.status(404).render('error', { message: 'Page not found' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`✅ Server listening on port ${PORT}`));