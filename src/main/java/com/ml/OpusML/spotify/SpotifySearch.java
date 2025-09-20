package com.ml.OpusML.spotify;

import java.net.http.*;
import java.net.URI;
import org.json.*;

public class SpotifySearch {

    public static JSONArray search(String query, int limit, String token) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=" + limit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        return json.getJSONObject("tracks").getJSONArray("items");
    }
}
