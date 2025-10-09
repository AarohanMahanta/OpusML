package com.ml.OpusML.grpc;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;


import com.ml.OpusML.spotify.SpotifyAuth;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import opusml.Spotify;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpusMLServiceImpl extends opusml.OpusMLServiceGrpc.OpusMLServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(OpusMLServiceImpl.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final Classifier moodModel;

    public OpusMLServiceImpl() {
        Classifier loadedModel;
        try {
            loadedModel = (Classifier) weka.core.SerializationHelper.read("models/mood_model.model");
            logger.info("Loaded Weka model successfully.");
        } catch (Exception e) {
            logger.error("Failed to load Weka model.", e);
            loadedModel = null;
        }
        this.moodModel = loadedModel;
    }

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
        try {
            // 1. Get OAuth token
            String accessToken = SpotifyAuth.getAccessToken();

            // 2. Fetch audio features from Spotify API
            String url = String.format(
                    "https://api.spotify.com/v1/audio-features/%s",
                    request.getTrackId()
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                logger.error("Spotify API error fetching audio features: status={}, body={}", httpResponse.statusCode(), httpResponse.body());
                responseObserver.onError(Status.UNAVAILABLE
                        .withDescription("Spotify API returned status: " + httpResponse.statusCode())
                        .asRuntimeException());
                return;
            }

            JSONObject featuresJson = new JSONObject(httpResponse.body());

            //Extract features for ML
            double tempo = featuresJson.optDouble("tempo", 0);
            double energy = featuresJson.optDouble("energy", 0);
            double valence = featuresJson.optDouble("valence", 0);
            double acousticness = featuresJson.optDouble("acousticness", 0);
            double instrumentalness = featuresJson.optDouble("instrumentalness", 0);

            String predictedMood = "Unknown";
            float confidence = 0.0f;

            if (moodModel != null) {
                try {
                    // Define attributes in the same order used during training
                    ArrayList<Attribute> attributes = new ArrayList<>();
                    attributes.add(new Attribute("tempo"));
                    attributes.add(new Attribute("energy"));
                    attributes.add(new Attribute("valence"));
                    attributes.add(new Attribute("acousticness"));
                    attributes.add(new Attribute("instrumentalness"));

                    // Define class attribute (moods)
                    ArrayList<String> classValues = new ArrayList<>();
                    classValues.add("Calm");
                    classValues.add("Happy");
                    classValues.add("Sad");
                    classValues.add("Energetic");
                    Attribute classAttr = new Attribute("mood", classValues);
                    attributes.add(classAttr);

                    // Build dataset
                    Instances dataset = new Instances("TestInstance", attributes, 1);
                    dataset.setClassIndex(dataset.numAttributes() - 1);

                    // Create instance with Spotify features
                    DenseInstance instance = new DenseInstance(dataset.numAttributes());
                    instance.setValue(attributes.get(0), tempo);
                    instance.setValue(attributes.get(1), energy);
                    instance.setValue(attributes.get(2), valence);
                    instance.setValue(attributes.get(3), acousticness);
                    instance.setValue(attributes.get(4), instrumentalness);

                    dataset.add(instance);

                    // Classify instance
                    double labelIndex = moodModel.classifyInstance(dataset.firstInstance());
                    predictedMood = classValues.get((int) labelIndex);

                    // Get confidence from distribution
                    double[] distribution = moodModel.distributionForInstance(dataset.firstInstance());
                    confidence = (float) distribution[(int) labelIndex];

                } catch (Exception e) {
                    logger.error("Error running Weka model", e);
                }
            }


            // 5. Optionally, fetch recommendations from Spotify
            // Example: using features for seed_tracks or target_* params
            // We'll stub 3 dummy recommendations for now
            Spotify.RecommendedTrack rec1 = Spotify.RecommendedTrack.newBuilder()
                    .setName("Dummy Track 1")
                    .setArtist("Dummy Artist 1")
                    .setUri("spotify:track:dummy1")
                    .build();
            Spotify.RecommendedTrack rec2 = Spotify.RecommendedTrack.newBuilder()
                    .setName("Dummy Track 2")
                    .setArtist("Dummy Artist 2")
                    .setUri("spotify:track:dummy2")
                    .build();
            Spotify.RecommendedTrack rec3 = Spotify.RecommendedTrack.newBuilder()
                    .setName("Dummy Track 3")
                    .setArtist("Dummy Artist 3")
                    .setUri("spotify:track:dummy3")
                    .build();

            // 6. Build response
            Spotify.AnalyzeResponse response = Spotify.AnalyzeResponse.newBuilder()
                    .setPredictedMood(predictedMood)
                    .setConfidence(confidence)
                    .addRecommendations(rec1)
                    .addRecommendations(rec2)
                    .addRecommendations(rec3)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IOException | InterruptedException e) {
            logger.error("Spotify API request failed", e);
            responseObserver.onError(Status.UNAVAILABLE
                    .withDescription("Spotify API unavailable")
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Unexpected error in analyzeTrack", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unexpected error in analyzeTrack")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

}
