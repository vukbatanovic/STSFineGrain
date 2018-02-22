package stsfinegrain.similarity.algorithms;

import stsfinegrain.utilities.MathCalc;
import stsfinegrain.utilities.TextVectorizer;

/**
* @author Vuk Batanović
*/
public class WordOverlap extends SimilarityCalculation {

	private double similarityScore;
	
	public WordOverlap(String[] sent1, String[] sent2, double initialScore, TextVectorizer vectorizer){
		super(sent1, sent2, initialScore);
		int [] vector1 = vectorizer.getSentenceVector(sent1);
		int [] vector2 = vectorizer.getSentenceVector(sent2);
		similarityScore = MathCalc.cosineSimilarity(vector1, vector2);
	}
	
	@Override
	public double getSimilarity() {
		return similarityScore;
	}
}
