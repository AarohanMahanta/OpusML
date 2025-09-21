package com.ml.OpusML.grpc;

import com.ml.OpusML.spotify.SpotifyAuth;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import opusml.Spotify;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpusMLServiceImpl extends opusml.OpusMLServiceGrpc.OpusMLServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(OpusMLServiceImpl.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void searchTracks(Spotify.SearchRequest request,
                             StreamObserver<Spotify.SearchResponse> responseObserver) {
        try {
            //Get OAuth token
            String accessToken = SpotifyAuth.getAccessToken();

            //Build Spotify API search request
            String url = String.format(
                    "https://api.spotify.com/v1/search?q=%s&type=track&limit=%d",
                    request.getQuery().replace(" ", "%20"),
                    request.getLimit()
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            //Execute HTTP call
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                logger.error("Spotify API error: status={}, body={}", httpResponse.statusCode(), httpResponse.body());
                responseObserver.onError(Status.UNAVAILABLE
                        .withDescription("Spotify API returned status: " + httpResponse.statusCode())
                        .asRuntimeException());
                return;
            }

            //Parse JSON response
            JSONObject json = new JSONObject(httpResponse.body());
            JSONArray tracksJson = json.getJSONObject("tracks").getJSONArray("items");

            Spotify.SearchResponse.Builder responseBuilder = Spotify.SearchResponse.newBuilder();

            for (int i = 0; i < tracksJson.length(); i++) {
                JSONObject trackJson = tracksJson.getJSONObject(i);

                Spotify.Track track = Spotify.Track.newBuilder()
                        .setId(trackJson.optString("id", ""))
                        .setName(trackJson.optString("name", ""))
                        .setArtist(trackJson.getJSONArray("artists").getJSONObject(0).optString("name", ""))
                        .setAlbum(trackJson.getJSONObject("album").optString("name", ""))
                        .setUri(trackJson.optString("uri", ""))
                        .build();

                responseBuilder.addTracks(track);
            }

            //Send response
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (IOException | InterruptedException e) {
            logger.error("Spotify API request failed", e);
            responseObserver.onError(Status.UNAVAILABLE
                    .withDescription("Spotify API unavailable")
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Unexpected error in searchTracks", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unexpected error in searchTracks")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void analyzeTrack(Spotify.AnalyzeRequest request,
                             StreamObserver<Spotify.AnalyzeResponse> responseObserver) {
    }
}
