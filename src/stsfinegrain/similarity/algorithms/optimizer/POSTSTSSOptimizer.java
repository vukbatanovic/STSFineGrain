package stsfinegrain.similarity.algorithms.optimizer;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import stsfinegrain.CVGenerator;
import stsfinegrain.Experiment;
import stsfinegrain.languagespecific.POSMapping;
import stsfinegrain.similarity.algorithms.POSTSTSS;
import stsfinegrain.similarity.algorithms.ParameterizedSimilarityCalculation;
import stsfinegrain.similarity.algorithms.SimilarityCalculation;
import stsfinegrain.similarity.algorithms.optimizer.POSTSTSSHyperparameters.POSWeightingFunction;
import stsfinegrain.similarity.algorithms.optimizer.spacesearch.HillClimb;
import stsfinegrain.similarity.algorithms.optimizer.spacesearch.POSSearchState;
import stsfinegrain.similarity.algorithms.optimizer.spacesearch.PseudoExhaustiveSearch;
import stsfinegrain.similarity.algorithms.optimizer.spacesearch.SearchState;

/**
* @author Vuk Batanović
*/
public class POSTSTSSOptimizer extends ParameterizedSimilarityCalculationOptimizer {

	public static final int NESTED_CV_FOLD_NUMBER = 3;
	public static final String PSEUDOEXHAUSTIVESEARCH_RESULTS_DIR = "/PseudoExhaustiveSearch";
	public static final String HILLCLIMB_RESULTS_DIR = "/HillClimb";
	
	private POSMapping posMapping;
	private OptimizationResults optimizationResults = new OptimizationResults();
	
	public POSTSTSSOptimizer (POSMapping posMaping) {
		this.posMapping = posMaping;
	}
	
