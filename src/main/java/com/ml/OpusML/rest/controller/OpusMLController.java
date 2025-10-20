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

    /**
     * Endpoint takes name of composer/piece from user with number of tracks to show.
     * @param query: name of composer/piece, keyword that is used fetch tracks from spotify/database.
     * @param limit: maximum number of tracks to be fetched, defaulting to 5 for no input.
     * @return is a SearchResponse DTO.
     */
    @GetMapping("/search")
    public SearchResponse searchTracks(@RequestParam String query, @RequestParam(defaultValue = "5") int limit) {
        return spotifyService.searchTracks(query, limit);
    }

    /**
     * Endpoint takes ID of track with the top number of recommended tracks to show.
     * @param trackId: ID of track selected by user, for which recommendations are to be shown.
     * @param topK: maximum number of recommendations to be shown i.e. the top K number of tracks sorted by similarity.
     * @return is a RecommendationResponse DTO.
     */
    @PostMapping("/recommend")
    public SpotifyService.RecommendationResponse recommendTracks(@RequestParam String trackId, @RequestParam(defaultValue = "3") int topK) {
        return spotifyService.recommendTracks(trackId, topK);
    }
}
