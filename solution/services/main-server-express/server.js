import morgan from "morgan";
import express from "express";
import path from "path";
import dotenv from "dotenv";
import { engine } from "express-handlebars";
import { fileURLToPath } from "url";
import { dirname } from "path";
import { dataExpressApi, dataSpringApi } from "./lib/api.js";
import profileRouter from './routes/profile.js';

// only for mock datas
import {
  MOCK_ANIME_HOME,
  MOCK_ANIME_LIST,
  MOCK_ANIME_DETAIL,
  MOCK_CHARACTERS_LIST,
  MOCK_CHARACTERS_DETAIL,
  MOCK_FAVOURITES,
  MOCK_PROFILE
} from "./lib/mockDb.js";

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


/* --- REST API (gateway) routes --- */
/**
 * NOTE:
 * - In production (USE_MOCK_DATA=false) these endpoints behave as a thin gateway/proxy:
 *   they forward query params as-is and return the upstream JSON without reshaping.
 * - In mock mode we emulate the formats to allow frontend development.
 */

function pickFields(obj, fieldsCsv) {
  const fields = (fieldsCsv || "")
    .split(",")
    .map(s => s.trim())
    .filter(Boolean);

  if (fields.length === 0) return obj;

  const out = {};
  for (const f of fields) {
    // allow "id" as alias of "anime_id" for convenience (as in the PDF examples)
    if (f === "id" && obj.anime_id !== undefined) out.anime_id = obj.anime_id;
    else if (obj[f] !== undefined) out[f] = obj[f];
    else if (f === "anime_id" && obj.anime_id !== undefined) out.anime_id = obj.anime_id;
  }
  return out;
}

function safeInt(v, fallback = undefined) {
  const n = Number.parseInt(v, 10);
  return Number.isFinite(n) ? n : fallback;
}

function deriveYear(anime) {
  if (anime.year) return anime.year;
  const d = anime.start_date || anime.startDate;
  if (typeof d === "string" && d.length >= 4) {
    const y = safeInt(d.slice(0, 4));
    if (y) return y;
  }
  return undefined;
}

function applySearchFilterSort(animes, query) {
  let items = [...animes];

  // Filtering (e.g., ?type=TV&year=2013)
  if (query.type) items = items.filter(a => (a.type || "").toLowerCase() === String(query.type).toLowerCase());
  if (query.year) {
    const y = safeInt(query.year);
    if (y) items = items.filter(a => deriveYear(a) === y);
  }

  // Text search (e.g., ?search=attack)
  if (query.search) {
    const q = String(query.search).toLowerCase();
    items = items.filter(a => (a.title || "").toLowerCase().includes(q));
  }

  // Sorting (e.g., ?sort=-score or ?sort=score)
  if (query.sort) {
    const raw = String(query.sort);
    const desc = raw.startsWith("-");
    const key = desc ? raw.slice(1) : raw;

    items.sort((a, b) => {
      const av = a[key];
      const bv = b[key];

      // numbers first
      if (typeof av === "number" && typeof bv === "number") return desc ? (bv - av) : (av - bv);

      // fallback to string compare
      const as = av === undefined || av === null ? "" : String(av);
      const bs = bv === undefined || bv === null ? "" : String(bv);
      return desc ? bs.localeCompare(as) : as.localeCompare(bs);
    });
  }

  return items;
}

/**
 * GET /api/anime
 * Supports:
 * - List
 * - Filtering (?type=...&year=...)
 * - Search (?search=...)
 * - Sort (?sort=... or ?sort=-...)
 * - Pagination (?limit&offset) OR (?page&pageSize)
 *
 * In non-mock mode, forwards everything to the upstream dataSpringApi.
 */
