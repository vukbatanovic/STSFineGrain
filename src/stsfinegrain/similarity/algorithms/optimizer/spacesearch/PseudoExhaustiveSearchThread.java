package stsfinegrain.similarity.algorithms.optimizer.spacesearch;

import java.io.IOException;
import java.util.ArrayList;

import stsfinegrain.similarity.algorithms.optimizer.POSTSTSSHyperparameters;

/**
* @author Vuk Batanović
*/
public class PseudoExhaustiveSearchThread extends Thread {
	
	private PseudoExhaustiveSearch es;
	private POSSearchState state;
	private int pos1Index = -1, pos2Index = -1;
	private int [] currentThreads, waitObject;
	private int combinationNumber;
	
	private ArrayList<PseudoExhaustiveSearchPairHighPoint> candidatePairResults;
	private ArrayList<ArrayList<Double>> candidatePosWeights;
	private ArrayList<POSSearchState> posCombinations;
	
	public ArrayList<PseudoExhaustiveSearchPairHighPoint> getCandidatePairResults () { return candidatePairResults; }
	public ArrayList<POSSearchState> getPosCombinations () { return posCombinations; }
	public double getPearson () { return state.getCurrentPearson(); }
	
	public PseudoExhaustiveSearchThread(PseudoExhaustiveSearch es, POSSearchState s, int pos1Index, int pos2Index, int [] currentThreads, int [] waitObject) {
		this.es = new PseudoExhaustiveSearch (es,	s);
		this.state = s;
		this.pos1Index = pos1Index;
		this.pos2Index = pos2Index;
		this.currentThreads = currentThreads;
		this.waitObject = waitObject;
		this.candidatePairResults = new ArrayList<PseudoExhaustiveSearchPairHighPoint> ();
	}
	
	public PseudoExhaustiveSearchThread(PseudoExhaustiveSearch es, POSSearchState s, ArrayList<ArrayList<Double>> candidatePosWeights, int combNo, int [] currentThreads, int [] waitObject) {
		this.es = new PseudoExhaustiveSearch (es,	s);
		this.state = s;
		this.candidatePosWeights = candidatePosWeights;
		this.currentThreads = currentThreads;
		this.waitObject = waitObject;
		this.combinationNumber = combNo;
		posCombinations = new ArrayList<POSSearchState>();
	}
	
	@Override
	public void run() {
		try {
			if (pos1Index != -1 && pos2Index != -1)
				exploreStartingPairSpace();
			else
				explorePOSCombinations();
			synchronized (currentThreads) {
				currentThreads[0]--;
			}
			synchronized (waitObject) {
				waitObject.notifyAll();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void exploreStartingPairSpace ()  throws IOException {
		for (state.getPosWeights()[pos1Index] = POSTSTSSHyperparameters.LOWER_POS_BOUND; state.getPosWeights()[pos1Index] <= POSTSTSSHyperparameters.UPPER_POS_BOUND + POSTSTSSHyperparameters.ROUNDING_ERROR; state.getPosWeights()[pos1Index] += POSTSTSSHyperparameters.PARAMETER_STEP)
			for (state.getPosWeights()[pos2Index] = POSTSTSSHyperparameters.LOWER_POS_BOUND; state.getPosWeights()[pos2Index] <= POSTSTSSHyperparameters.UPPER_POS_BOUND + POSTSTSSHyperparameters.ROUNDING_ERROR; state.getPosWeights()[pos2Index] += POSTSTSSHyperparameters.PARAMETER_STEP) {
				double corr = es.calculatePearsonFromData (es.getTrainData(), state);
				processResults(corr);
				state.invertPOSPairingIndex(pos1Index, pos2Index);
				double corrInverted = es.calculatePearsonFromData (es.getTrainData(), state);
				processResults(corrInverted);
				state.invertPOSPairingIndex(pos1Index, pos2Index);
			}
	}
	
	private void explorePOSCombinations () throws IOException {
		int [] maxN_POSWeights = new int [candidatePosWeights.size()];
	    int [] currentN_POSWeights = new int [maxN_POSWeights.length];
	   
	    for (int i=0; i<candidatePosWeights.size(); i++) {
	    	state.getPosWeights()[i] = candidatePosWeights.get(i).get(0);
	    	maxN_POSWeights[i] = candidatePosWeights.get(i).size();
	    }
	   
	    while (true) {
		    double corr = es.calculatePearsonFromData (es.getTrainData(), state);
		    if (corr > state.getCurrentPearson()) {
		    	posCombinations.clear();
		    	state.setCurrentPearson(corr);
		    	posCombinations.add(state.clone());
		    	state.setCurrentPearson(corr);
		    }
		    else if (corr == state.getCurrentPearson()) {
		    	state.setCurrentPearson(corr);
		    	posCombinations.add(state.clone());
		    }
		    
	        // Iterate through all candidate POS weights
		    boolean more = false;
	    	int i = currentN_POSWeights.length-1;
	    	for (; i>=0; i--)
	    		if (currentN_POSWeights[i] != maxN_POSWeights[i] - 1) {
	    			more = true;
	    			break;
	    		}
	    	if (more) {
	    		currentN_POSWeights[i]++;
	    		for (int x=i+1; x<currentN_POSWeights.length; x++)
	    			currentN_POSWeights[x] = 0;
	    		for (int x=i; x<currentN_POSWeights.length; x++)
	    			state.getPosWeights()[x] = candidatePosWeights.get(x).get(currentN_POSWeights[x]);
	    		continue;
	    	}
	    	break;
	    }
		System.out.println("Exploration of POS interaction matrix value combination: " + combinationNumber + " completed.");
	}

	private void processResults (double corr) {
		if (corr >= state.getCurrentPearson()) {
			PseudoExhaustiveSearchPairHighPoint hp = new PseudoExhaustiveSearchPairHighPoint (corr, pos1Index, pos2Index, state.getPosWeights()[pos1Index], state.getPosWeights()[pos2Index], state.getPosPairingIndices()[pos1Index][pos2Index]);
			if (corr == state.getCurrentPearson())
				candidatePairResults.add(hp);
			else {
				candidatePairResults.clear();
				candidatePairResults.add(hp);
				state.setCurrentPearson(corr);
			}
		}	
	}
}
