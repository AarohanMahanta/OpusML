package com.ml.OpusML.service;

import com.ml.OpusML.spotify.SpotifyAuth;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SearchResponse searchTracks(String query, int limit) {
        try {
            String accessToken = SpotifyAuth.getAccessToken();
            String url = String.format("https://api.spotify.com/v1/search?q=%s&type=track&limit=%d",
                    query.replace(" ", "%20"), limit);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                logger.error("Spotify API error: {}", httpResponse.body());
                throw new RuntimeException("Spotify API returned error " + httpResponse.statusCode());
            }

            JSONObject json = new JSONObject(httpResponse.body());
            JSONArray items = json.getJSONObject("tracks").getJSONArray("items");

            List<Track> tracks = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject t = items.getJSONObject(i);
                Track track = new Track(
                        t.optString("id", ""),
                        t.optString("name", ""),
                        t.getJSONArray("artists").getJSONObject(0).optString("name", ""),
                        t.getJSONObject("album").optString("name", ""),
                        t.optString("uri", "")
                );
                tracks.add(track);
            }

            return new SearchResponse(tracks);

        } catch (Exception e) {
            logger.error("searchTracks failed", e);
            throw new RuntimeException("Failed to search tracks: " + e.getMessage());
        }
    }

    //Internal DTO Response
    public record Track(String id, String name, String artist, String album, String uri) {}
    public record SearchResponse(List<Track> tracks) {}
}
