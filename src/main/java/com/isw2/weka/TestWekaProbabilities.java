package com.isw2.weka;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.evaluation.*;

public class TestWekaProbabilities {
  public static void main(String args[]) throws Exception{
    // load datasets
    Instances training = DataSource.read("C:\\Users\\matte\\Downloads\\breast-cancer-train.arff");
    Instances testing = DataSource.read("C:\\Users\\matte\\Downloads\\breast-cancer-test.arff");
    int numAttr = training.numAttributes();
    training.setClassIndex(numAttr - 1);
    testing.setClassIndex(numAttr - 1);
    // make sure they're compatible
    String msg = training.equalHeadersMsg(testing);
    if (msg != null)
      throw new Exception(msg);
    
    int numtesting = testing.numInstances();
    System.out.printf("There are %d test instances\n", numtesting);
    
    RandomForest classifier = new RandomForest();
    classifier.buildClassifier(training);
    
    // Loop over each test instance.
    for (int i = 0; i < numtesting; i++)
    {
        // Get the true class label from the instance's own classIndex.
        String trueClassLabel = 
            testing.instance(i).toString(testing.classIndex());
        System.out.println("TrueClassLabel" + trueClassLabel);

        // Make the prediction here.
        double predictionIndex = 
            classifier.classifyInstance(testing.instance(i)); 

        // Get the predicted class label from the predictionIndex.
        String predictedClassLabel =
            testing.classAttribute().value((int) predictionIndex);

        // Get the prediction probability distribution.
        double[] predictionDistribution = 
            classifier.distributionForInstance(testing.instance(i)); 

        // Print out the true label, predicted label, and the distribution.
        System.out.printf("%5d: true=%-10s, predicted=%-10s, distribution=", 
                          i, trueClassLabel, predictedClassLabel); 

        // Loop over all the prediction labels in the distribution.
	        for (int predictionDistributionIndex = 0; 
	             predictionDistributionIndex < predictionDistribution.length; 
	             predictionDistributionIndex++)
	        {
	            // Get this distribution index's class label.
	            String predictionDistributionIndexAsClassLabel = 
	                testing.classAttribute().value(
	                    predictionDistributionIndex);
	
	            // Get the probability.
	            double predictionProbability = 
	                predictionDistribution[predictionDistributionIndex];
	
	            System.out.printf("[%10s : %6.3f]", 
	                              predictionDistributionIndexAsClassLabel, 
	                              predictionProbability );
	        }
	        System.out.printf("\n");
    	}    
    Evaluation eval = new Evaluation(testing);	

	eval.evaluateModel(classifier, testing); 
	
	System.out.println("AUC = "+eval.areaUnderROC(1));
	System.out.println("Precision = "+eval.precision(1));
	System.out.println("Recall = "+eval.recall(1));
	
	
	
	
    }
}