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

/**
 * Mock data (temporary)
 * Keep it centralized so you can remove it in one shot later.
 */
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
  },
  {
    anime_id: 3,
    title: "One Piece",
    image_url: "https://cdn.myanimelist.net/images/anime/6/73245.jpg",
    score: 8.7,
    type: "TV",
    episodes: 1000,
  },
  {
    anime_id: 4,
    title: "Fullmetal Alchemist: Brotherhood",
    image_url: "https://cdn.myanimelist.net/images/anime/1223/96541.jpg",
    score: 9.1,
    type: "TV",
    episodes: 64,
  },
  {
    anime_id: 5,
    title: "Steins;Gate",
    image_url: "https://cdn.myanimelist.net/images/anime/5/73199.jpg",
    score: 9.0,
    type: "TV",
    episodes: 24,
  },
  {
    anime_id: 6,
    title: "Hunter x Hunter",
    image_url: "https://cdn.myanimelist.net/images/anime/11/33657.jpg",
    score: 9.0,
    type: "TV",
    episodes: 148,
  },
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
      return res.render("anime/list", {
        title: "Anime List",
        animes: MOCK_ANIME_LIST,
        warning: "Mock data enabled (USE_MOCK_DATA=true)",
      });
    }

    const anime = await dataSpringApi.get("/api/anime");
    return res.render("anime/list", {
      title: "Anime List",
      animes: anime.data || [],
    });
  } catch (error) {
    console.error("Anime list error:", error.message);
    return res.render("anime/list", {
      title: "Anime List",
      error: "Unable to load anime list",
      animes: [],
    });
  }
});

// Anime detail route
app.get("/anime/:id", async (req, res) => {
  try {
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
        `Profile access blocked: user "${req.params.username}" not authenticated`
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

// 404 Handler
app.use((req, res) => {
  return res.status(404).render("error", { message: "Page not found" });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`✅ Server listening on port ${PORT}`));