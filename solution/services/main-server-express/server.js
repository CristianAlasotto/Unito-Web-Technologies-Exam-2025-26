import morgan from "morgan";
import express from "express";
import path from "path";
import dotenv from "dotenv";
import { engine } from "express-handlebars";
import { fileURLToPath } from "url";
import { dirname } from "path";
import { dataExpressApi, dataSpringApi } from "./lib/api.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Load environment variables from solution/.env (two levels up)
dotenv.config({ path: path.resolve(__dirname, "../../.env") });

const app = express();

// Toggle HTTP request logging via LOG_HTTP_ENABLED
const LOG_HTTP_ENABLED =
  (process.env.LOG_HTTP_ENABLED || "true").toLowerCase() === "true";

if (LOG_HTTP_ENABLED) {
  app.use(
    morgan((tokens, req, res) => {
      const method = tokens.method(req, res).padEnd(7);
      const url = tokens.url(req, res).padEnd(30);
      const status = tokens.status(req, res).padEnd(6);
      const time = (tokens["response-time"](req, res) + " ms").padEnd(10);
      return `${method} | ${url} | ${status} | ${time}`;
    })
  );
}

// Handlebars configuration
app.engine(
  "hbs",
  engine({
    extname: ".hbs",
    layoutsDir: path.join(__dirname, "views/layout"),
    defaultLayout: "main",
    partialsDir: path.join(__dirname, "views/partials"),
    helpers: {
      json: function(context) {
        return JSON.stringify(context, null, 2);
      }
    }
  })
);

app.set("view engine", "hbs");
app.set("views", path.join(__dirname, "views"));

// Middleware
app.use(express.static(path.join(__dirname, "public")));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// (Optional) service base URLs (kept for reference; api clients are in ./lib/api.js)
const DATA_EXPRESS_URL = process.env.DATA_EXPRESS_URL || "http://localhost:3001";
const DATA_SPRING_URL = process.env.DATA_SPRING_URL || "http://localhost:8080";

// Enable mock data while backend is incomplete
const USE_MOCK_DATA =
  (process.env.USE_MOCK_DATA || "false").toLowerCase() === "true";

/* --- Mock data (temporary) --- */
const MOCK_ANIME_HOME = [
  {
    anime_id: 1,
    title: "Attack on Titan",
    image_url: "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
    score: 8.5,
    type: "TV",
    episodes: 25,
  },
  {
    anime_id: 2,
    title: "Death Note",
    image_url: "https://cdn.myanimelist.net/images/anime/9/9453.jpg",
    score: 8.6,
    type: "TV",
    episodes: 37,
  }
];

const MOCK_ANIME_LIST = [
  {
    anime_id: 1,
    title: "Attack on Titan",
    image_url: "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
    score: 8.5,
    type: "TV",
    episodes: 25,
  },
  {
    anime_id: 2,
    title: "Death Note",
    image_url: "https://cdn.myanimelist.net/images/anime/9/9453.jpg",
    score: 8.6,
    type: "TV",
    episodes: 37,
  },
];

const MOCK_ANIME_DETAIL = {
  1: {
    anime_id: 1,
    title: "Attack on Titan",
    title_japanese: "進撃の巨人",
    image_url: "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
    score: 8.5,
    type: "TV",
    status: "Finished Airing",
    episodes: 25,
    start_date: "2013-04-07",
    end_date: "2013-09-29",
    synopsis: "Centuries ago, mankind was slaughtered to near extinction by monstrous humanoid creatures called Titans, forcing humans to hide in fear behind enormous concentric walls. What makes these giants truly terrifying is that their taste for human flesh is not born out of hunger but what appears to be out of pleasure.",
    genres: ["Action", "Drama", "Fantasy", "Mystery"],
    themes: ["Gore", "Military", "Survival"],
    studios: ["Wit Studio"],
    demographics: ["Shounen"],
    rank: 1,
    popularity: 1,
    members: 3500000,
    favorites: 180000,
    url: "https://myanimelist.net/anime/16498/Shingeki_no_Kyojin"
  },
  2: {
    anime_id: 2,
    title: "Death Note",
    title_japanese: "デスノート",
    image_url: "https://cdn.myanimelist.net/images/anime/9/9453.jpg",
    score: 8.6,
    type: "TV",
    status: "Finished Airing",
    episodes: 37,
    start_date: "2006-10-04",
    end_date: "2007-06-27",
    synopsis: "A shinigami, as a god of death, can kill any person—provided they see their victim's face and write their victim's name in a notebook called a Death Note. One day, Ryuk, bored by the shinigami lifestyle and interested in seeing how a human would use a Death Note, drops one into the human realm.",
    genres: ["Mystery", "Supernatural", "Suspense"],
    themes: ["Psychological"],
    studios: ["Madhouse"],
    demographics: ["Shounen"],
    rank: 2,
    popularity: 2,
    members: 3200000,
    favorites: 165000,
    url: "https://myanimelist.net/anime/1535/Death_Note"
  }
};

