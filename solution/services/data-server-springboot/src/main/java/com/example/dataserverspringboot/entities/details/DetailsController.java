package com.example.dataserverspringboot.entities.details;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Details (Anime) endpoints
 * Provides API endpoints that server.js uses
 */
@RestController
@RequestMapping("/api/anime")
@CrossOrigin(origins = "*")
public class DetailsController {

    @Autowired
    private DetailsService service;

    /**
     * GET /api/anime/stats
     *
     * Required by: server.js line 57
     * Purpose: Homepage statistics
     *
     * Returns database statistics for the homepage
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAnime", service.countAllAnime());
        stats.put("totalCharacters", 0); // TODO: implement when Characters is ready
        stats.put("totalUsers", 0);      // TODO: implement when Profiles is ready
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/anime
     *
     * Required by: server.js line 85
     * Purpose: Anime list page
     *
     * Returns all anime from the database
     */
    @GetMapping
    public ResponseEntity<List<Details>> getAllAnime() {
        List<Details> animeList = service.getAllAnime();
        return ResponseEntity.ok(animeList);
    }

    /**
     * GET /api/anime/{id}
     *
     * Required by: server.js line 96
     * Purpose: Single anime detail page
     *
     * Returns a single anime by its mal_id
     */
    @GetMapping("/{mal_id}")
    public ResponseEntity<Details> getAnimeById(@PathVariable Integer mal_id) {
        Optional<Details> anime = service.getAnimeById(mal_id);
        return anime.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // BONUS ENDPOINTS (optional - not required by server.js)
    // ============================================================

    /**
     * GET /api/anime/search?title=xxx
     * Search anime by title
     */
    @GetMapping("/search")
    public ResponseEntity<List<Details>> searchByTitle(@RequestParam String title) {
        try {
            List<Details> results = service.searchByTitle(title);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/anime/top-rated
     * Get top 10 highest rated anime
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<Details>> getTopRated() {
        List<Details> topAnime = service.getTopRated();
        return ResponseEntity.ok(topAnime);
    }

    /**
     * GET /api/anime/type/{type}
     * Filter anime by type (TV, Movie, OVA, etc.)
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Details>> getByType(@PathVariable String type) {
        List<Details> animeList = service.getByType(type);
        return ResponseEntity.ok(animeList);
    }

    /**
     * GET /api/anime/genre/{genre}
     * Filter anime by genre
     */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Details>> getByGenre(@PathVariable String genre) {
        List<Details> animeList = service.getByGenre(genre);
        return ResponseEntity.ok(animeList);
    }

    /**
     * GET /api/anime/score?minScore=8.0&maxScore=10.0
     * Filter anime by score range
     */
    @GetMapping("/score")
    public ResponseEntity<List<Details>> getByScoreRange(
            @RequestParam Double minScore,
            @RequestParam Double maxScore) {
        try {
            List<Details> animeList = service.getByScoreRange(minScore, maxScore);
            return ResponseEntity.ok(animeList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}