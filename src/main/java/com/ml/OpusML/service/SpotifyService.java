package com.ml.OpusML.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import opusml.OpusMLServiceGrpc;
import opusml.Spotify;
import org.springframework.stereotype.Service;

@Service
public class SpotifyService {

    private final OpusMLServiceGrpc.OpusMLServiceBlockingStub stub;

    public SpotifyService() {
        //Create a gRPC channel to connect to OpusML gRPC server
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090) //same port as gRPC server
                .usePlaintext()
                .build();

        stub = OpusMLServiceGrpc.newBlockingStub(channel);
    }

    public Spotify.SearchResponse searchTracks(String query, int limit) {
        Spotify.SearchRequest request = Spotify.SearchRequest.newBuilder()
                .setQuery(query)
                .setLimit(limit)
                .build();

        return stub.searchTracks(request);
    }

}
