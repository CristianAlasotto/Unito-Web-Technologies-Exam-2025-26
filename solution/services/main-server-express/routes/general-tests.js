const express = require('express');
const axios = require('axios');
var router = express.Router();

const dataExpressApi = axios.create({
  baseURL: process.env.DATA_EXPRESS_URL || 'http://localhost:3001'
});

// General tests route - displays data from MongoDB endpoints
router.get('/', async function(req, res) {
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
      statsError: !stats.data,
      currentPage: 'general-tests'
    });
  } catch (error) {
    console.error("General tests error:", error.message);
    return res.status(500).render("error", { 
      message: "Unable to load test data",
      error: error.message
    });
  }
});

/* --- Mongo requests --- */
router.get("/api/favorites", async (req, res) => {
  try {
    const response = await dataExpressApi.get("/getfavs");
    return res.json(response.data);
  } catch (error) {
    console.error("Favorites API error:", error.message);
    return res.status(500).json({ error: "Unable to load favorites" });
  }
});

router.get("/api/ratings", async (req, res) => {
  try {
    const response = await dataExpressApi.get("/getratings");
    return res.json(response.data);
  } catch (error) {
    console.error("Ratings API error:", error.message);
    return res.status(500).json({ error: "Unable to load ratings" });
  }
});

router.get("/api/stats", async (req, res) => {
  try {
    const response = await dataExpressApi.get("/getstats");
    return res.json(response.data);
  } catch (error) {
    console.error("Stats API error:", error.message);
    return res.status(500).json({ error: "Unable to load stats" });
  }
});

// New route to read data directly
router.get('/read-data', async (req, res) => {
    try {
        const [favs, ratings, stats] = await Promise.all([
            dataExpressApi.get('/getfavs'),
            dataExpressApi.get('/getratings'),
            dataExpressApi.get('/getstats')
        ]);
        return res.json({ favs: favs.data, ratings: ratings.data, stats: stats.data });
    } catch (error) {
        console.error('Error fetching data:', error.message);
        return res.status(500).json({ error: 'Unable to load data' });
    }
});

module.exports = router;