	@Override
	public OptimizationResults optimizeParameters(ParameterizedSimilarityCalculation[] trainCalcs, ParameterizedSimilarityCalculation[] testCalcs, int foldNum) {
		POSTSTSS[] trainData = new POSTSTSS[trainCalcs.length];
		POSTSTSS[] testData = new POSTSTSS[testCalcs.length];
		int i=0;
		for (ParameterizedSimilarityCalculation calc: trainCalcs)
			trainData[i++] = (POSTSTSS) calc;
		i=0;
		for (ParameterizedSimilarityCalculation calc: testCalcs)
			testData[i++] = (POSTSTSS) calc;
		
		ArrayList<POSTSTSSHyperparameters> bestHyperparameters = findBestHyperparameters(trainData);
		
		File directoryPES = new File(Experiment.RESULTS_DIR + PSEUDOEXHAUSTIVESEARCH_RESULTS_DIR);
		if (!directoryPES.exists())
			directoryPES.mkdirs();
		File directoryHC = new File(Experiment.RESULTS_DIR + HILLCLIMB_RESULTS_DIR);
		if (!directoryHC.exists())
			directoryHC.mkdirs();
		
		int hyperparameterSetIndex=0;
		for (POSTSTSSHyperparameters hyperparameterSettings : bestHyperparameters) {
			PseudoExhaustiveSearch pes = new PseudoExhaustiveSearch (posMapping, hyperparameterSettings, trainData, testData);
			try {
				pes.optimizeFull();
				File directoryHPComb = new File(Experiment.RESULTS_DIR + PSEUDOEXHAUSTIVESEARCH_RESULTS_DIR);
				if (!directoryHPComb.exists())
					directoryHPComb.mkdirs();
				int pesIndex=0;
				for (POSSearchState pesState: pes.getFinalBest()) {
					PrintWriter pwPES = new PrintWriter(Experiment.RESULTS_DIR + PSEUDOEXHAUSTIVESEARCH_RESULTS_DIR + "/FinalLowDimStates_" + foldNum + "_" + hyperparameterSetIndex + "_" + pesIndex + ".txt", "UTF-8");
					PrintWriter pwHC = new PrintWriter(Experiment.RESULTS_DIR + HILLCLIMB_RESULTS_DIR + "/FinalHighDimStates_" + foldNum + "_" + hyperparameterSetIndex + "_" + pesIndex + ".txt", "UTF-8");
					pwPES.print(pesState + "\n");
					HillClimb hc = new HillClimb(pes, pesState, true);
					POSSearchState bestState = hc.optimize();
					pwHC.print(bestState + "\n");
					if (bestState.getCurrentPearson() > optimizationResults.getBestPearsonTrain()) {
						optimizationResults.setBestPearsonTrain(bestState.getCurrentPearson());
						optimizationResults.setFinalPearson(hc.calculatePearsonFromData(hc.getTestData(), bestState));
						ArrayList<SearchState> newList = new ArrayList<SearchState>();
						newList.add(bestState);
						optimizationResults.setBestParametersTrain(newList);
					}
					pwPES.flush();
					pwPES.close();
					pwHC.flush();
					pwHC.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return optimizationResults;
	}

	private ArrayList<POSTSTSSHyperparameters> findBestHyperparameters (POSTSTSS[] trainCalcs) {
		CVGenerator cvGenerator = new CVGenerator(trainCalcs, NESTED_CV_FOLD_NUMBER);
		ArrayList<SimilarityCalculation[]> trainFoldsTemp = cvGenerator.getTrainingData();
		ArrayList<SimilarityCalculation[]> testFoldsTemp = cvGenerator.getTestData();
		ArrayList<POSTSTSS[]> trainFolds = new ArrayList<POSTSTSS[]> (trainFoldsTemp.size());
		ArrayList<POSTSTSS[]> validationFolds = new ArrayList<POSTSTSS[]> (testFoldsTemp.size());
		for (int fold=0; fold<NESTED_CV_FOLD_NUMBER; fold++) {
			POSTSTSS[] trainData = new POSTSTSS[trainFoldsTemp.get(fold).length];
			POSTSTSS[] testData = new POSTSTSS[testFoldsTemp.get(fold).length];
			int i=0;
			for (SimilarityCalculation calc: trainFoldsTemp.get(fold))
				trainData[i++] = (POSTSTSS) calc;
			i=0;
			for (SimilarityCalculation calc: testFoldsTemp.get(fold))
				testData[i++] = (POSTSTSS) calc;
			trainFolds.add(trainData);
			validationFolds.add(testData);
		}
		
		ArrayList<POSTSTSSHyperparameters> bestHyperparameters = new ArrayList<POSTSTSSHyperparameters>();
		ArrayList<Double> chosenPearsonOnTrainFolds = new ArrayList<Double>();
		double bestPearsonOnValidationFolds = 0;
		int hyperparameterCombinationCnt = 1;
		for (POSWeightingFunction posWeightingFunction: POSTSTSSHyperparameters.POS_WEIGHTING_FUNCTION_OPTIONS)
			for (double initialStringSimilarityWeight: POSTSTSSHyperparameters.INITIAL_STRING_SIMILARITY_WEIGHT_OPTIONS)
				for (double initialPosWeight: POSTSTSSHyperparameters.INITIAL_POS_WEIGHT_OPTIONS)
					for (int initialPosPairingValue: POSTSTSSHyperparameters.INITIAL_POS_INDEX_OPTIONS) 
						for (boolean useValueMinimization : POSTSTSSHyperparameters.VALUE_MINIMIZATION_OPTIONS) {
							System.out.println("\n==================================================");
							System.out.println("HYPERPARAMETER COMBINATION:\t" + hyperparameterCombinationCnt++);
							System.out.println("==================================================\n");
							POSTSTSSHyperparameters currentHyperparameters = new POSTSTSSHyperparameters(initialPosWeight, initialPosPairingValue, 
									initialStringSimilarityWeight, posWeightingFunction, useValueMinimization);
							double currentPearsonOnTrainFolds = 0;
							double currentPearsonOnValidationFolds = 0;
							for (int fold=0; fold<NESTED_CV_FOLD_NUMBER; fold++) {
								System.out.println("--------------------------------------------------");
								System.out.println("Nested fold:\t" + fold);
								System.out.println("--------------------------------------------------\n");
								PseudoExhaustiveSearch pes = new PseudoExhaustiveSearch (posMapping, currentHyperparameters, trainFolds.get(fold), validationFolds.get(fold));
								try {
									pes.optimizeFull();;
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								POSSearchState bestState = pes.getFinalBest().get(0);
	    						currentPearsonOnTrainFolds += bestState.getCurrentPearson() / NESTED_CV_FOLD_NUMBER;
	    						currentPearsonOnValidationFolds += pes.calculatePearsonFromData(pes.getTestData(), bestState) / NESTED_CV_FOLD_NUMBER;
		        			}
							
							if (currentPearsonOnValidationFolds > bestPearsonOnValidationFolds) {
								bestHyperparameters.clear();
								bestHyperparameters.add(currentHyperparameters);
								bestPearsonOnValidationFolds = currentPearsonOnValidationFolds;
								chosenPearsonOnTrainFolds.clear();
								chosenPearsonOnTrainFolds.add(currentPearsonOnTrainFolds);
							}
							else if (currentPearsonOnValidationFolds == bestPearsonOnValidationFolds) {
								bestHyperparameters.add(currentHyperparameters);
								chosenPearsonOnTrainFolds.add(currentPearsonOnTrainFolds);
							}
						}
		
		System.out.println("**************************************************");
		System.out.println("Best validation folds Pearson correlation:\t" + bestPearsonOnValidationFolds + "\n");
		System.out.println("Number of optimal hyperparameter settings: " + bestHyperparameters.size());
		for (int i=0; i<bestHyperparameters.size(); i++) {
			System.out.println("Training folds Pearson correlation:\t" + chosenPearsonOnTrainFolds.get(i));
			System.out.println(bestHyperparameters.get(i));
		}
		System.out.println("**************************************************");

		return bestHyperparameters;
	}
}
