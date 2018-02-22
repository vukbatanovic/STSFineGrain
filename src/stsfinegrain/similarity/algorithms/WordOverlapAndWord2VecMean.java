package stsfinegrain.similarity.algorithms;

import stsfinegrain.utilities.MathCalc;
import stsfinegrain.utilities.TextVectorizer;
import stsfinegrain.utilities.Word2VecModel;

/**
* @author Vuk Batanović
*/
public class WordOverlapAndWord2VecMean extends Word2VecMean {
	
	public WordOverlapAndWord2VecMean(String[] sent1, String[] sent2, double initialScore, TextVectorizer vectorizer, Word2VecModel model){
		super(sent1, sent2, initialScore, model);
		int [] vectorWordOverlap1 = vectorizer.getSentenceVector(sent1);
		int [] vectorWordOverlap2 = vectorizer.getSentenceVector(sent2);
		double [] vectorJoint1 = new double[vectorizer.getDictionarySize() + model.getDimensions()];
		double [] vectorJoint2 = new double[vectorizer.getDictionarySize() + model.getDimensions()];
		for (int i=0; i< vectorizer.getDictionarySize(); i++) {
			vectorJoint1[i] = vectorWordOverlap1[i];
			vectorJoint2[i] = vectorWordOverlap2[i];
		}
		for (int i=0; i< model.getDimensions(); i++) {
			vectorJoint1[i + vectorizer.getDictionarySize()] = vectorWord2Vec1[i];
			vectorJoint2[i + vectorizer.getDictionarySize()] = vectorWord2Vec2[i];
		}
		similarityScore = MathCalc.cosineSimilarity(vectorJoint1, vectorJoint2);
	}
}