app.get("/api/anime", async (req, res) => {
  try {
    if (!USE_MOCK_DATA) {
      const response = await dataSpringApi.get("/api/anime", { params: req.query });
      return res.json(response.data);
    }

    // Mock mode
    // Use detail objects for richer filtering/sorting support, but keep lightweight fields in output when possible.
    const base = Object.values(MOCK_ANIME_DETAIL);
    const filtered = applySearchFilterSort(base, req.query);

    const total = filtered.length;

    // Pagination: limit/offset
    const limit = safeInt(req.query.limit);
    const offset = safeInt(req.query.offset);
    if (limit !== undefined || offset !== undefined) {
      const l = limit ?? 20;
      const o = offset ?? 0;
      const items = filtered.slice(o, o + l);
      return res.json({ limit: l, offset: o, total, items });
    }

    // Pagination: page/pageSize
    const page = safeInt(req.query.page);
    const pageSize = safeInt(req.query.pageSize);
    if (page !== undefined || pageSize !== undefined) {
      const ps = pageSize ?? 20;
      const p = page ?? 1;
      const totalPages = Math.max(1, Math.ceil(total / ps));
      const start = (p - 1) * ps;
      const items = filtered.slice(start, start + ps);
      return res.json({ page: p, pageSize: ps, totalPages, items });
    }

    // Default list response: array
    return res.json(filtered);
  } catch (error) {
    console.error("GET /api/anime error:", error.message);
    return res.status(500).json({ error: "Unable to load anime list" });
  }
});

/**
 * GET /api/anime/:id
 * Supports:
 * - Resource single
 * - Field selection (?fields=...)
 * - Expansion (?include=characters,staff) -> in mock we attach empty arrays if requested
 *
 * In non-mock mode, forwards everything to the upstream dataSpringApi.
 */
app.get("/api/anime/:id", async (req, res) => {
  try {
    if (!USE_MOCK_DATA) {
      const response = await dataSpringApi.get(`/api/anime/${req.params.id}`, { params: req.query });
      return res.json(response.data);
    }

    const animeId = safeInt(req.params.id);
    const anime = MOCK_ANIME_DETAIL[animeId];

    if (!anime) return res.status(404).json({ error: "Anime not found" });

    let out = { ...anime };

    // Expansion relations (mock: empty payloads, but correct shape)
    if (req.query.include) {
      const includes = String(req.query.include)
        .split(",")
        .map(s => s.trim())
        .filter(Boolean);

      if (includes.includes("characters") && out.characters === undefined) out.characters = [];
      if (includes.includes("staff") && out.staff === undefined) out.staff = [];
    }

    // Field selection
    if (req.query.fields) out = pickFields(out, req.query.fields);

    return res.json(out);
  } catch (error) {
    console.error("GET /api/anime/:id error:", error.message);
    return res.status(500).json({ error: "Unable to load anime detail" });
  }
});

/**
 * GET /api/anime/:id/summary
 * Returns a reduced view.
 *
 * In non-mock mode, forwards everything to the upstream dataSpringApi.
 */
app.get("/api/anime/:id/summary", async (req, res) => {
  try {
    if (!USE_MOCK_DATA) {
      const response = await dataSpringApi.get(`/api/anime/${req.params.id}/summary`, { params: req.query });
      return res.json(response.data);
    }

    const animeId = safeInt(req.params.id);
    const anime = MOCK_ANIME_DETAIL[animeId];
    if (!anime) return res.status(404).json({ error: "Anime not found" });

    // Mock summary (shape as per PDF example)
    const summary = {
      anime_id: anime.anime_id,
      title: anime.title,
      score: anime.score,
      popularity: anime.popularity
    };

    return res.json(summary);
  } catch (error) {
    console.error("GET /api/anime/:id/summary error:", error.message);
    return res.status(500).json({ error: "Unable to load anime summary" });
  }
});
/* --- END REST API (gateway) routes --- */


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
    if (USE_MOCK_DATA) {
      return res.render("characters/list", {
        title: "Characters",
        characters: MOCK_CHARACTERS_LIST,
        warning: "Mock data enabled (USE_MOCK_DATA=true)",
      });
    }

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

// Character detail route
app.get("/characters/:id", async (req, res) => {
  try {
    if (USE_MOCK_DATA) {
      const characterId = parseInt(req.params.id);
      const character = MOCK_CHARACTERS_DETAIL[characterId];
      
      if (!character) {
        return res.status(404).render("error", { 
          message: "Character not found in mock data" 
        });
      }
      
      return res.render("characters/detail", {
        title: character.name || "Character Details",
        character: character,
        warning: "Mock data enabled (USE_MOCK_DATA=true)",
      });
    }

    const character = await dataExpressApi.get(`/api/characters/${req.params.id}`);
    return res.render("characters/detail", {
      title: character.data.name || "Character Details",
      character: character.data,
    });
  } catch (error) {
    console.error("Character detail error:", error.message);
    return res.status(404).render("error", { message: "Character not found" });
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

// Use profile router
app.use('/profile', profileRouter);

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