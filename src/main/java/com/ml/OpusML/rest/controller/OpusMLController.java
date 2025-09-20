package com.ml.OpusML.rest.controller;

import com.ml.OpusML.service.SpotifyService;
import opusml.OpusMLServiceGrpc;
import opusml.Spotify;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/spotify")
public class OpusMLController {

    private final SpotifyService spotifyService;


    public OpusMLController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }


    @GetMapping("/search")
    public Spotify.SearchResponse searchTracks(@RequestParam String query, @RequestParam(defaultValue = "10") int limit) {
        return spotifyService.searchTracks(query, limit);
    }

    @GetMapping("/analyze/{id}")
    public Spotify.AnalyzeResponse analyzeTrack(@PathVariable("id") String trackId) {
        return spotifyService.analyzeTrack(trackId);
    }
}
