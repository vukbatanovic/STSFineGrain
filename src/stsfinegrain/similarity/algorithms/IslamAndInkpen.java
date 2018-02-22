package stsfinegrain.similarity.algorithms;

import stsfinegrain.similarity.StringSimilarity;
import stsfinegrain.similarity.algorithms.optimizer.spacesearch.SearchState;
import stsfinegrain.utilities.MathCalc;
import stsfinegrain.utilities.MatrixOperations;
import stsfinegrain.utilities.Word2VecModel;

import java.util.ArrayList;

/**
 * @author Vuk Batanović
 */
public class IslamAndInkpen extends ParameterizedSimilarityCalculation {
	
	protected Word2VecModel model;
	protected double stringSimilarityWeight = 0.5;
	protected StringSimilarity sSim;
	protected double[][] stringSimilarityMatrix, semanticSimilarityMatrix;
	
	public void setStringSimilarityWeight (double weight) {	stringSimilarityWeight = weight; }
	public double getStringSimilarityWeight() { return stringSimilarityWeight; }
	public double getSemanticSimilarityWeight() { return 1 - stringSimilarityWeight; }
	
	protected void createStringSimilarity() {
		sSim = new StringSimilarity(sent1, sent2);
		sSim.initialize();
		
        //        we construct m-sigmaM x n-sigmaN StringSimilarity matrix
        //        m is number of strings in the shorter sentence
        //        n is number of strings in the longer  sentence
        //        we count the number of delta(num of exact similarity strings)
        //        in sentence. We remove all delta tokens from both P and R
        stringSimilarityMatrix = sSim.getStringSimilarityMatrix();
	}
	
	public IslamAndInkpen(String[] sent1, String[] sent2, double initialScore, Word2VecModel model) {
		super(sent1, sent2, initialScore);
		this.model = model;
	}
	
	public void initialize() {
		createStringSimilarity();

        //        we construct m-sigmaM x n-sigmaN semantic similarity matrix
        semanticSimilarityMatrix = new double[sSim.getM()-sSim.getSigmaM()][sSim.getN()-sSim.getSigmaN()];
        ArrayList<double []> vectorList = new ArrayList<double []>();
        for(int i=0; i<sSim.getM()-sSim.getSigmaM(); i++) {
        	double[] v2 = model.getWordVector(sSim.getR().get(i));
        	for(int j=0; j<sSim.getN()-sSim.getSigmaN(); j++) {
                double[] v1 = null;
                if (i==0) {
                    // only the first time
                    v1 = model.getWordVector(sSim.getP().get(j));
                    vectorList.add(v1);
                }
                else
                	v1 = vectorList.get(j);
                if (v1 != null && v2 != null)
                	semanticSimilarityMatrix[i][j] = MathCalc.cosineSimilarity(v1, v2);
                else
                	semanticSimilarityMatrix[i][j] = 0.0;
            }
        }
	}
	
	protected double[][] constructJointMatrix () {
	    // we construct the joint matrix whose dimensions are: m-sigmaM X n-sigmaN
        double [][] weightedStringSimilarityMatrix = MatrixOperations.scalarMultiplication(stringSimilarityMatrix, stringSimilarityWeight, sSim.getM()-sSim.getSigmaM(), sSim.getN()-sSim.getSigmaN());
        double [][] weightedSemanticSimilarityMatrix = MatrixOperations.scalarMultiplication(semanticSimilarityMatrix, getSemanticSimilarityWeight(), sSim.getM()-sSim.getSigmaM(), sSim.getN()-sSim.getSigmaN());
        double [][] jointMatrix = MatrixOperations.addition(weightedStringSimilarityMatrix, weightedSemanticSimilarityMatrix, sSim.getM()-sSim.getSigmaM(), sSim.getN()-sSim.getSigmaN());
        return jointMatrix;
	}
	
	protected double calculateRoSum (double [][] jointMatrix) {
		// construct ro list, which consists of maximal elements from the joint matrix
        ArrayList<Double> ro = new ArrayList<Double>();
        int i = 0;
        while (jointMatrix != null) {
            jointMatrix = MatrixOperations.findMaxElemAndRemove(jointMatrix, sSim.getM()-sSim.getSigmaM()-i, sSim.getN()-sSim.getSigmaN()-i, ro);
            i++;
        }
        // make sum of all the elements in ro
        double roSum = 0;
        for (i=0; i<ro.size(); i++)
        	roSum += ro.get(i);
        return roSum;
	}
	
	protected double getDeltaValue () {
		return sSim.getSigmaM();
	}
	
	@Override
    public double getSimilarity() {
        double[][] jointMatrix = constructJointMatrix();
        double roSum = calculateRoSum(jointMatrix);
        double deltaValue = getDeltaValue();
        // make numerator which is: (roSum+deltaValue)x(m+n)
        double numerator = (roSum+deltaValue)*(sSim.getM()+sSim.getN());
        // make denominator
        double denominator = 2*sSim.getM()*sSim.getN();
        return numerator/denominator;
    }

	@Override
	public void applyParameters(SearchState p) {
		setStringSimilarityWeight(p.getStringSimilarityWeight());
	}
} 