// Home page route - development approach + optional mock
app.get("/", async (req, res) => {
  try {
    if (USE_MOCK_DATA) {
      return res.render("anime/index", {
        title: "Anime Database",
        animes: MOCK_ANIME_HOME,
        warning: "Mock data enabled (USE_MOCK_DATA=true)",
      });
    }

    // API-first (development approach)
    try {
      const stats = await dataSpringApi.get("/api/anime/stats");
      return res.render("anime/index", {
        title: "Anime Database",
        stats: stats.data,
      });
    } catch (statsError) {
      console.warn("API stats not available, loading fallback data");

      const fallbackStats = {
        totalAnime: "N/A",
        totalCharacters: "N/A",
        totalUsers: "N/A",
      };

      return res.render("anime/index", {
        title: "Anime Database",
        stats: fallbackStats,
        warning: "API not available at the moment",
      });
    }
  } catch (error) {
    console.error("Home error:", error.message);
    return res.render("anime/index", {
      title: "Anime Database",
      error: "Error loading page",
      animes: [],
    });
  }
});

// Anime list route - development approach + optional mock
app.get("/anime", async (req, res) => {
  try {
    if (USE_MOCK_DATA) {
      return res.render("anime/anime_list", {
        title: "Anime List",
        animes: MOCK_ANIME_LIST,
        warning: "Mock data enabled (USE_MOCK_DATA=true)",
      });
    }

    const anime = await dataSpringApi.get("/api/anime");
    return res.render("anime/anime_list", {
      title: "Anime List",
      animes: anime.data || [],
    });
  } catch (error) {
    console.error("Anime list error:", error.message);
    return res.render("anime/anime_list", {
      title: "Anime List",
      error: "Unable to load anime list",
      animes: [],
    });
  }
});

// Anime detail route
app.get("/anime/:id", async (req, res) => {
  try {
    if (USE_MOCK_DATA) {
      const animeId = parseInt(req.params.id);
      const anime = MOCK_ANIME_DETAIL[animeId];
      
      if (!anime) {
        return res.status(404).render("error", { 
          message: "Anime not found in mock data" 
        });
      }
      
      return res.render("anime/detail", {
        title: anime.title || "Anime Details",
        anime: anime,
        warning: "Mock data enabled (USE_MOCK_DATA=true)",
      });
    }

    const anime = await dataSpringApi.get(`/api/anime/${req.params.id}`);
    return res.render("anime/detail", {
      title: anime.data.title || "Anime Details",
      anime: anime.data,
    });
  } catch (error) {
    console.error("Anime detail error:", error.message);
    return res.status(404).render("error", { message: "Anime not found" });
  }
});

// Characters route
app.get("/characters", async (req, res) => {
  try {
    const characters = await dataExpressApi.get("/api/characters");
    return res.render("characters/list", {
      title: "Characters",
      characters: characters.data || [],
    });
  } catch (error) {
    console.error("Characters error:", error.message);
    return res.render("characters/list", {
      title: "Characters",
      error: "Unable to load characters",
      characters: [],
    });
  }
});

// Staff list route
app.get("/staff", async (req, res) => {
  try {
    const staff = await dataSpringApi.get("/api/staff");
    return res.render("staff/list", {
      title: "Staff",
      staff: staff.data || [],
    });
  } catch (error) {
    console.error("Staff error:", error.message);
    return res.render("staff/list", {
      title: "Staff",
      error: "Unable to load staff",
      staff: [],
    });
  }
});

// Staff detail route
app.get("/staff/:id", async (req, res) => {
  try {
    const staff = await dataSpringApi.get(`/api/staff/${req.params.id}`);
    return res.render("staff/detail", {
      title: staff.data.name || "Staff Details",
      staff: staff.data,
    });
  } catch (error) {
    console.error("Staff detail error:", error.message);
    return res.status(404).render("error", { message: "Staff not found" });
  }
});

