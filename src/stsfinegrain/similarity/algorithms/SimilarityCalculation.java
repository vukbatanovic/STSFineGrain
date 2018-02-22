package stsfinegrain.similarity.algorithms;

import java.util.HashMap;

import stsfinegrain.utilities.TextVectorizer;
import stsfinegrain.utilities.Word2VecModel;

/**
 * @author Vuk Batanović
 */
public abstract class SimilarityCalculation implements Comparable<SimilarityCalculation> {
	
	public enum SimilarityAlg {WORD_OVERLAP, WORD2VEC_MEAN, WORD_OVERLAP_AND_WORD2VEC_MEAN, ISLAM_AND_INKPEN, LINSTSS, POST_STSS, POS_TF_STSS}
	
	public static SimilarityCalculation createSimilarityCalculation(SimilarityAlg type, String [] sent1, String [] sent2, String [] tags1,
			String [] tags2, double initialScore, TextVectorizer vectorizer, Word2VecModel model, HashMap<String, Double> termFrequencies) {
		switch(type) {
			case WORD_OVERLAP:
			default:
				return new WordOverlap(sent1, sent2, initialScore, vectorizer);
			case WORD2VEC_MEAN:
				return new Word2VecMean(sent1, sent2, initialScore, model);
			case WORD_OVERLAP_AND_WORD2VEC_MEAN:
				return new WordOverlapAndWord2VecMean(sent1, sent2, initialScore, vectorizer, model);
			case ISLAM_AND_INKPEN:
				IslamAndInkpen islamAndInkpen = new IslamAndInkpen(sent1, sent2, initialScore, model);
				islamAndInkpen.initialize();
				return islamAndInkpen;
			case LINSTSS:
				LInSTSS linstss = new LInSTSS(sent1, sent2, initialScore, model, termFrequencies);
				linstss.initialize();
				return linstss;
			case POST_STSS:
				POSTSTSS poststss = new POSTSTSS(sent1, sent2, tags1, tags2, initialScore, model);
				poststss.initialize();
				return poststss;
			case POS_TF_STSS:
				POSTFSTSS postfstss = new POSTFSTSS(sent1, sent2, tags1, tags2, initialScore, model, termFrequencies);
				postfstss.initialize();
				return postfstss;
		}
	}
	
	public static String getSimilarityAlgorithmName(SimilarityAlg type) {
		switch(type) {
			case WORD_OVERLAP:
			default:
				return "Word overlap";
			case WORD2VEC_MEAN:
				return "Mean of word2vec word vectors";
			case WORD_OVERLAP_AND_WORD2VEC_MEAN:
				return "Mixture of word overlap and the mean of word2vec word vectors";
			case ISLAM_AND_INKPEN:
				return "Islam and Inkpen method";
			case LINSTSS:
				return "LInSTSS";
			case POST_STSS:
				return "POST STSS";
			case POS_TF_STSS:
				return "POS-TF STSS";
		}
	}
	
	public static SimilarityAlg getSimilarityCalculationType (SimilarityCalculation sim) {
		// Ordering is important due to inheritance
		if (sim instanceof POSTFSTSS)	
			return SimilarityAlg.POS_TF_STSS;
		if (sim instanceof POSTSTSS)	
			return SimilarityAlg.POST_STSS;
		else if (sim instanceof LInSTSS)
			return SimilarityAlg.LINSTSS;
		else if (sim instanceof IslamAndInkpen)
			return SimilarityAlg.ISLAM_AND_INKPEN;
		else if (sim instanceof WordOverlapAndWord2VecMean)
			return SimilarityAlg.WORD_OVERLAP_AND_WORD2VEC_MEAN;
		else if (sim instanceof Word2VecMean)
			return SimilarityAlg.WORD2VEC_MEAN;
		else if (sim instanceof WordOverlap)
			return SimilarityAlg.WORD_OVERLAP;
		else
			return null;
	}
	
	protected String [] sent1, sent2;
	protected double initialScore;
	
	public double getInitialScore () { return initialScore; }
	
	public SimilarityCalculation(String[] sent1, String[] sent2, double initialScore) {
		this.sent1 = sent1;
		this.sent2 = sent2;
		this.initialScore = initialScore;
	}
	
	public abstract double getSimilarity();
	
	public int compareTo(SimilarityCalculation sim) {
		if (this.initialScore < sim.initialScore)
			return -1;
		else if (this.initialScore == sim.initialScore)
			return 0;
		else
			return 1;
	}
} 