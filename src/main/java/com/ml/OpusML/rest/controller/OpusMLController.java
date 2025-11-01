package com.ml.OpusML.rest.controller;

import com.ml.OpusML.service.SpotifyService;
import com.ml.OpusML.service.SpotifyService.SearchResponse;
import com.ml.OpusML.service.SpotifyService.RecommendationResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
public class OpusMLController {

    private final SpotifyService spotifyService;

    public OpusMLController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    // Search tracks
    @GetMapping("/search")
    public SearchResponse searchTracks(@RequestParam String query, @RequestParam(defaultValue = "5") int limit) {
        return spotifyService.searchTracks(query, limit);
    }

    // Recommend tracks
    @PostMapping("/recommend")
    public RecommendationResponse recommendTracks(@RequestBody Map<String, Object> body) {
        String trackId = (String) body.get("trackId");
        int topK = (body.get("topK") != null) ? (int) body.get("topK") : 3;
        return spotifyService.recommendTracks(trackId, topK);
    }
}
