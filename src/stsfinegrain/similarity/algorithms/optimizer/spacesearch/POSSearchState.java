package stsfinegrain.similarity.algorithms.optimizer.spacesearch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import stsfinegrain.similarity.algorithms.optimizer.POSTSTSSHyperparameters;

/**
* @author Vuk Batanović
*/
public class POSSearchState extends SearchState {
	private double [] posWeights;
	private int [][] posPairingIndices;
	private double currentPearson;

	public double [] getPosWeights () { return posWeights; }
	public int [][] getPosPairingIndices () { return posPairingIndices; }
	public double getCurrentPearson () { return currentPearson; }
	public void setCurrentPearson (double corr) { this.currentPearson = corr; }
	
	public POSSearchState(double currentPearson, double stringSimilarityWeight, double[] posWeights, int[][] posPairingIndices) {
		super(stringSimilarityWeight);
		this.currentPearson = currentPearson;
		this.posWeights = posWeights;
		this.posPairingIndices = posPairingIndices;
	}
	
	public POSSearchState (String fileName, int dimensions) throws IOException {
		BufferedReader br = new BufferedReader (new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		this.posWeights = new double[dimensions];
		this.posPairingIndices = new int[dimensions][dimensions];
		for (int i=0; i<dimensions; i++) {
			String line = br.readLine();
			String [] strs = line.split("\\s");
			posWeights[i] = Double.parseDouble(strs[strs.length-1]);
		}
		String line = br.readLine();
		String [] strs = line.split("\\s");
		this.stringSimilarityWeight = Double.parseDouble(strs[strs.length-1]);
		br.readLine();
		br.readLine();
		for (int i=0; i<dimensions; i++) {
			line = br.readLine();
			strs = line.split("\t");
			for (int j=0; j<dimensions; j++)
				posPairingIndices[i][j] = Integer.parseInt(strs[j]);
		}
		br.close();
	}
	
	public void invertPOSPairingIndex (int i, int j) {
		if (posPairingIndices[i][j] == POSTSTSSHyperparameters.POS_INDEX_FORBID_PAIRING)
			posPairingIndices[i][j] = posPairingIndices[j][i] = POSTSTSSHyperparameters.POS_INDEX_ALLOW_PAIRING;
		else
			posPairingIndices[i][j] = posPairingIndices[j][i] = POSTSTSSHyperparameters.POS_INDEX_FORBID_PAIRING;
	}
	
    public POSSearchState clone () {
    	double [] newPosWeights = new double [posWeights.length];
    	System.arraycopy(posWeights, 0, newPosWeights, 0, posWeights.length);
    	int [][] newPosPairingIndices = new int [posPairingIndices.length][posPairingIndices[0].length];
    	for (int i=0; i<posPairingIndices.length; i++)
    		System.arraycopy(posPairingIndices[i], 0, newPosPairingIndices[i], 0, posPairingIndices[i].length);
    	POSSearchState state = new POSSearchState (currentPearson, stringSimilarityWeight, newPosWeights, newPosPairingIndices);
    	return state;
    }
    
    public String toString () {
    	StringBuilder sb = new StringBuilder();
    	for (int x=0; x<posWeights.length; x++)
			sb.append(posWeights[x] + "\r\n");
    	sb.append("\r\n");
    	sb.append("Best weight for string similarity: " + stringSimilarityWeight + "\r\n");
    	sb.append("Best weight for semantic similarity: " + (1-stringSimilarityWeight));
    	sb.append("\r\n");
    	for (int x=0; x<posPairingIndices.length; x++) {
    		for (int y=0; y<posPairingIndices.length-1; y++)
    			sb.append(posPairingIndices[x][y] + "\t");
    		sb.append(posPairingIndices[x][posPairingIndices.length-1] + "\r\n");
    	}
    	sb.append("Maximal Pearson correlation on the training set is: " + currentPearson);
    	return sb.toString();
    }
    
    public boolean posEquals(POSSearchState state) {
		if (this.posWeights.length != state.posWeights.length)
			return false;
		for (int i=0; i<posWeights.length; i++)
			if (this.posWeights[i] != state.posWeights[i])
				return false;
		for (int i=0; i<posWeights.length; i++)
			for (int j=0; j<posWeights.length; j++)
				if (this.posPairingIndices[i][j] != state.posPairingIndices[i][j])
					return false;
		return true;
    }
}
