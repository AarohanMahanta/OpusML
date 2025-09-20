package com.ml.OpusML.grpc;

import com.ml.OpusML.spotify.SpotifyAuth;
import com.ml.OpusML.spotify.SpotifySearch;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import opusml.Spotify;
import org.json.JSONArray;
import org.json.JSONObject;

public class OpusMLServer {

    static class OpusMLServiceImpl extends opusml.OpusMLServiceGrpc.OpusMLServiceImplBase {

        @Override
        public void searchTracks(Spotify.SearchRequest request, StreamObserver<Spotify.SearchResponse> responseObserver) {
            try {
                String token = SpotifyAuth.getAccessToken();
                JSONArray items = SpotifySearch.search(request.getQuery(), request.getLimit(), token);

                Spotify.SearchResponse.Builder builder = Spotify.SearchResponse.newBuilder();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject track = items.getJSONObject(i);
                    builder.addTracks(Spotify.Track.newBuilder()
                            .setId(track.getString("id"))
                            .setName(track.getString("name"))
                            .setArtist(track.getJSONArray("artists").getJSONObject(0).getString("name"))
                            .setAlbum(track.getJSONObject("album").getString("name"))
                            .setUri(track.getString("uri"))
                            .build());
                }

                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();

            } catch (Exception e) {
                responseObserver.onError(e);
            }
        }


        @Override
        public void analyzeTrack(Spotify.AnalyzeRequest request,
                                 StreamObserver<Spotify.AnalyzeResponse> responseObserver) {

            Spotify.AnalyzeResponse response = Spotify.AnalyzeResponse.newBuilder()
                    .setPredictedMood("Calm")
                    .setConfidence(0.85f)
                    .addRecommendations(Spotify.RecommendedTrack.newBuilder()
                            .setName("Recommended Track 1")
                            .setArtist("Artist 1")
                            .setUri("uri1")
                            .build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(9090)
                .addService(new OpusMLServiceImpl())
                .build()
                .start();

        System.out.println("OpusML gRPC Server started on port 9090");
        server.awaitTermination();
    }
}
