package stsfinegrain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import stsfinegrain.similarity.algorithms.SimilarityCalculation;

/**
* @author Vuk Batanović
*/
public class CVGenerator {
	
	public static int RANDOM_SEED = 1;
	
	private int foldNum;
	private SimilarityCalculation[] calcs;
	private ArrayList<SimilarityCalculation[]> trainingData = new ArrayList<SimilarityCalculation[]>();
	private ArrayList<SimilarityCalculation[]> testData = new ArrayList<SimilarityCalculation[]>();
	
    public ArrayList<SimilarityCalculation[]> getTrainingData() { return trainingData; }
    public ArrayList<SimilarityCalculation[]> getTestData() { return testData; }
	
    public CVGenerator (SimilarityCalculation [] calcs, int foldNum) {
    	this.calcs = calcs;
    	Arrays.sort(calcs);
    	this.foldNum = foldNum;
    	initializeCVFolds();
    }
    
    /**
     *  Sorted stratification
     */
    private void initializeCVFolds () {
    	for (int fold=0; fold<foldNum; fold++) {
			Random rng = new Random (fold + RANDOM_SEED);
			SimilarityCalculation [] test = new SimilarityCalculation[calcs.length/foldNum];
			SimilarityCalculation [] train = new SimilarityCalculation[calcs.length - test.length];
			int testIndex = 0;
			int trainIndex = 0;
			
			int upperBound = foldNum * ((calcs.length/foldNum) - 1);
			int i=0;
			for (; i<upperBound; i+=foldNum) {
				int pairNo = rng.nextInt(foldNum) + i;
				test[testIndex++] = calcs[pairNo];
				for (int j=i; j<i+foldNum; j++)
					if (j != pairNo)
						train[trainIndex++] = calcs[j];
			}
			int pairNo = rng.nextInt(calcs.length-upperBound) + i;
			test[testIndex++] = calcs[pairNo];
			for (int j=i; j<calcs.length; j++)
				if (j != pairNo)
					train[trainIndex++] = calcs[j];

			trainingData.add(train);
			testData.add(test);
    	}
    }
}
