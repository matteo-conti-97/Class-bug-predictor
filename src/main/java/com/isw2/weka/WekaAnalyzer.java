package com.isw2.weka;

import com.isw2.util.ClassifierType;
import com.isw2.util.Printer;
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
import java.util.Arrays;
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

    private List<Double> evualuateClassifier(Instances trainingSet, Instances testingSet, ClassifierType classifierType) throws Exception {
        List<Double> ret = new ArrayList<>();
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
        ret.add(kappa);
        LOGGER.info("Kappa = {}", kappa);
        double precision = eval.precision(classIndex);
        ret.add(precision);
        LOGGER.info("Precision = {}", precision);
        double recall = eval.recall(classIndex);
        ret.add(recall);
        LOGGER.info("Recall = {}", recall);
        double auc = eval.areaUnderROC(classIndex);
        ret.add(auc);
        LOGGER.info("AUC = {}\n\n", auc);
        return ret;
    }

    public void runExperiment(String project, int datasetNum) throws Exception {
        List<List<String>> datasetPath= generateDatasetPaths(project, datasetNum);
        List<Double> nvEval = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0));
        List<Double> ibkEval = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0));
        List<Double> rfEval = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0));
        int numDataset = datasetPath.size();
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

            List<Double> tmpNb;
            List<Double> tmpIbk;
            List<Double> tmpRf;
            //Evaluate classifiers
            tmpNb=evualuateClassifier(trainingSet, testingSet, ClassifierType.NAIVE_BAYES);
            tmpIbk=evualuateClassifier(trainingSet, testingSet, ClassifierType.IBK);
            tmpRf=evualuateClassifier(trainingSet, testingSet, ClassifierType.RANDOM_FOREST);
            //Sum up the evaluation results
            for(int i=0;i<4;i++){
                nvEval.set(i, nvEval.get(i)+tmpNb.get(i));
                ibkEval.set(i, ibkEval.get(i)+tmpIbk.get(i));
                rfEval.set(i, rfEval.get(i)+tmpRf.get(i));
            }
        }
        //Average the evaluation results
        List<Double> nbAvg = new ArrayList<>(Arrays.asList(nvEval.get(0)/numDataset, nvEval.get(1)/numDataset, nvEval.get(2)/numDataset, nvEval.get(3)/numDataset));
        List<Double> ibkAvg = new ArrayList<>(Arrays.asList(ibkEval.get(0)/numDataset, ibkEval.get(1)/numDataset, ibkEval.get(2)/numDataset, ibkEval.get(3)/numDataset));
        List<Double> rfAvg = new ArrayList<>(Arrays.asList(rfEval.get(0)/numDataset, rfEval.get(1)/numDataset, rfEval.get(2)/numDataset, rfEval.get(3)/numDataset));

        //Print the average evaluation results
        Printer.printMeanEval(nbAvg, ClassifierType.NAIVE_BAYES);
        Printer.printMeanEval(ibkAvg, ClassifierType.IBK);
        Printer.printMeanEval(rfAvg, ClassifierType.RANDOM_FOREST);
    }
}






