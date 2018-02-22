package stsfinegrain.similarity.algorithms.optimizer.spacesearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import stsfinegrain.CVGenerator;
import stsfinegrain.Experiment;
import stsfinegrain.languagespecific.POSMapping.POSMappingDimensionality;
import stsfinegrain.similarity.algorithms.optimizer.POSTSTSSHyperparameters;

/**
* @author Vuk Batanović
*/
public class HillClimb extends SearchMethod {

	public static final double HILLCLIMB_STOPPING_ERROR_REDUCTION_THRESHOLD = 0.05;

	private ArrayList<HillClimbMove> possibleMoves = new ArrayList<HillClimbMove> (200);
	private ArrayList<HillClimbMove> moveHistory = new ArrayList<HillClimbMove> ();
	private	Random rng = new Random (CVGenerator.RANDOM_SEED);
	
	public HillClimb(SearchMethod sm, POSSearchState state) {
		super(sm, state);
	}
	
	public HillClimb (SearchMethod sm, POSSearchState state, boolean expand) {
		super(sm, state, expand);
	}
	
	public POSSearchState optimize () throws InterruptedException, IOException {
		setCurrentPearson(calculatePearsonFromData(trainData));
		System.out.println("Starting train correlation: " + getCurrentPearson());
		double testStart = calculatePearsonFromData(testData);
		System.out.println("Starting test correlation: " + testStart);

		int moveCnt = 0;
		while (true) {
			createStringSemanticWeightMoves();
			createPOSWeightMoves();
			HillClimbMove bestMove = selectBestMove();
			if (bestMove == null || !isErrorReductionSufficient(bestMove)) 
				break;
			moveCnt++;
			applyHillClimbMove(bestMove);
			
			double corr = calculatePearsonFromData(testData);
			bestMove.setTestPearson(corr);
			possibleMoves.clear();
			
			System.out.println("Move number: " + moveCnt);
			System.out.println("Selected best move:");
			if (bestMove.getParameterIndex() < POSTSTSSHyperparameters.POS_INDEX_PARAMETER_SHIFT)
				System.out.println("Parameter: " + getParameterNames()[bestMove.getParameterIndex()]);
			else {
				System.out.println("Parameter: POS-Index matrix, i = " + getPosNames()[getPosPairingIndexMappingI().get(bestMove.getParameterIndex() - POSTSTSSHyperparameters.POS_INDEX_PARAMETER_SHIFT)]
						+ " , j = " + getPosNames()[getPosPairingIndexMappingJ().get(bestMove.getParameterIndex()- POSTSTSSHyperparameters.POS_INDEX_PARAMETER_SHIFT)]);
				System.out.println("Old value: " + bestMove.getOldParameterValue());
			}
			System.out.println("Change: " + bestMove.getParameterChange());
			System.out.println("Error reduction: " + bestMove.getErrorReduction());
			System.out.println("New Train Pearson correlation: " + bestMove.getPearson());
			System.out.println("New Test Pearson correlation: " + bestMove.getTestPearson() + "\n");
		}
		printFinalResults();
		return state;
	}
	
	private void createStringSimilarityMove (POSSearchState s, int parameterIndex, double parameterChange, double oldParameterValue) {
		double errorLevel = POSTSTSSHyperparameters.MAXIMAL_PERFORMANCE_VALUE - s.getCurrentPearson();
		double errorReduction = getCurrentErrorLevel() - errorLevel;
		HillClimbMove move = new HillClimbMove (s, errorReduction, parameterIndex, parameterChange, oldParameterValue, false);
		possibleMoves.add(move);
	}
	
	private void createStringSemanticWeightMoves () throws InterruptedException {
		double stringWeight = state.getStringSimilarityWeight();
		if (stringWeight + POSTSTSSHyperparameters.PARAMETER_STEP <= POSTSTSSHyperparameters.UPPER_STRINGWEIGHT_BOUND + POSTSTSSHyperparameters.ROUNDING_ERROR) {
			state.setStringSimilarityWeight(stringWeight + POSTSTSSHyperparameters.PARAMETER_STEP);
			double corr = calculatePearsonFromData(trainData);
			if (corr > getCurrentPearson()) {
				POSSearchState s = state.clone();
				s.setCurrentPearson(corr);
				createStringSimilarityMove (s, getParameterNames().length-1, POSTSTSSHyperparameters.PARAMETER_STEP, stringWeight);			
			}
		}
		if (stringWeight  + 2*POSTSTSSHyperparameters.PARAMETER_STEP <= POSTSTSSHyperparameters.UPPER_STRINGWEIGHT_BOUND + POSTSTSSHyperparameters.ROUNDING_ERROR) {
			state.setStringSimilarityWeight(stringWeight + 2*POSTSTSSHyperparameters.PARAMETER_STEP);
			double corr = calculatePearsonFromData(trainData);
			if (corr > getCurrentPearson()) {
				POSSearchState s = state.clone();
				s.setCurrentPearson(corr);
				createStringSimilarityMove (s, getParameterNames().length-1, 2*POSTSTSSHyperparameters.PARAMETER_STEP, stringWeight);			
			}
		}
		if (stringWeight - POSTSTSSHyperparameters.PARAMETER_STEP >= POSTSTSSHyperparameters.LOWER_STRINGWEIGHT_BOUND - POSTSTSSHyperparameters.ROUNDING_ERROR) {
			state.setStringSimilarityWeight(stringWeight - POSTSTSSHyperparameters.PARAMETER_STEP);
			double corr = calculatePearsonFromData(trainData);
			if (corr > getCurrentPearson()) {
				POSSearchState s = state.clone();
				s.setCurrentPearson(corr);
				createStringSimilarityMove (s, getParameterNames().length-1, -POSTSTSSHyperparameters.PARAMETER_STEP, stringWeight);
			}
		}
		if (stringWeight - 2*POSTSTSSHyperparameters.PARAMETER_STEP >= POSTSTSSHyperparameters.LOWER_STRINGWEIGHT_BOUND - POSTSTSSHyperparameters.ROUNDING_ERROR) {
			state.setStringSimilarityWeight(stringWeight - 2*POSTSTSSHyperparameters.PARAMETER_STEP);
			double corr = calculatePearsonFromData(trainData);
			if (corr > getCurrentPearson()) {
				POSSearchState s = state.clone();
				s.setCurrentPearson(corr);
				createStringSimilarityMove (s, getParameterNames().length-1, -2*POSTSTSSHyperparameters.PARAMETER_STEP, stringWeight);
			}
		}
		state.setStringSimilarityWeight(stringWeight);
	}
	
