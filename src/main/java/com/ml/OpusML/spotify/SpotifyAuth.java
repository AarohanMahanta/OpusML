package com.ml.OpusML.spotify;

import java.net.http.*;
import java.net.URI;
import java.util.Base64;
import org.json.JSONObject;

public class SpotifyAuth {

    private static String cachedToken = null;
    private static long expiryTime = 0;

    public static String getAccessToken() throws Exception {
        if (cachedToken != null && System.currentTimeMillis() < expiryTime) {
            return cachedToken;
        }

        String clientId = System.getenv("SPOTIFY_CLIENT_ID");
        String clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET");

        if (clientId == null || clientSecret == null) {
            throw new RuntimeException("Missing Spotify client credentials in environment variables!");
        }

        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Spotify Auth failed: " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        cachedToken = json.getString("access_token");
        expiryTime = System.currentTimeMillis() + (json.getInt("expires_in") - 30) * 1000;

        return cachedToken;
    }
}
