package com.ml.OpusML.rest.controller;

import com.ml.OpusML.service.SpotifyService;
import com.ml.OpusML.service.SpotifyService.SearchResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
public class OpusMLController {

    private final SpotifyService spotifyService;

    public OpusMLController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/search")
    public SearchResponse searchTracks(@RequestParam String query, @RequestParam(defaultValue = "5") int limit) {
        return spotifyService.searchTracks(query, limit);
    }

    @PostMapping("/recommend")
    public SpotifyService.RecommendationResponse recommendTracks(@RequestParam String trackId, @RequestParam(defaultValue = "3") int topK) {
        return spotifyService.recommendTracks(trackId, topK);
    }
}
