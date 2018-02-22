package stsfinegrain.similarity;

import stsfinegrain.similarity.algorithms.SimilarityCalculation;
import stsfinegrain.similarity.algorithms.SimilarityCalculation.SimilarityAlg;
import stsfinegrain.utilities.TextCleaner;
import stsfinegrain.utilities.TextVectorizer;
import stsfinegrain.utilities.Word2VecModel;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Vuk Batanović
 */
public class SentenceSimilarity extends Thread {
    
    private void getSimilarityCalculation(String sent1,String sent2) throws IOException {
    	String [] sent1Tokens = TextCleaner.filter(sent1).split("\\s+");
        String [] sent2Tokens = TextCleaner.filter(sent2).split("\\s+");
        sim = SimilarityCalculation.createSimilarityCalculation(algorithmType, sent1Tokens, sent2Tokens, wordMsdPairs1, wordMsdPairs2, initialScore, vectorizer, model, termFrequencies);
    }
    
    private double initialScore;
    private String sent1, sent2;
    private String [] wordMsdPairs1, wordMsdPairs2;
    private int lineCnt;
    private SimilarityCalculation [] simCalcVector;
    private int [] currentThreads;
    private TextVectorizer vectorizer;
    private Word2VecModel model;
    private HashMap<String, Double> termFrequencies;
    private SimilarityCalculation sim;
    private SimilarityAlg algorithmType;
    
    public SentenceSimilarity (SimilarityAlg algorithmType, String initialScore, String sent1, String sent2, String[] msds1, String[] msds2, int lineCnt, SimilarityCalculation [] simCalcVector, int [] currentThreads, TextVectorizer vectorizer, Word2VecModel model, HashMap<String, Double> termFrequencies) {
    	this.algorithmType = algorithmType;
    	this.initialScore = Double.parseDouble(initialScore);
    	this.sent1 = sent1;
    	this.sent2 = sent2;
    	this.wordMsdPairs1 = msds1;
    	this.wordMsdPairs2 = msds2;
    	this.lineCnt = lineCnt;
    	this.simCalcVector = simCalcVector;
    	this.currentThreads = currentThreads;
    	this.vectorizer = vectorizer;
    	this.model = model;
    	this.termFrequencies = termFrequencies;
    }

	public void run() {
		try {
			getSimilarityCalculation(sent1, sent2);
			simCalcVector[lineCnt-1] = sim;
			synchronized (currentThreads) {
				currentThreads[0]--;
			}
			synchronized (simCalcVector)	{
				simCalcVector.notifyAll();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}