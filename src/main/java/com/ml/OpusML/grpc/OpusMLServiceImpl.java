package com.ml.OpusML.grpc;

import com.ml.OpusML.spotify.SpotifyAuth;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import opusml.Spotify;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpusMLServiceImpl extends opusml.OpusMLServiceGrpc.OpusMLServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(OpusMLServiceImpl.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void searchTracks(Spotify.SearchRequest request, StreamObserver<Spotify.SearchResponse> responseObserver) {
        try {
            String accessToken = SpotifyAuth.getAccessToken();
            String url = String.format("https://api.spotify.com/v1/search?q=%s&type=track&limit=%d",
                    request.getQuery().replace(" ", "%20"), request.getLimit());

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                logger.error("Spotify API error: status={}, body={}", httpResponse.statusCode(), httpResponse.body());
                responseObserver.onError(Status.UNAVAILABLE
                        .withDescription("Spotify API returned status: " + httpResponse.statusCode())
                        .asRuntimeException());
                return;
            }

            JSONObject json = new JSONObject(httpResponse.body());
            JSONArray tracksJson = json.getJSONObject("tracks").getJSONArray("items");
            Spotify.SearchResponse.Builder respB = Spotify.SearchResponse.newBuilder();

            for (int i = 0; i < tracksJson.length(); i++) {
                JSONObject t = tracksJson.getJSONObject(i);

                String trackId = t.optString("id", "");
                String trackName = t.optString("name", "");
                String artist = t.getJSONArray("artists").getJSONObject(0).optString("name", "");

                logger.info("🔍 Track: {} by {} | ID: {}", trackName, artist, trackId);

                Spotify.Track tr = Spotify.Track.newBuilder()
                        .setId(trackId)
                        .setName(trackName)
                        .setArtist(artist)
                        .setAlbum(t.getJSONObject("album").optString("name", ""))
                        .setUri(t.optString("uri", ""))
                        .build();
                respB.addTracks(tr);

                if (!trackId.isEmpty()) {
                    logger.info("Starting background thread for track: {}", trackId);
                    new Thread(() -> addTrackToPythonDB(trackId, trackName, artist)).start();
                } else {
                    logger.info("Skipping track - missing ID: {}", trackId);
                }
            }

            responseObserver.onNext(respB.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("searchTracks failed", e);
            responseObserver.onError(Status.INTERNAL.withDescription("searchTracks failed").asRuntimeException());
        }
    }

    @Override
    public void getSimilarTracks(Spotify.SimilarTracksRequest request,
                                 StreamObserver<Spotify.SimilarTracksResponse> responseObserver) {
        try {
            String pythonUrl = "http://localhost:8000/recommend";
            String jsonBody = String.format(
                    "{\"track_id\": \"%s\", \"top_k\": %d}",
                    request.getTrackId(), request.getLimit()
            );

            HttpRequest pythonRequest = HttpRequest.newBuilder()
                    .uri(URI.create(pythonUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(pythonRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray recommendations = new JSONArray(response.body());
                Spotify.SimilarTracksResponse.Builder responseBuilder = Spotify.SimilarTracksResponse.newBuilder();

                for (int i = 0; i < recommendations.length(); i++) {
                    JSONObject rec = recommendations.getJSONObject(i);
                    Spotify.RecommendedTrack track = Spotify.RecommendedTrack.newBuilder()
                            .setTrackId(rec.getString("track_id"))
                            .setName(rec.getString("name"))
                            .setArtist(rec.getString("composer")) // <-- changed here
                            .setSimilarityScore((float) rec.getDouble("similarity_score"))
                            .build();
                    responseBuilder.addTracks(track);
                }

                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted();
            } else {
                throw new RuntimeException("Python service returned: " + response.statusCode());
            }

        } catch (Exception e) {
            logger.error("getSimilarTracks failed", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Recommendation failed: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    private void addTrackToPythonDB(String trackId, String name, String composer) {
        try {
            if (trackId.isEmpty()) {
                return;
            }

            String pythonUrl = "http://localhost:8000/tracks";
            String jsonBody = String.format(
                    "{\"track_id\": \"%s\", \"name\": \"%s\", \"composer\": \"%s\"}",
                    trackId,
                    name.replace("\"", "\\\""),
                    composer.replace("\"", "\\\"")
            );

            HttpRequest pythonRequest = HttpRequest.newBuilder()
                    .uri(URI.create(pythonUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(pythonRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                logger.info("Added track to Python DB: {} by {}", name, composer);
            } else {
                logger.warn("Failed to add track to Python DB: {} - {}", response.statusCode(), response.body());
            }

        } catch (Exception e) {
            logger.warn("Failed to add track {} to Python DB: {}", trackId, e.getMessage());
        }
    }
}
