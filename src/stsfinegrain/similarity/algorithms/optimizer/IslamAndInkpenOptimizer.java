package stsfinegrain.similarity.algorithms.optimizer;

import stsfinegrain.similarity.algorithms.ParameterizedSimilarityCalculation;
import stsfinegrain.similarity.algorithms.optimizer.spacesearch.SearchState;
import stsfinegrain.utilities.MathCalc;

/**
* @author Vuk Batanović
*/
public class IslamAndInkpenOptimizer extends ParameterizedSimilarityCalculationOptimizer {

	private static double MIN_STRING_SIMILARITY_WEIGHT = 0.3;
	private static double MAX_STRING_SIMILARIY_WEIGHT = 0.7;
	private static double WEIGHT_STEP = 0.1;
	
	private OptimizationResults optimizationResults = new OptimizationResults();
	
	@Override
	public OptimizationResults optimizeParameters(ParameterizedSimilarityCalculation[] trainCalcs, ParameterizedSimilarityCalculation[] testCalcs, int foldNum) {
		for (double stringSimilarityWeight = MIN_STRING_SIMILARITY_WEIGHT; stringSimilarityWeight <= MAX_STRING_SIMILARIY_WEIGHT; stringSimilarityWeight += WEIGHT_STEP) {
         	double [] trainScores = calculateSimilarityScores (new SearchState(stringSimilarityWeight), trainCalcs);
         	double [] testScores = calculateSimilarityScores (new SearchState(stringSimilarityWeight), testCalcs);
         	double [] initialTrainScores = getInitialScores(trainCalcs);
         	double [] initialTestScores = getInitialScores(testCalcs);
         	double trainPearson = MathCalc.pearsonCorrelation(initialTrainScores, trainScores);
         	double testPearson = MathCalc.pearsonCorrelation(initialTestScores, testScores);
         	if (trainPearson > optimizationResults.getBestPearsonTrain()) {
         		optimizationResults.setBestPearsonTrain(trainPearson);
         		optimizationResults.setFinalPearson(testPearson);
         		optimizationResults.getBestParametersTrain().clear();
         		optimizationResults.getBestParametersTrain().add(new SearchState(stringSimilarityWeight));
         		optimizationResults.getSelectedParameters().clear();
         		optimizationResults.getSelectedParameters().add(new SearchState(stringSimilarityWeight));
         	}
         	else if (trainPearson == optimizationResults.getBestPearsonTrain()) {
         		optimizationResults.getBestParametersTrain().add(new SearchState(stringSimilarityWeight));
         		if (testPearson > optimizationResults.getFinalPearson()) {
         			optimizationResults.setFinalPearson(testPearson);
 	        		optimizationResults.getSelectedParameters().clear();
 	        		optimizationResults.getSelectedParameters().add(new SearchState(stringSimilarityWeight));
         		}
         		else if (testPearson == optimizationResults.getFinalPearson())
         			optimizationResults.getSelectedParameters().add(new SearchState(stringSimilarityWeight));
         	}
         	if (testPearson > optimizationResults.getBestPearsonTest()) {
         		optimizationResults.setBestPearsonTest(testPearson);
         		optimizationResults.getBestParametersTest().clear();
         		optimizationResults.getBestParametersTest().add(new SearchState(stringSimilarityWeight));
         	}
         	else if (testPearson == optimizationResults.getBestPearsonTest())
         		optimizationResults.getBestParametersTest().add(new SearchState(stringSimilarityWeight));
         }
		return optimizationResults;
	}
}
