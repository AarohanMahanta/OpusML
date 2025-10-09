package com.ml.OpusML.ml;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Weka wrapper: loads a serialized Classifier and exposes predict() that returns
 * (label, confidence).
 *
 * IMPORTANT:
 * - The attribute order & class labels here MUST match the dataset/model you trained.
 * - Put your model file (e.g. "classical_mood.model") in resources or a configurable path.
 */
public class MoodClassifier {

    private final Classifier classifier;
    private final Instances datasetFormat;
    private final List<String> classLabels;

    public static class Prediction {
        public final String label;
        public final double confidence; // 0..1

        public Prediction(String label, double confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }

    // modelPath: filesystem path to serialized Weka model (.model)
    public MoodClassifier(String modelPath, List<String> classLabels) throws Exception {
        this.classLabels = new ArrayList<>(classLabels);

        // load classifier
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
            classifier = (Classifier) ois.readObject();
        }

        // define attributes in the exact order used during training
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("tempo"));
        attributes.add(new Attribute("energy"));
        attributes.add(new Attribute("valence"));
        attributes.add(new Attribute("acousticness"));
        attributes.add(new Attribute("instrumentalness"));
        attributes.add(new Attribute("danceability"));
        attributes.add(new Attribute("loudness"));
        attributes.add(new Attribute("mode")); // numeric 0/1 or use as nominal if you trained that way

        // class attribute (nominal)
        ArrayList<String> nominalLabels = new ArrayList<>(this.classLabels);
        attributes.add(new Attribute("mood", nominalLabels));

        datasetFormat = new Instances("OpusMLTest", attributes, 0);
        datasetFormat.setClassIndex(datasetFormat.numAttributes() - 1);
    }

    // Predict returns label and confidence (max probability from distributionForInstance)
    public Prediction predict(double tempo, double energy, double valence,
                              double acousticness, double instrumentalness,
                              double danceability, double loudness, double mode) throws Exception {

        Instance inst = new DenseInstance(datasetFormat.numAttributes());
        inst.setValue(datasetFormat.attribute("tempo"), tempo);
        inst.setValue(datasetFormat.attribute("energy"), energy);
        inst.setValue(datasetFormat.attribute("valence"), valence);
        inst.setValue(datasetFormat.attribute("acousticness"), acousticness);
        inst.setValue(datasetFormat.attribute("instrumentalness"), instrumentalness);
        inst.setValue(datasetFormat.attribute("danceability"), danceability);
        inst.setValue(datasetFormat.attribute("loudness"), loudness);
        inst.setValue(datasetFormat.attribute("mode"), mode);

        inst.setDataset(datasetFormat);

        double[] dist = classifier.distributionForInstance(inst); // probabilities for each class
        int bestIdx = 0;
        double bestProb = dist[0];
        for (int i = 1; i < dist.length; i++) {
            if (dist[i] > bestProb) {
                bestProb = dist[i];
                bestIdx = i;
            }
        }
        String predictedLabel = datasetFormat.classAttribute().value(bestIdx);
        return new Prediction(predictedLabel, (float) bestProb);
    }
}
