package stsfinegrain.similarity.algorithms;

import stsfinegrain.similarity.algorithms.optimizer.spacesearch.SearchState;

/**
* @author Vuk Batanović
*/
public abstract class ParameterizedSimilarityCalculation extends SimilarityCalculation {
	
	public ParameterizedSimilarityCalculation (String[] sent1, String[] sent2, double initialScore) {
		super(sent1, sent2, initialScore);
	}
	
	public abstract void applyParameters(SearchState p);
	
	public double getSimilarity(SearchState p) {
		applyParameters(p);
		return getSimilarity();
	}
}
