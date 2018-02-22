package stsfinegrain;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import stsfinegrain.languagespecific.POSMapping;
import stsfinegrain.similarity.algorithms.ParameterizedSimilarityCalculation;
import stsfinegrain.similarity.algorithms.SimilarityCalculation;
import stsfinegrain.similarity.algorithms.SimilarityCalculation.SimilarityAlg;
import stsfinegrain.similarity.algorithms.optimizer.OptimizationResults;
import stsfinegrain.similarity.algorithms.optimizer.ParameterizedSimilarityCalculationOptimizer;
import stsfinegrain.similarity.algorithms.optimizer.spacesearch.SearchState;
import stsfinegrain.utilities.MathCalc;

/**
 * @author Vuk Batanović
 */
public class Evaluator {

	public enum EvaluationMode {FULL_DATASET, CROSS_VALIDATION}
	
	public static String getEvaluationModeName (EvaluationMode evaluationMode) {
		switch(evaluationMode) {
			case FULL_DATASET:
			default:
				return "Evaluation on the full dataset";
			case CROSS_VALIDATION:
				return "Cross-validation";
		}
	}
	
	public static int PAIR_COUNT = 1192;
	private static int FOLD_NUM = 10;
	
    private String outputPath;
    private SimilarityCalculation [] allCalcs;
	private double [] initialScores = new double[PAIR_COUNT];
    private EvaluationMode evaluationMode;
    private SimilarityAlg similarityAlg;
    private POSMapping posMapping;
	private CVGenerator cvGenerator;
    
    private double finalPearson = 0;
    
    public Evaluator (String outputPath, SimilarityCalculation [] allCalcs, EvaluationMode evaluationMode, POSMapping posMapping) throws IOException {
    	this.outputPath = outputPath;
    	this.allCalcs = allCalcs;
    	Arrays.sort(allCalcs);
    	this.evaluationMode = evaluationMode;
    	if (evaluationMode == EvaluationMode.CROSS_VALIDATION)
    		cvGenerator = new CVGenerator(allCalcs, FOLD_NUM);
    	int i=0;
    	for (SimilarityCalculation sim: allCalcs)
            initialScores[i++] = sim.getInitialScore();
    	similarityAlg = SimilarityCalculation.getSimilarityCalculationType(allCalcs[0]);
    	this.posMapping = posMapping;
    }
    
    public double getFinalPearson () throws IOException {
    	if (evaluationMode == EvaluationMode.FULL_DATASET) {
	        double [] simScores = calculateSimilarityScores(allCalcs);
        	finalPearson = MathCalc.pearsonCorrelation(initialScores, simScores);
        }
    	else {
    		crossValidation();
    	}
    	return finalPearson;
    }
    
    private double [] calculateSimilarityScores (SimilarityCalculation [] calcs) {
        double [] simScores = new double[calcs.length];
    	int i=0;
    	for (SimilarityCalculation sim: calcs)
            simScores[i++] = sim.getSimilarity();
    	return simScores;
    }

    private void crossValidation () throws IOException {
    	PrintWriter pw = new PrintWriter(outputPath, "UTF-8");
    	
    	OptimizationResults[] optimizationResults = new OptimizationResults[FOLD_NUM];
        double avgBestPearsonTrain = 0, avgBestPearsonTest = 0;
    	
    	for (int fold=0; fold<FOLD_NUM; fold++) {
	        SimilarityCalculation [] trainCalcs = cvGenerator.getTrainingData().get(fold);
	        SimilarityCalculation [] testCalcs = cvGenerator.getTestData().get(fold);
	        
	        if (evaluationMode == EvaluationMode.CROSS_VALIDATION)
	        	if (! (trainCalcs[0] instanceof ParameterizedSimilarityCalculation)) {
		    		double [] initialTestScores = new double [testCalcs.length];
			        for (int i=0; i<testCalcs.length; i++)
			        	initialTestScores[i] = testCalcs[i].getInitialScore();
		        	finalPearson += MathCalc.pearsonCorrelation(initialTestScores, calculateSimilarityScores (testCalcs));
		        }
	        	else {
		        	ParameterizedSimilarityCalculation [] ptrainCalcs = new ParameterizedSimilarityCalculation[trainCalcs.length];
		        	for (int i=0; i<trainCalcs.length; i++)
		        		ptrainCalcs[i] = (ParameterizedSimilarityCalculation) trainCalcs[i];
		        	ParameterizedSimilarityCalculation [] ptestCalcs = new ParameterizedSimilarityCalculation[testCalcs.length];
		        	for (int i=0; i<testCalcs.length; i++)
		        		ptestCalcs[i] = (ParameterizedSimilarityCalculation) testCalcs[i];
	
		        	ParameterizedSimilarityCalculationOptimizer optimizer = ParameterizedSimilarityCalculationOptimizer.createSimilarityCalculation(similarityAlg, posMapping);
		        	optimizationResults[fold] = optimizer.optimizeParameters(ptrainCalcs, ptestCalcs, fold);
	        	}
        }
    	
    	if (evaluationMode == EvaluationMode.CROSS_VALIDATION)
        	if (! (allCalcs[0] instanceof ParameterizedSimilarityCalculation))
        		finalPearson /= FOLD_NUM;
        	else {
		    	for (int i=0; i<FOLD_NUM; i++) {
		    		avgBestPearsonTrain += optimizationResults[i].getBestPearsonTrain() / FOLD_NUM;
		    		avgBestPearsonTest += optimizationResults[i].getBestPearsonTest() / FOLD_NUM;
		    		finalPearson += optimizationResults[i].getFinalPearson() / FOLD_NUM;
	
			        pw.println("///  Fold " + (i+1) + "  ///");
			        pw.println("////////////////////////");
			        pw.println("///  Training folds  ///");
			        pw.println("Best Pearson correlation: " + optimizationResults[i].getBestPearsonTrain());
			        pw.print("Best parameters: ");
			        for (SearchState p: optimizationResults[i].getBestParametersTrain())
			        	pw.print(p + "\t");
			        pw.println();
			        pw.println("///  Test fold  ///");
			        pw.println("Best Pearson correlation: " + optimizationResults[i].getBestPearsonTest());
			        pw.print("Best parameters: ");
			        for (SearchState p: optimizationResults[i].getBestParametersTest())
			        	pw.print(p + "\t");
			        pw.println();
			        pw.println("Final Pearson correlation: " + optimizationResults[i].getFinalPearson());
			        pw.print("Selected parameters: ");
			        for (SearchState p: optimizationResults[i].getSelectedParameters())
			        	pw.print(p + "\t");
			        pw.println();
				}
		        pw.println();
		        pw.println("Average best Pearson correlation on training folds: " + avgBestPearsonTrain);
		        pw.println("Average best Pearson correlation on test folds: " + avgBestPearsonTest);
		        pw.println("Final average Pearson correlation on test folds: " + finalPearson);
		        pw.flush();
		        pw.close();
        	}
    }
}