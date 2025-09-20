package com.ml.OpusML.service;

import opusml.Spotify;
import org.springframework.stereotype.Service;
import io.grpc.StatusRuntimeException;

@Service
public class SpotifyService {

    private final opusml.OpusMLServiceGrpc.OpusMLServiceBlockingStub stub;

    public SpotifyService(opusml.OpusMLServiceGrpc.OpusMLServiceBlockingStub stub) {
        this.stub = stub;
    }

    public Spotify.SearchResponse searchTracks(String query, int limit) {
        try {
            Spotify.SearchRequest request = Spotify.SearchRequest.newBuilder()
                    .setQuery(query)
                    .setLimit(limit)
                    .build();
            return stub.searchTracks(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("gRPC searchTracks failed", e);
        }
    }

    public Spotify.AnalyzeResponse analyzeTrack(String trackId) {
        try {
            Spotify.AnalyzeRequest request = Spotify.AnalyzeRequest.newBuilder()
                    .setTrackId(trackId)
                    .build();
            return stub.analyzeTrack(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("gRPC analyzeTrack failed", e);
        }
    }
}
