package com.ml.OpusML.ml;

import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.InputStream;
import java.util.ArrayList;

@Service
public class MoodModelService {

    private final Classifier model;
    private final ArrayList<String> classValues;

    public MoodModelService() {
        try {
            InputStream modelStream = getClass().getResourceAsStream("/models/mood_model.model");
            if (modelStream == null)
                throw new RuntimeException("Model file not found in /models/");

            model = (Classifier) SerializationHelper.read(modelStream);

            classValues = new ArrayList<>();
            classValues.add("Calm");
            classValues.add("Happy");
            classValues.add("Sad");
            classValues.add("Energetic");

            System.out.println("Mood model loaded successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load mood model", e);
        }
    }

    public String predictMood(double tempo, double energy, double valence,
                              double acousticness, double instrumentalness) {
        try {
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("energy"));
            attributes.add(new Attribute("tempo"));
            attributes.add(new Attribute("valence"));
            attributes.add(new Attribute("acousticness"));
            attributes.add(new Attribute("instrumentalness"));
            Attribute classAttr = new Attribute("mood", classValues);
            attributes.add(classAttr);

            Instances data = new Instances("MoodData", attributes, 0);
            data.setClassIndex(data.numAttributes() - 1);

            double[] values = {energy, tempo, valence, acousticness, instrumentalness, 0};
            DenseInstance instance = new DenseInstance(1.0, values);
            instance.setDataset(data);
            data.add(instance);

            double result = model.classifyInstance(instance);
            return classValues.get((int) result);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}
