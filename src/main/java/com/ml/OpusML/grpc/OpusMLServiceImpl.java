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
                responseObserver.onError(Status.UNAVAILABLE.withDescription("Spotify API returned status: " + httpResponse.statusCode()).asRuntimeException());
                return;
            }

            JSONObject json = new JSONObject(httpResponse.body());
            JSONArray tracksJson = json.getJSONObject("tracks").getJSONArray("items");
            Spotify.SearchResponse.Builder respB = Spotify.SearchResponse.newBuilder();

            for (int i = 0; i < tracksJson.length(); i++) {
                JSONObject t = tracksJson.getJSONObject(i);
                Spotify.Track tr = Spotify.Track.newBuilder()
                        .setId(t.optString("id", ""))
                        .setName(t.optString("name", ""))
                        .setArtist(t.getJSONArray("artists").getJSONObject(0).optString("name", ""))
                        .setAlbum(t.getJSONObject("album").optString("name", ""))
                        .setUri(t.optString("uri", ""))
                        .build();
                respB.addTracks(tr);
            }

            responseObserver.onNext(respB.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("searchTracks failed", e);
            responseObserver.onError(Status.INTERNAL.withDescription("searchTracks failed").asRuntimeException());
        }
    }
}