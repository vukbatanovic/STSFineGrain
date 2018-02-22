package stsfinegrain.similarity.algorithms.optimizer;

import stsfinegrain.languagespecific.POSMapping;
import stsfinegrain.similarity.algorithms.ParameterizedSimilarityCalculation;
import stsfinegrain.similarity.algorithms.SimilarityCalculation.SimilarityAlg;
import stsfinegrain.similarity.algorithms.optimizer.spacesearch.SearchState;

/**
* @author Vuk Batanović
*/
public abstract class ParameterizedSimilarityCalculationOptimizer {
	
	public static ParameterizedSimilarityCalculationOptimizer createSimilarityCalculation(SimilarityAlg type, POSMapping posMapping) {
		switch(type) {
			case POST_STSS:
			case POS_TF_STSS:
				return new POSTSTSSOptimizer(posMapping);
			case ISLAM_AND_INKPEN:
			case LINSTSS:
				return new IslamAndInkpenOptimizer();
			default:
				return null;
		}
	}
	
	public abstract OptimizationResults optimizeParameters(ParameterizedSimilarityCalculation [] trainCalcs, ParameterizedSimilarityCalculation [] testCalcs, int foldNum);
	
    protected double [] calculateSimilarityScores (SearchState p, ParameterizedSimilarityCalculation [] calcs) {
        double [] simScores = new double[calcs.length];
    	int i=0;
    	for (ParameterizedSimilarityCalculation sim: calcs)
            simScores[i++] = sim.getSimilarity(p);
    	return simScores;
    }
    
    protected double [] getInitialScores(ParameterizedSimilarityCalculation [] calcs) {
    	double [] initialScores = new double[calcs.length];
        for (int i=0; i<calcs.length; i++)
        	initialScores[i] = calcs[i].getInitialScore();
        return initialScores;
    }
}
