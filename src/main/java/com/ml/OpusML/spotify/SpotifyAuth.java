package com.ml.OpusML.spotify;

import java.net.URI;
import java.net.http.*;
import java.util.Base64;
import org.json.JSONObject;

public class SpotifyAuth {

    private static final String CLIENT_ID = "<your_client_id>";
    private static final String CLIENT_SECRET = "<your_client_secret>";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    public static String getAccessToken() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String auth = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        return json.getString("access_token");
    }
}
