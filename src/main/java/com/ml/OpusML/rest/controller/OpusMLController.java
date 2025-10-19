package com.ml.OpusML.rest.controller;

import com.ml.OpusML.service.SpotifyService;
import com.ml.OpusML.service.SpotifyService.SearchResponse;
import org.springframework.web.bind.annotation.*;

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


}
