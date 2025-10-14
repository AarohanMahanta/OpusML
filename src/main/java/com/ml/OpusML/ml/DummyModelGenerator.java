package com.ml.OpusML.ml;

import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.util.ArrayList;

public class DummyModelGenerator {
    public static void main(String[] args) throws Exception {
        // Define features
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("energy"));
        attributes.add(new Attribute("tempo"));
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

        // Create dataset
        Instances dataset = new Instances("MoodDataset", attributes, 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);

        // Dummy training data (few varied instances)
        double[][] data = {
                {0.2, 60, 0.3, 0.9, 0.1, classValues.indexOf("Calm")},
                {0.8, 130, 0.8, 0.1, 0.0, classValues.indexOf("Happy")},
                {0.3, 70, 0.2, 0.8, 0.3, classValues.indexOf("Sad")},
                {0.9, 150, 0.9, 0.05, 0.0, classValues.indexOf("Energetic")}
        };

        for (double[] vals : data) {
            dataset.add(new DenseInstance(1.0, vals));
        }

        // Train classifier
        RandomForest classifier = new RandomForest();
        classifier.buildClassifier(dataset);

        // Save model
        SerializationHelper.write("src/main/resources/models/mood_model.model", classifier);
        System.out.println("Weka mood model trained & saved successfully.");
    }
}
