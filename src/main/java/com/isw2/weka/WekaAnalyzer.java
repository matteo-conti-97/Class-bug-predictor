package com.isw2.weka;

import com.isw2.util.ClassifierType;
import com.isw2.util.CsvHandler;
import com.isw2.util.ExperimentType;
import com.isw2.util.Printer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.classifiers.meta.FilteredClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class WekaAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WekaAnalyzer.class);
    private static final String DATASET_SET_PATH = "src/main/java/resource/arff/dataset_";
    //private static final String[] FEATURES={ "#AuthorsInRelease", "LOC", "AvgChurnInRelease", "AvgChurnFromStart", "AvgLOCAddedInRelease", "AvgLOCAddedFromStart", "#RevisionInRelease", "#RevisionFromStart", "#BugFixInRelease", "#BugFixFromStart"};

    List<List<String>> generateDatasetPaths(String project, int datasetNum) {
        List<List<String>> ret = new ArrayList<>();
        int i=2;
        if(project.equals("zookeeper")) i=3; //Zookeeper jump release 2 cause of NaN values
        for (; i <= datasetNum; i++) {
            List<String> dataset = new ArrayList<>();
            String trainingSetPath=DATASET_SET_PATH + project + "_" + i + "Train.arff";
            String testingSetPath=DATASET_SET_PATH + project + "_" + i + "Test.arff";
            dataset.add(trainingSetPath);
            dataset.add(testingSetPath);
            ret.add(dataset);
        }
        return ret;
    }

    private List<Double> evualuateClassifier(Instances trainingSet, Instances testingSet, ClassifierType classifierType, FilteredClassifier fc, CostSensitiveClassifier csc) throws Exception {
        List<Double> ret = new ArrayList<>();
        Classifier classifier = null;

        switch(classifierType) {
            case NAIVE_BAYES:
                classifier = new NaiveBayes();
                LOGGER.info("\nUsing Naive Bayes classifier");
                break;
            case RANDOM_FOREST:
                classifier = new RandomForest();
                LOGGER.info("\nUsing Random Forest classifier");
                break;
            case IBK:
                classifier = new IBk();
                LOGGER.info("\nUsing IBK classifier");
                break;
            default:
                LOGGER.error("\nInvalid classifier type");
        }

        assert classifier != null;


        Evaluation eval = new Evaluation(testingSet);
        if(fc!=null) { //Uso il classificatore che fa sampling
            fc.setClassifier(classifier);
            fc.buildClassifier(trainingSet);
            eval.evaluateModel(fc, testingSet);
        }
        else if(csc!=null){
            csc.setClassifier(classifier);
            csc.buildClassifier(trainingSet);
            eval.evaluateModel(csc, testingSet);
        }
        else {
            classifier.buildClassifier(trainingSet);
            eval.evaluateModel(classifier, testingSet);
        }

        int classIndex = 0; // First class, the buggy "YES"
        double kappa = eval.kappa();
        ret.add(kappa);
        double precision = eval.precision(classIndex);
        ret.add(precision);
        double recall = eval.recall(classIndex);
        ret.add(recall);
        double auc = eval.areaUnderROC(classIndex);
        ret.add(auc);
        double[][] confusionMatrix = eval.confusionMatrix();
        double tp = confusionMatrix[0][0];
        double fn = confusionMatrix[0][1];
        double fp = confusionMatrix[1][0];
        double tn = confusionMatrix[1][1];
        ret.add(tp);
        ret.add(fp);
        ret.add(tn);
        ret.add(fn);
        LOGGER.info("KAPPA: {}, PRECISION: {}, RECALL: {}, AUC: {}\n TP: {}, FP: {}, TN: {}, FN: {}\n\n",
                ret.get(0), ret.get(1), ret.get(2), ret.get(3), ret.get(4), ret.get(5), ret.get(6), ret.get(7));
        return ret;
    }

    private List<Instances> featureSelection(Instances trainingSet, Instances testingSet) throws Exception {
        CfsSubsetEval subsetEval = new CfsSubsetEval();
        BestFirst bestFirstSrc = new BestFirst();
        bestFirstSrc.setDirection(new SelectedTag(BestFirst.TAGS_SELECTION[2].getID(), BestFirst.TAGS_SELECTION)); //Bidirectional ASSUNZIONE 24
        bestFirstSrc.setSearchTermination(10);
        AttributeSelection filter = new AttributeSelection();

        filter.setEvaluator(subsetEval);
        filter.setSearch(bestFirstSrc);
        filter.setInputFormat(trainingSet);
        Instances filteredTrainingSet = Filter.useFilter(trainingSet, filter);
        Instances filteredTestingSet = Filter.useFilter(testingSet, filter);

        //Set attribute of interest, the buggyness
        int numAttr = filteredTrainingSet.numAttributes();
        filteredTrainingSet.setClassIndex(numAttr - 1);
        filteredTestingSet.setClassIndex(numAttr - 1);
        // Print the selected attribute subset
        // lol int[] selectedAttributes = bestFirstSrc.search(subsetEval, trainingSet);
        /*lol LOGGER.info("Selected features: \n");
        lol for (int selectedAttribute : selectedAttributes) {
            lol LOGGER.info("Feature: {}", FEATURES[selectedAttribute]);
        lol }*/
        return Arrays.asList(filteredTrainingSet, filteredTestingSet);
    }

    private CostMatrix getCostMatrix(double cfn){
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, 1.0);
        costMatrix.setCell(0, 1, cfn);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }

    public void runExperiment(String project, int datasetNum, ExperimentType type) throws Exception {
        List<List<String>> datasetPath= generateDatasetPaths(project, datasetNum);
        List<Double> nvEval = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        List<Double> ibkEval = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        List<Double> rfEval = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        int cntNb = 0;
        int cntIbk = 0;
        int cntRf = 0;
        LOGGER.info("Analyzing project: {}", project);
        for(List<String> dataset: datasetPath){
            String trainingSetPath=dataset.get(0);
            String testingSetPath=dataset.get(1);
            LOGGER.info("Training set path: {}", trainingSetPath);
            LOGGER.info("Testing set path: {}\n", testingSetPath);

            //load datasets
            DataSource trainingSetSrc = new DataSource(dataset.get(0));
            DataSource testingSetSrc = new DataSource(dataset.get(1));
            Instances vanillaTrainingSet = trainingSetSrc.getDataSet();
            Instances vanillaTestingSet = testingSetSrc.getDataSet();
            Instances trainingSet = null;
            Instances testingSet = null;
            List<Instances> filteredSets = null;
            FilteredClassifier fc = null;
            String sampleSizePercentage = null;
            CostSensitiveClassifier csc = null;
            CostMatrix costMatrix1 = getCostMatrix(4.0);
            CostMatrix costMatrix2 = getCostMatrix(3.0);
            CostMatrix costMatrixLearning = getCostMatrix(18.7);


            switch(type){
                case FEATURE_SELECTION: //Feature selection backwards
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);
                    break;

                case FEATURE_SELECTION_WITH_UNDER_SAMPLING:
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);

                    SpreadSubsample spreadSubsample = new SpreadSubsample();
                    spreadSubsample.setInputFormat(trainingSet);
                    spreadSubsample.setOptions(new String[] {"-M", "1.0"});
                    fc = new FilteredClassifier();
                    fc.setFilter(spreadSubsample);
                    break;

                case FEATURE_SELECTION_WITH_OVER_SAMPLING:
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);

                    Resample resample = new Resample();
                    resample.setInputFormat(trainingSet);
                    sampleSizePercentage = computeSampleSizePercentage(trainingSet);
                    resample.setOptions(new String[] {"-B", "1.0", "-S", "1", "-Z", sampleSizePercentage});
                    fc = new FilteredClassifier();
                    fc.setFilter(resample);
                    break;

                case FEATURE_SELECTION_WITH_COST_SENSITIVE_CLASSIFIER_CFN_4:
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);
                    csc = new CostSensitiveClassifier();
                    csc.setMinimizeExpectedCost(true);
                    csc.setCostMatrix(costMatrix1);
                    break;

                case FEATURE_SELECTION_WITH_COST_SENSITIVE_CLASSIFIER_CFN_3:
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);
                    csc = new CostSensitiveClassifier();
                    csc.setMinimizeExpectedCost(true);
                    csc.setCostMatrix(costMatrix2);
                    break;

                case FEATURE_SELECTION_WITH_COST_SENSITIVE_LEARNING:
                    filteredSets = featureSelection(vanillaTrainingSet, vanillaTestingSet);
                    trainingSet = filteredSets.get(0);
                    testingSet = filteredSets.get(1);
                    csc = new CostSensitiveClassifier();
                    csc.setMinimizeExpectedCost(false);
                    csc.setCostMatrix(costMatrixLearning);
                    break;

                default:
                    trainingSet = vanillaTrainingSet;
                    testingSet = vanillaTestingSet;
                    int numAttr = trainingSet.numAttributes();
                    trainingSet.setClassIndex(numAttr - 1);
                    testingSet.setClassIndex(numAttr - 1);
                    break;
            }

            List<Double> tmpNb;
            List<Double> tmpIbk;
            List<Double> tmpRf;

            //Evaluate classifiers
            tmpNb=evualuateClassifier(trainingSet, testingSet, ClassifierType.NAIVE_BAYES, fc, csc);
            tmpIbk=evualuateClassifier(trainingSet, testingSet, ClassifierType.IBK, fc, csc);
            tmpRf=evualuateClassifier(trainingSet, testingSet, ClassifierType.RANDOM_FOREST, fc, csc);

            CsvHandler.writeOutData(project, trainingSetPath, type, tmpNb, tmpIbk, tmpRf);

            boolean someNanNb= checkNans(tmpNb);
            boolean someNanIbk= checkNans(tmpIbk);
            boolean someNanRf= checkNans(tmpRf);

            //Sum up the evaluation results ASSUNZIONE 25/26
            if(!someNanNb){
                sumEvalResults(nvEval, tmpNb);
                cntNb++;
            }
            if(!someNanIbk){
                sumEvalResults(ibkEval, tmpIbk);
                cntIbk++;
            }
            if(!someNanRf) {
                sumEvalResults(rfEval, tmpRf);
                cntRf++;
            }

        }
        //Average the evaluation results
        List<Double> nbAvg = computeMeanEval(nvEval, cntNb);
        List<Double> ibkAvg = computeMeanEval(ibkEval, cntIbk);
        List<Double> rfAvg = computeMeanEval(rfEval, cntRf);

        CsvHandler.writeOutData(project, "mean", type, nbAvg, ibkAvg, rfAvg);

        //Print the average evaluation results
        Printer.printMeanEval(nbAvg, ClassifierType.NAIVE_BAYES);
        Printer.printMeanEval(ibkAvg, ClassifierType.IBK);
        Printer.printMeanEval(rfAvg, ClassifierType.RANDOM_FOREST);
    }

    private void sumEvalResults(List<Double> eval, List<Double> tmp){
        for(int i=0;i<8;i++){
            eval.set(i, eval.get(i) + tmp.get(i));
        }
    }

    private boolean checkNans(List<Double> tmp){
        for(Double d: tmp){
            if(d.isNaN()) return true;
        }
        return false;
    }

    private String computeSampleSizePercentage(Instances trainingSet) {
        double ret;
        int positive=0;
        int negative=0;
        for (weka.core.Instance instance : trainingSet) {
            if(Objects.equals(instance.toString(trainingSet.classIndex()), "YES")) positive++;
            else negative++;
        }
        assert negative !=0;
        assert positive !=0;
        if(positive>negative) ret= 100*(positive - negative)/(double)negative;
        else ret= 100*(negative - positive)/(double)positive;
        return String.valueOf(ret);
    }

    private List<Double> computeMeanEval(List<Double> eval, int numDataset){
        return new ArrayList<>(Arrays.asList(eval.get(0)/numDataset, eval.get(1)/numDataset, eval.get(2)/numDataset, eval.get(3)/numDataset, eval.get(4)/numDataset, eval.get(5)/numDataset, eval.get(6)/numDataset, eval.get(7)/numDataset));
    }
}




