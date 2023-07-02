package com.isw2.weka;

import com.isw2.util.ClassifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;


public class WekaAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WekaAnalyzer.class);
    private static final String DATASET_SET_PATH = "src/main/java/resource/arff/dataset_";

    List<List<String>> generateDatasetPaths(String project, int datasetNum) {
        List<List<String>> ret = new ArrayList<>();
        for (int i = 2; i <= datasetNum; i++) {
            List<String> dataset = new ArrayList<>();
            String trainingSetPath=DATASET_SET_PATH + project + "_" + i + "Train.arff";
            String testingSetPath=DATASET_SET_PATH + project + "_" + i + "Test.arff";
            dataset.add(trainingSetPath);
            dataset.add(testingSetPath);
            ret.add(dataset);
        }
        return ret;
    }

    private void evualuateClassifier(Instances trainingSet, Instances testingSet, ClassifierType classifierType) throws Exception {
        int numAttr = trainingSet.numAttributes();
        trainingSet.setClassIndex(numAttr - 1);
        testingSet.setClassIndex(numAttr - 1);
        Classifier classifier = null;

        switch(classifierType) {
            case NAIVE_BAYES:
                classifier = new NaiveBayes();
                LOGGER.info("Using Naive Bayes classifier");
                break;
            case RANDOM_FOREST:
                classifier = new RandomForest();
                LOGGER.info("Using Random Forest classifier");
                break;
            case IBK:
                classifier = new IBk();
                LOGGER.info("Using IBK classifier");
                break;
            default:
                LOGGER.error("Invalid classifier type");
        }

        assert classifier != null;
        classifier.buildClassifier(trainingSet);

        Evaluation eval = new Evaluation(testingSet);

        eval.evaluateModel(classifier, testingSet);
        int classIndex = 0; // First class, the buggy "YES"
        double kappa = eval.kappa();
        LOGGER.info("Kappa = {}", kappa);
        double precision = eval.precision(classIndex);
        LOGGER.info("Precision = {}", precision);
        double recall = eval.recall(classIndex);
        LOGGER.info("Recall = {}", recall);
        double auc = eval.areaUnderROC(classIndex);
        LOGGER.info("AUC = {}\n\n", auc);
    }

    public void runExperiment(String project, int datasetNum) throws Exception {
        List<List<String>> datasetPath= generateDatasetPaths(project, datasetNum);
        LOGGER.info("Analyzing project: {}", project);
        for(List<String> dataset: datasetPath){
            String trainingSetPath=dataset.get(0);
            String testingSetPath=dataset.get(1);
            LOGGER.info("Training set path: {}", trainingSetPath);
            LOGGER.info("Testing set path: {}\n", testingSetPath);
            //load datasets
            DataSource trainingSetSrc = new DataSource(dataset.get(0));
            Instances trainingSet = trainingSetSrc.getDataSet();
            DataSource testingSetSrc = new DataSource(dataset.get(1));
            Instances testingSet = testingSetSrc.getDataSet();

            evualuateClassifier(trainingSet, testingSet, ClassifierType.NAIVE_BAYES);
            evualuateClassifier(trainingSet, testingSet, ClassifierType.IBK);
            evualuateClassifier(trainingSet, testingSet, ClassifierType.RANDOM_FOREST);
        }

    }
}