	private void createPOSWeightMoves () throws InterruptedException {
	    int [] currentThreads = new int [1];
	    currentThreads[0] = 0;
	    int [] waitObject = new int [1];
	    HillClimbThread [] hcList = new HillClimbThread [state.getPosWeights().length];
		for (int i=0; i<state.getPosWeights().length; i++) {	
			synchronized (waitObject) {
				while (currentThreads[0] > Experiment.MAX_THREADS)
					waitObject.wait();
			}
			synchronized(currentThreads) {
				currentThreads[0]++;
			}
			HillClimbThread sThread = new HillClimbThread (this, state.clone(), i, currentThreads, waitObject);
			hcList[i] = sThread;
			sThread.start();		
		}
		synchronized (waitObject) {
			while (currentThreads[0] > 0)
	    		waitObject.wait();
	    }
	    
	    for (int i=0; i<hcList.length; i++)
	    	possibleMoves.addAll(hcList[i].getPossibleMoves());
	}
	
	private HillClimbMove selectBestMove () {
		if (possibleMoves.isEmpty()) return null;
		
		// Momentum
		if (!moveHistory.isEmpty()) {
			int index = -1;
			double best = 0;
			HillClimbMove previousMove = moveHistory.get(moveHistory.size()-1);
			for (int i=0; i<possibleMoves.size(); i++)
				if ((previousMove.getParameterIndex() == possibleMoves.get(i).getParameterIndex()) &&
						possibleMoves.get(i).getErrorReduction() > best) {
					best = possibleMoves.get(i).getErrorReduction();
					index = i;
				}
			if (index != -1)
				return possibleMoves.get(index);
		}
		///////////////////////////////////////////////////////////////////////////////////////////////
		
		double errorReduction = 0;
		ArrayList<HillClimbMove> list = new ArrayList<HillClimbMove> ();
		for (int i=0; i<possibleMoves.size(); i++)
			if (possibleMoves.get(i).getErrorReduction() > errorReduction) {
				errorReduction = possibleMoves.get(i).getErrorReduction();
				list.clear();
				list.add(possibleMoves.get(i));
			}
			else if (possibleMoves.get(i).getErrorReduction() == errorReduction)
				list.add(possibleMoves.get(i));
		
		System.out.println("Number of best possible moves: " + list.size()); 
		if (list.size() == 1)
			return list.get(0);
		else
			return list.get(rng.nextInt(list.size()));
	}
	
	private void applyHillClimbMove (HillClimbMove move) {
		state = move.getNewState();
		moveHistory.add(move);
	}
	
	private void printFinalResults () {
		super.printFinalResults(this.state);
	}
	
	private boolean isErrorReductionSufficient(HillClimbMove proposedMove) {
		if (moveHistory.isEmpty())
			return true;
		HillClimbMove firstMove = moveHistory.get(0);
		if (proposedMove.getErrorReduction() / firstMove.getErrorReduction() > HILLCLIMB_STOPPING_ERROR_REDUCTION_THRESHOLD)
			return true;
		else
			return false;
	}
	
	public double getCurrentErrorLevel () {
		return POSTSTSSHyperparameters.MAXIMAL_PERFORMANCE_VALUE - state.getCurrentPearson();
	}
	
	public String [] getParameterNames() { return posMapping.getParameterNames(POSMappingDimensionality.HIGH_DIMENSIONAL); }
	public HashMap<String, Integer> getPosNameToIndexMap() { return posMapping.getPosNameToIndexMap(POSMappingDimensionality.HIGH_DIMENSIONAL); }
	public HashMap<String, Integer> getPosPairingIndexMapping() { return posMapping.getPosPairingIndexMapping(POSMappingDimensionality.HIGH_DIMENSIONAL); }
	public HashMap<Integer, Integer> getPosPairingIndexMappingI() { return posMapping.getPosPairingIndexMappingI(POSMappingDimensionality.HIGH_DIMENSIONAL); }
	public HashMap<Integer, Integer> getPosPairingIndexMappingJ() { return posMapping.getPosPairingIndexMappingJ(POSMappingDimensionality.HIGH_DIMENSIONAL);	}
}
