package stsfinegrain.similarity.algorithms;

import stsfinegrain.similarity.StringAndPOSSimilarity;
import stsfinegrain.similarity.algorithms.optimizer.spacesearch.SearchMethod;
import stsfinegrain.utilities.Word2VecModel;

/**
* @author Vuk Batanović
*/
public class POSTSTSS extends IslamAndInkpen {

	protected SearchMethod sm;
	protected String [] tags1;
	protected String [] tags2;
	
	public POSTSTSS (String[] sent1, String[] sent2, String [] tags1, String [] tags2, double initialScore, Word2VecModel model) {
		super(sent1, sent2, initialScore, model);
		this.tags1 = tags1;
		this.tags2 = tags2;
	}
	
	@Override
	protected void createStringSimilarity() {
		StringAndPOSSimilarity sSimPOS = new StringAndPOSSimilarity(sent1, sent2, tags1, tags2);
		sSimPOS.initialize();
		
        //        we construct m-sigmaM x n-sigmaN StringSimilarity matrix
        //        m is number of strings in the shorter sentence
        //        n is number of strings in the longer  sentence
        //        we count the number of delta(num of exact similarity strings)
        //        in sentence. We remove all delta tokens from both P and R
        this.stringSimilarityMatrix = sSimPOS.getStringSimilarityMatrix();
        this.sSim = sSimPOS;
	}
	
	@Override
	protected double[][] constructJointMatrix () {
		double [][] jointMatrix = super.constructJointMatrix();
		double [][] posWeightingMatrix = ((StringAndPOSSimilarity)sSim).getPOSSimilarityMatrix(sm);
        for(int i=0; i<sSim.getM()-sSim.getSigmaM(); i++)
        	for(int j=0; j<sSim.getN()-sSim.getSigmaN(); j++)
                jointMatrix[i][j] *= posWeightingMatrix[i][j];
        return jointMatrix;
	}
	
	@Override
	protected double getDeltaValue() {
		double delta=sSim.getSigmaM();
        double deltaValue = 0; 
        for (int i=0; i<delta; i++) {
        	String tag1 = ((StringAndPOSSimilarity)sSim).getSameWordTags1()[i];
        	String tag2 = ((StringAndPOSSimilarity)sSim).getSameWordTags2()[i];
           	double posScore = sm.getPOSSimilarity(tag1, tag2);
           	deltaValue += posScore;
        }
        return deltaValue;
	}
	
	@Override
    public double getSimilarity() {
		double dSim = super.getSimilarity();
        if (dSim <= 1)
        	return dSim;
        else return 1;
	}
	
	public synchronized double getSimilarity(SearchMethod sm) {
		this.sm = sm;
		return getSimilarity();
	}
}
