package com.ml.OpusML.spotify;

import java.net.URI;
import java.net.http.*;
import java.util.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotifyAuth {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyAuth.class);
    private static final String CLIENT_ID = "2f7a08299ce341aab734c5ae015d1cd4";
    private static final String CLIENT_SECRET = "ad8455cdddb14da0abf555560aa667bb";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    private static String accessToken = null;
    private static long expiresAt = 0;

    public static synchronized String getAccessToken() throws Exception {
        if (accessToken != null && System.currentTimeMillis() < expiresAt - 60000) {
            return accessToken;
        }

        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        String body = "grant_type=client_credentials";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get Spotify token: " + response.statusCode() + " - " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        accessToken = json.getString("access_token");
        int expiresIn = json.getInt("expires_in");
        expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);

        logger.info("Got new Spotify token, expires in {} seconds", expiresIn);
        return accessToken;
    }
}