// User profile route
app.get("/profile/:username", async (req, res) => {
  // Check if the user is authenticated
  if (!req.isAuthenticated()) {
    console.log(
      `User ${req.params.username} attempted to access profile without authentication`
    );
    return res.render("error", {
      message: "Please log in to access your profile",
      clientLogJson: JSON.stringify(
        `Profile access blocked: sis "${req.params.username}" not authenticated`
      ),
    });
  }

  try {
    const profile = await dataExpressApi.get(`/api/users/${req.params.username}`);
    return res.render("profile/user", {
      title: `Profile - ${req.params.username}`,
      profile: profile.data,
    });
  } catch (error) {
    console.error("Profile error:", error.message);
    return res.status(404).render("error", { message: "Profile not found" });
  }
});

// Profile route without username (fallback)
app.get("/profile", (req, res) => {
  return res.status(400).render("error", {
    message: "Invalid profile path. Use /profile/:username",
    clientLogJson: JSON.stringify("Username not found /profile/:username"),
  });
});

// General tests route - displays data from MongoDB endpoints
app.get("/general-tests", async (req, res) => {
  try {
    const [favs, ratings, stats] = await Promise.all([
      dataExpressApi.get("/getfavs").catch(err => ({ data: null, error: err.message })),
      dataExpressApi.get("/getratings").catch(err => ({ data: null, error: err.message })),
      dataExpressApi.get("/getstats").catch(err => ({ data: null, error: err.message }))
    ]);

    return res.render("general-tests", {
      title: "General Tests - MongoDB Data",
      favs: favs.data || { error: favs.error },
      ratings: ratings.data || { error: ratings.error },
      stats: stats.data || { error: stats.error },
      favsError: !favs.data,
      ratingsError: !ratings.data,
      statsError: !stats.data
    });
  } catch (error) {
    console.error("General tests error:", error.message);
    return res.status(500).render("error", { 
      message: "Unable to load test data",
      clientLogJson: JSON.stringify({ error: error.message })
    });
  }
});

// Favourites route
app.get("/favourites", async (req, res) => {
  try {
    const favourites = await dataExpressApi.get("/api/favourites");
    return res.render("favourites/list", {
      title: "My Favourites",
      favourites: favourites.data || [],
    });
  } catch (error) {
    console.error("Favourites error:", error.message);
    return res.render("favourites/list", {
      title: "My Favourites",
      error: "Unable to load favourites",
      favourites: [],
    });
  }
});

// Add to favourites route
app.post("/favourites/:id", async (req, res) => {
  try {
    await dataExpressApi.post(`/api/favourites/${req.params.id}`);
    return res.json({ success: true, message: "Added to favourites" });
  } catch (error) {
    console.error("Add favourite error:", error.message);
    return res
      .status(400)
      .json({ success: false, error: "Unable to add to favourites" });
  }
});

// Remove from favourites route
app.delete("/favourites/:id", async (req, res) => {
  try {
    await dataExpressApi.delete(`/api/favourites/${req.params.id}`);
    return res.json({ success: true, message: "Removed from favourites" });
  } catch (error) {
    console.error("Remove favourite error:", error.message);
    return res
      .status(400)
      .json({ success: false, error: "Unable to remove from favourites" });
  }
});

/* --- Mongo requests --- */
app.get("/api/favorites", async (req, res) => {
  try {
    const response = await dataExpressApi.get("/getfavs");
    return res.json(response.data);
  } catch (error) {
    console.error("Favorites API error:", error.message);
    return res.status(500).json({ error: "Unable to load favorites" });
  }
});

app.get("/api/ratings", async (req, res) => {
  try {
    const response = await dataExpressApi.get("/getratings");
    return res.json(response.data);
  } catch (error) {
    console.error("Ratings API error:", error.message);
    return res.status(500).json({ error: "Unable to load ratings" });
  }
});

app.get("/api/stats", async (req, res) => {
  try {
    const response = await dataExpressApi.get("/getstats");
    return res.json(response.data);
  } catch (error) {
    console.error("Stats API error:", error.message);
    return res.status(500).json({ error: "Unable to load stats" });
  }
});

// 404 Handler
app.use((req, res) => {
  return res.status(404).render("error", { message: "Page not found" });
});

// Error handler centralized (x debug)
app.use((err, req, res, next) => {
  console.error("[UNHANDLED_ERROR]", err);
  res.status(500).render("error", {
    message: "Internal Server Error",
    clientLogJson: JSON.stringify(
      {
        path: req.originalUrl,
        method: req.method,
        error: err.message,
        stack: process.env.NODE_ENV === "development" ? err.stack : undefined,
      },
      null,
      2
    ),
  });
});

const PORT = process.env.MAIN_EXPRESS_PORT || 3000;
app.listen(PORT, () => console.log(`✅ MAIN EXPRESS Server listening on port ${PORT}`));