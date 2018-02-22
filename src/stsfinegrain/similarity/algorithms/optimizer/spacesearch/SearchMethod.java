package stsfinegrain.similarity.algorithms.optimizer.spacesearch;

import java.util.HashMap;

import stsfinegrain.languagespecific.POSMapping;
import stsfinegrain.similarity.algorithms.POSTSTSS;
import stsfinegrain.similarity.algorithms.optimizer.POSTSTSSHyperparameters;
import stsfinegrain.similarity.algorithms.optimizer.POSTSTSSHyperparameters.POSWeightingFunction;
import stsfinegrain.utilities.MathCalc;

/**
* @author Vuk Batanović
*/
public abstract class SearchMethod {

	protected POSMapping posMapping;
	protected POSTSTSSHyperparameters hyperparameters;
	protected POSTSTSS [] trainData, testData;
	protected POSSearchState state;
	
	public POSMapping getPosMapping() {	return posMapping; }
	public POSTSTSSHyperparameters getHyperparameters () { return hyperparameters; }
	public POSTSTSS [] getTrainData () { return trainData; }
	public POSTSTSS [] getTestData () { return testData; }
	
	public double getCurrentPearson () { return state.getCurrentPearson(); }
	public void setCurrentPearson (double corr) { state.setCurrentPearson(corr); }
	
	public String[] getPosNames() { return posMapping.getPosNames(); }
	public String transformMSDtoPOStag (String msd) { return posMapping.transformMSDtoPOStag(msd); }
	public abstract String [] getParameterNames();
	public abstract HashMap<String, Integer> getPosNameToIndexMap();
	public abstract HashMap<String, Integer> getPosPairingIndexMapping();
	public abstract HashMap<Integer, Integer> getPosPairingIndexMappingI();
	public abstract HashMap<Integer, Integer> getPosPairingIndexMappingJ();
	
	public SearchMethod (POSMapping posMapping, POSTSTSSHyperparameters hyperparameters, POSTSTSS[] trainData, POSTSTSS[] testData) {
		this.posMapping = posMapping;
		this.hyperparameters = hyperparameters;
		this.trainData = trainData;
		this.testData = testData;
	}
	
	public SearchMethod (SearchMethod sm, POSSearchState state) {
		this.posMapping = sm.getPosMapping();
		this.hyperparameters = sm.getHyperparameters();
		this.trainData = sm.getTrainData();
		this.testData = sm.getTestData();
		this.state = state;
	}

	public SearchMethod (SearchMethod sm, POSSearchState state, boolean expand) {
		this (sm, state);
		if (expand) {
			POSSearchState lowDimState = state;
			double [] highDimPosWeights = new double [posMapping.getHighDimensionSize()];
			int[][] highDimPosPairingIndices = new int [posMapping.getHighDimensionSize()][posMapping.getHighDimensionSize()];
			POSSearchState highDimState = new POSSearchState(lowDimState.getCurrentPearson(), lowDimState.getStringSimilarityWeight(), 
					highDimPosWeights, highDimPosPairingIndices);
			this.state = highDimState;
			
			for (int i=0; i<highDimPosWeights.length; i++) {
				String pos1 = getPosNames()[i].split("\t")[0].toLowerCase();
				int index1 = sm.getPosNameToIndexMap().get(pos1);
				highDimPosWeights[i] = lowDimState.getPosWeights()[index1];
				for (int j=i; j<highDimPosWeights.length; j++) {
					String pos2 = getPosNames()[j].split("\t")[0].toLowerCase();
					int index2 = sm.getPosNameToIndexMap().get(pos2);
					if (index1 != index2)
						highDimPosPairingIndices[i][j] = highDimPosPairingIndices[j][i] = lowDimState.getPosPairingIndices()[index1][index2];
					else
						highDimPosPairingIndices[i][j] = highDimPosPairingIndices[j][i] = POSTSTSSHyperparameters.POS_INDEX_ALLOW_PAIRING;
				}
			}
		}	
	}
	
	public double getPOSSimilarity (String tag1, String tag2) {
		HashMap<String, Integer> posNameToIndexMap = getPosNameToIndexMap();
		int index1 = posNameToIndexMap.get(transformMSDtoPOStag(tag1.toLowerCase()));
		int index2 = posNameToIndexMap.get(transformMSDtoPOStag(tag2.toLowerCase()));
    	if (index1 == index2)
    		return state.getPosWeights()[index1];
    	else {
    		if (state.getPosPairingIndices()[index1][index2] == POSTSTSSHyperparameters.POS_INDEX_FORBID_PAIRING)
    			return 0;
    		else {
    			double w1 = state.getPosWeights()[index1];
    			double w2 = state.getPosWeights()[index2];
    			return chooseWeightingFactor(w1, w2);
    		}
    	}
    }
	
	protected double chooseWeightingFactor (double w1, double w2) {
		if (hyperparameters.getPosWeightingFunction() == POSWeightingFunction.CHOOSE_BIGGER)
			return  w1 > w2 ? w1 : w2;
		else if (hyperparameters.getPosWeightingFunction() == POSWeightingFunction.CHOOSE_SMALLER)
			return  w1 < w2 ? w1 : w2;
		else if (hyperparameters.getPosWeightingFunction() == POSWeightingFunction.CHOOSE_ARITHMETIC_MEAN)
			return (w1+w2)/2;
		else if (hyperparameters.getPosWeightingFunction() == POSWeightingFunction.CHOOSE_GEOMETRIC_MEAN)
			return Math.sqrt(w1*w2);
		else
			return 2*w1*w2/(w1+w2);
	}

	public double calculatePearsonFromData (POSTSTSS[] data) {
        double [] simScores = new double[data.length];
        double [] initialScores = new double[data.length];
    	int i=0;
    	for (POSTSTSS sim: data) {
            simScores[i] = sim.getSimilarity(this);
            initialScores[i++] = sim.getInitialScore();
    	}
        return MathCalc.pearsonCorrelation(simScores, initialScores);
	}
	
	public double calculatePearsonFromData (POSTSTSS[] data, POSSearchState s) {
		POSSearchState backup = state;
		state = s;
		double result = calculatePearsonFromData(data);
		state = backup;
		return result;
	}
	
	/**
	 * Print out the final best parameter settings
	 */
	protected void printFinalResults (POSSearchState finalState) {
		String[] parameterNames = getParameterNames();
    	double trainCorr = calculatePearsonFromData(trainData, finalState);
    	double testCorr = calculatePearsonFromData(testData, finalState);
		for (int i=0; i<finalState.getPosWeights().length; i++)
			System.out.println("Best weight for " + parameterNames[i] + " is: " + finalState.getPosWeights()[i]);
		System.out.println("Best weight for string similarity: " + finalState.getStringSimilarityWeight());
		System.out.println("Best weight for semantic similarity: " + (1-finalState.getStringSimilarityWeight()));
    	for (int i=0; i<finalState.getPosPairingIndices().length; i++) {
    		for (int j=0; j<finalState.getPosPairingIndices().length-1; j++)
    			System.out.print(finalState.getPosPairingIndices()[i][j] + "\t");
    		System.out.println(finalState.getPosPairingIndices()[i][finalState.getPosPairingIndices().length-1]);
    	}
		System.out.println();
    	System.out.println("Maximal Pearson correlation on the training set: " + trainCorr);
		System.out.println ("Maximal Pearson correlation on the validation/test set: " + testCorr);
		System.out.println();
	}
}
