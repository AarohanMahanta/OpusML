package com.ml.OpusML.ml;

import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.util.ArrayList;

/**
 * This is a placeholder Weka model aimed to load successfully and return some dummy predictions when called to test
 * the functionality of the application.
 */
public class DummyModelGenerator {
    public static void main(String[] args) throws Exception {
        //First define features
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("tempo"));
        attributes.add(new Attribute("energy"));
        attributes.add(new Attribute("valence"));
        attributes.add(new Attribute("acousticness"));
        attributes.add(new Attribute("instrumentalness"));

        //Define class attribute (moods)
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("Calm");
        classValues.add("Happy");
        classValues.add("Sad");
        classValues.add("Energetic");
        Attribute classAttr = new Attribute("mood", classValues);
        attributes.add(classAttr);

        //Create empty dataset
        Instances dataset = new Instances("MoodDataset", attributes, 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);

        //Dummy instances for training purposes
        double[] vals = new double[dataset.numAttributes()];
        vals[0] = 120;     //tempo
        vals[1] = 0.8;     //energy
        vals[2] = 0.7;     //valence
        vals[3] = 0.1;     //acousticness
        vals[4] = 0.0;     //instrumentalness
        vals[5] = classValues.indexOf("Happy"); //class label

        dataset.add(new DenseInstance(1.0, vals));

        //Train a dummy RandomForest classifier on empty dataset
        RandomForest classifier = new RandomForest();
        classifier.buildClassifier(dataset);
        //Will not actually learn, but can classify dummy instances

        //Save the model
        SerializationHelper.write("src/main/resources/models/mood_model.model", classifier);

        System.out.println("Dummy Weka model created!");
    }
}
