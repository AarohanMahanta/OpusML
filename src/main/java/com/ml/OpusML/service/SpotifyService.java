package com.ml.OpusML.service;

import com.ml.OpusML.spotify.SpotifyAuth;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final RestTemplate restTemplate;
    public SpotifyService() {
        this.restTemplate = new RestTemplate();
    }

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



    public RecommendationResponse recommendTracks(String trackId, int topK) {
        try {
            String url = UriComponentsBuilder
                    .fromUriString("http://0.0.0.0:8000/recommend")
                    .toUriString();

            // Request body for FastAPI POST
            Map<String, Object> body = Map.of(
                    "track_id", trackId,
                    "top_k", topK
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body);

            ResponseEntity<List<RecommendedTrack>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<RecommendedTrack>>() {}
            );

            return new RecommendationResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return new RecommendationResponse(List.of());
        }
    }


    //Internal DTO for search response
    public record Track(String id, String name, String artist, String album, String uri) {}
    public record SearchResponse(List<Track> tracks) {}

    //Internal DTO for recommendations
    public record RecommendedTrack(String track_id, String name, String composer, double similarity_score) {}
    public record RecommendationResponse(List<RecommendedTrack> recommendations) {}

}
