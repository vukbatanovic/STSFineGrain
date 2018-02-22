package stsfinegrain.similarity.algorithms.optimizer.spacesearch;

import java.util.ArrayList;

import stsfinegrain.similarity.algorithms.optimizer.POSTSTSSHyperparameters;

/**
* @author Vuk Batanović
*/
public class HillClimbThread extends Thread {

	private HillClimb hillClimb;
	private double startingErrorLevel;
	private ArrayList<HillClimbMove> possibleMoves;
	private int i;
	private int [] currentThreads, waitObject;
	private POSSearchState state;
	
	public ArrayList<HillClimbMove> getPossibleMoves() { return possibleMoves; }
	
	public HillClimbThread(HillClimb hillClimb, POSSearchState state, int posIndex, int [] currentThreads, int [] waitObject) {
		this.hillClimb = new HillClimb(hillClimb, state);
		this.state = state;		
		this.i = posIndex;
		this.currentThreads = currentThreads;
		this.waitObject = waitObject;
		this.startingErrorLevel = hillClimb.getCurrentErrorLevel();
		this.possibleMoves = new ArrayList<HillClimbMove> ();
	}
	
	@Override
	public void run() {
		createPOSWeightMoves ();
		synchronized (currentThreads) {
			currentThreads[0]--;
		}
		synchronized (waitObject) {
			waitObject.notifyAll();
		}
	}
	
	private void createMove (POSSearchState s, int parameterIndex, double parameterChange, double oldParameterValue, boolean posIndexChange) {
		double errorLevel = POSTSTSSHyperparameters.MAXIMAL_PERFORMANCE_VALUE - s.getCurrentPearson();
		double errorReduction = startingErrorLevel - errorLevel;
		HillClimbMove move = new HillClimbMove (s, errorReduction, parameterIndex, parameterChange, oldParameterValue, posIndexChange);
		possibleMoves.add(move);
	}
	
	private void processWeightChange (int sign) {
		double corr = hillClimb.calculatePearsonFromData(hillClimb.getTrainData(), state);
		if (corr > state.getCurrentPearson()) {
			POSSearchState clone = state.clone();
			clone.setCurrentPearson(corr);
			createMove (clone, i, sign*POSTSTSSHyperparameters.PARAMETER_STEP, state.getPosWeights()[i]-sign*POSTSTSSHyperparameters.PARAMETER_STEP, false);
			for (int j=0; j<state.getPosWeights().length; j++) {
				if (i == j) continue;
				state.invertPOSPairingIndex(i, j);
				double corrInverted = hillClimb.calculatePearsonFromData(hillClimb.getTrainData(), state);
				if (corrInverted > corr) {
					clone = state.clone();
					clone.setCurrentPearson(corrInverted);
					createMove (clone, i, sign*POSTSTSSHyperparameters.PARAMETER_STEP, state.getPosWeights()[i]-sign*POSTSTSSHyperparameters.PARAMETER_STEP, true);
				}
				state.invertPOSPairingIndex(i, j);
			}
		}
	}
	
	private void processPOSIndexChange(int i) {
		for (int j=0; j<state.getPosWeights().length; j++) {
			if (i == j) continue;
			int oldParameterValue = state.getPosPairingIndices()[i][j];
			state.invertPOSPairingIndex(i, j);
			double corrInverted = hillClimb.calculatePearsonFromData(hillClimb.getTrainData(), state);
			if (corrInverted > state.getCurrentPearson()) {
				POSSearchState clone = state.clone();
				clone.setCurrentPearson(corrInverted);
				if (i < j)
					createMove (clone, POSTSTSSHyperparameters.POS_INDEX_PARAMETER_SHIFT + hillClimb.getPosPairingIndexMapping().get(i + " " + j), state.getPosPairingIndices()[i][j], oldParameterValue, true);
				else
					createMove (clone, POSTSTSSHyperparameters.POS_INDEX_PARAMETER_SHIFT + hillClimb.getPosPairingIndexMapping().get(j + " " + i), state.getPosPairingIndices()[i][j], oldParameterValue, true);
			}
			state.invertPOSPairingIndex(i, j);
		}
	}
	
	private void createPOSWeightMoves () {
		processPOSIndexChange(i);
		
		double oldWeight = state.getPosWeights()[i];
		state.getPosWeights()[i] -= POSTSTSSHyperparameters.PARAMETER_STEP;
		if (state.getPosWeights()[i] <= POSTSTSSHyperparameters.LOWER_POS_BOUND && state.getPosWeights()[i] >= POSTSTSSHyperparameters.LOWER_POS_BOUND - POSTSTSSHyperparameters.ROUNDING_ERROR)
			state.getPosWeights()[i] = POSTSTSSHyperparameters.LOWER_POS_BOUND;
		if (state.getPosWeights()[i] >= POSTSTSSHyperparameters.LOWER_POS_BOUND)
			processWeightChange(-1);
		
		state.getPosWeights()[i] = oldWeight;
		state.getPosWeights()[i] += POSTSTSSHyperparameters.PARAMETER_STEP;
		if (state.getPosWeights()[i] >= POSTSTSSHyperparameters.UPPER_POS_BOUND && state.getPosWeights()[i] <= POSTSTSSHyperparameters.UPPER_POS_BOUND + POSTSTSSHyperparameters.ROUNDING_ERROR)
			state.getPosWeights()[i] = POSTSTSSHyperparameters.UPPER_POS_BOUND;
		if (state.getPosWeights()[i] <= POSTSTSSHyperparameters.UPPER_POS_BOUND)
			processWeightChange(1);
		
		state.getPosWeights()[i] = oldWeight;
		state.getPosWeights()[i] -= 2*POSTSTSSHyperparameters.PARAMETER_STEP;
		if (state.getPosWeights()[i] <= POSTSTSSHyperparameters.LOWER_POS_BOUND && state.getPosWeights()[i] >= POSTSTSSHyperparameters.LOWER_POS_BOUND - POSTSTSSHyperparameters.ROUNDING_ERROR)
			state.getPosWeights()[i] = POSTSTSSHyperparameters.LOWER_POS_BOUND;
		if (state.getPosWeights()[i] >= POSTSTSSHyperparameters.LOWER_POS_BOUND)
			processWeightChange(-2);
		
		state.getPosWeights()[i] = oldWeight;
		state.getPosWeights()[i] += 2*POSTSTSSHyperparameters.PARAMETER_STEP;
		if (state.getPosWeights()[i] >= POSTSTSSHyperparameters.UPPER_POS_BOUND && state.getPosWeights()[i] <= POSTSTSSHyperparameters.UPPER_POS_BOUND + POSTSTSSHyperparameters.ROUNDING_ERROR)
			state.getPosWeights()[i] = POSTSTSSHyperparameters.UPPER_POS_BOUND;
		if (state.getPosWeights()[i] <= POSTSTSSHyperparameters.UPPER_POS_BOUND)
			processWeightChange(2);
	}
}
