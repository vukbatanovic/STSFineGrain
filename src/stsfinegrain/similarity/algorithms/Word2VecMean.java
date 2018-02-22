package stsfinegrain.similarity.algorithms;

import stsfinegrain.utilities.MathCalc;
import stsfinegrain.utilities.Word2VecModel;

/**
* @author Vuk Batanović
*/
public class Word2VecMean extends SimilarityCalculation {
	
	protected double similarityScore;
	protected double [] vectorWord2Vec1;
	protected double [] vectorWord2Vec2;
	
	public Word2VecMean (String[] sent1, String[] sent2, double initialScore, Word2VecModel model) {
		super(sent1, sent2, initialScore);
		vectorWord2Vec1 = calculateSentenceMeanVector(sent1, model);
		vectorWord2Vec2 = calculateSentenceMeanVector(sent2, model);
		similarityScore = MathCalc.cosineSimilarity(vectorWord2Vec1, vectorWord2Vec2);
	}
	
	private double[] calculateSentenceMeanVector (String [] sent, Word2VecModel model) {
		double [] vector = new double[model.getDimensions()];
		int count = 0;
		for (String word: sent)
			if (model.containsWord(word)) {
				count++;
				double [] wordVector = model.getWordVector(word);
				for (int i=0; i<wordVector.length; i++)
					vector[i] += wordVector[i];
			}
		for (int i=0; i<vector.length; i++)
			vector[i] /= count;
		return vector;
	}
	
	@Override
	public double getSimilarity() {
		return similarityScore;
	}

}
