package stsfinegrain.similarity.algorithms.optimizer.spacesearch;

import java.util.ArrayList;
import java.util.HashMap;

import stsfinegrain.Experiment;
import stsfinegrain.languagespecific.POSMapping;
import stsfinegrain.languagespecific.POSMapping.POSMappingDimensionality;
import stsfinegrain.similarity.algorithms.POSTSTSS;
import stsfinegrain.similarity.algorithms.optimizer.POSTSTSSHyperparameters;

/**
* @author Vuk Batanović
*/
public class PseudoExhaustiveSearch extends SearchMethod {

	private ArrayList<ArrayList<PseudoExhaustiveSearchPairHighPoint>> collectedCandidatePairResults;
	private ArrayList<ArrayList<Double>> candidatePosWeights;
	private ArrayList<ArrayList<Integer>> candidatePosPairingIndices;
	
    private ArrayList<POSSearchState> bestPOSCombinations;
    private ArrayList<POSSearchState> finalBest;
	
	public ArrayList<POSSearchState> getFinalBest () { return finalBest; }
    
	public PseudoExhaustiveSearch (POSMapping posMapping, POSTSTSSHyperparameters hyperparameters, POSTSTSS[] trainData, POSTSTSS[] testData) {
		super(posMapping, hyperparameters, trainData, testData);
		
		int dimensions = posMapping.getLowDimensionSize();
		double[] posWeights = new double [dimensions];
		int[][] posPairingIndices = new int [dimensions][dimensions];
		candidatePosWeights = new ArrayList<ArrayList<Double>> (dimensions); 
		
		for (int i=0; i<posWeights.length; i++) {
			posWeights[i] = hyperparameters.getInitialPosWeight();
			candidatePosWeights.add(new ArrayList<Double> ());
		}

		for (int i=0; i<posPairingIndices.length; i++)
			for (int j=0; j<posPairingIndices[i].length; j++)
				if (i != j)
					posPairingIndices[i][j] = hyperparameters.getInitialPosPairingValue();
				else
					posPairingIndices[i][j] = POSTSTSSHyperparameters.POS_INDEX_ALLOW_PAIRING;
		
		POSSearchState state = new POSSearchState(0, hyperparameters.getInitialStringSimilarityWeight(), posWeights, posPairingIndices);
		this.state = state;

		candidatePosPairingIndices = new ArrayList<ArrayList<Integer>> (dimensions*(dimensions-1)/2);
		collectedCandidatePairResults = new ArrayList<ArrayList<PseudoExhaustiveSearchPairHighPoint>> ();
		
	    bestPOSCombinations = new ArrayList<POSSearchState> ();
	    finalBest = new ArrayList<POSSearchState> ();
		
		for (int i=0; i<state.getPosPairingIndices().length-1; i++)
			for (int j=i+1; j<state.getPosPairingIndices().length; j++) 
				candidatePosPairingIndices.add(new ArrayList<Integer> ());
	}
	
	public PseudoExhaustiveSearch (SearchMethod es, POSSearchState state) {
		super(es, state);
	}
	
	public PseudoExhaustiveSearch (SearchMethod es, POSSearchState state, boolean expand) {
		super(es, state, expand);
	}
	
	public void optimizeFull () throws InterruptedException {
		optimizeUpToMinimization();
		finishOptimization();
	}
	
	public void optimizeUpToMinimization() throws InterruptedException {
		// Calculate starting Pearson correlation
		setCurrentPearson(calculatePearsonFromData (trainData));
		System.out.println("Starting train Pearson: " + getCurrentPearson());
		double testStart = calculatePearsonFromData (testData);
		System.out.println("Starting validation Pearson: " + testStart);

		explorePOSPairs();
		sortPOSPairInfo();
		int[] maxN_POStoPOSIndices = printPOSPairInfoAndCalculateNumberOfCombinations();
		exploreOptimalWeightCombinations(maxN_POStoPOSIndices);
		exploreStringSemanticSimilarityWeight();
	    sortAccordingToTestSetPerformance();
	}
	
	public void finishOptimization () {
	    if (hyperparameters.useValueMinimization())
	    	for (POSSearchState s: finalBest) {
	    		lowerUnnecessarilyHighPosScores(s);
	    		preventUnnecessaryWordPairings(s);
	    	}
	    sortAccordingToTestSetPerformance();
	    printFinalResults();
	}
	
	/**
	 * Explore all possible POS pairings in parallel
	 * @throws InterruptedException
	 */
	private void explorePOSPairs () throws InterruptedException {
		int [] currentThreads = new int [1];
		currentThreads[0] = 0;
		int [] waitObject = new int [1];
		PseudoExhaustiveSearchThread [] threadList = new PseudoExhaustiveSearchThread [candidatePosPairingIndices.size()];
	    int cnt = 0;
		for (int i=0; i<state.getPosWeights().length-1; i++)
			for (int j=i+1; j<state.getPosWeights().length; j++) {
				synchronized (waitObject) {
		    		while (currentThreads[0] > Experiment.MAX_THREADS)
		    			waitObject.wait();
				}
    			synchronized(currentThreads) {
    				currentThreads[0]++;
    			}
		    	PseudoExhaustiveSearchThread sThread = new PseudoExhaustiveSearchThread (this, state.clone(),  i, j, currentThreads, waitObject);
		    	threadList[cnt++] = sThread;
		    	sThread.start();
			}
		synchronized (waitObject) {
			while (currentThreads[0] > 0)
	    		waitObject.wait();
	    }
		for (int i=0; i<threadList.length; i++)
			collectedCandidatePairResults.add(threadList[i].getCandidatePairResults());
		System.out.println("Finished pair space search!");
	}
	
	/**
	 * Sort the candidate pair results into candidate POS weights and candidate POS interaction matrix indices
	 */
	private void sortPOSPairInfo () {
		for (int i=0; i<collectedCandidatePairResults.size(); i++) {
			ArrayList<PseudoExhaustiveSearchPairHighPoint> list = collectedCandidatePairResults.get(i);
			for (PseudoExhaustiveSearchPairHighPoint hp: list) {
				int posIndex1 = hp.getPos1Index();
				int posIndex2 = hp.getPos2Index();
				int pairingValue = hp.getPairingValue();
				if (!candidatePosWeights.get(posIndex1).contains(hp.getPos1Weight()))
					candidatePosWeights.get(posIndex1).add(hp.getPos1Weight());
				if (!candidatePosWeights.get(posIndex2).contains(hp.getPos2Weight()))
					candidatePosWeights.get(posIndex2).add(hp.getPos2Weight());
				int temp = getPosPairingIndexMapping().get(posIndex1 + " " + posIndex2);
				if (!candidatePosPairingIndices.get(temp).contains(pairingValue))
					candidatePosPairingIndices.get(temp).add(pairingValue);
			}
		}
	}
	
	/**
	 * Print the number of different POS weights and POS interaction matrix values that need to be considered
	 * @return An array of the number of values to consider for each POS interaction matrix position
	 */
	private int[] printPOSPairInfoAndCalculateNumberOfCombinations () {
	    int [] maxN_POSWeights = new int [state.getPosWeights().length];
	    int [] maxN_POStoPOSIndices = new int [candidatePosPairingIndices.size()];
	    for (int i=0; i<candidatePosWeights.size(); i++) {
	    	state.getPosWeights()[i] = candidatePosWeights.get(i).get(0);
	    	maxN_POSWeights[i] = candidatePosWeights.get(i).size();
	    }
	    
	    for (int i=0; i<state.getPosPairingIndices().length-1; i++)
	    	for (int j=i+1; j<state.getPosPairingIndices().length; j++) {
	    		int indexMapping = getPosPairingIndexMapping().get(i + " " + j);
	    		if (candidatePosPairingIndices.get(indexMapping).size() == 0)
	    			candidatePosPairingIndices.get(indexMapping).add(POSTSTSSHyperparameters.POS_INDEX_FORBID_PAIRING);
    			state.getPosPairingIndices()[i][j] = state.getPosPairingIndices()[j][i] = candidatePosPairingIndices.get(indexMapping).get(0);
    			maxN_POStoPOSIndices[indexMapping] = candidatePosPairingIndices.get(indexMapping).size();
	    	}
	    
	    setCurrentPearson(calculatePearsonFromData (trainData));
		System.out.println("Pseudoexhaustive search - initial parameter combination Pearson correlation on train data: " + getCurrentPearson());
		double testPearson = calculatePearsonFromData (testData);
		System.out.println("Pseudoexhaustive search - initial parameter combination Pearson correlation on validation/test data: " + testPearson);
	    
	    int totalCnt = 1;
	    for (int i=0; i<maxN_POSWeights.length; i++) {
	    	System.out.print(maxN_POSWeights[i] + "\t");
	    	totalCnt *= maxN_POSWeights[i];
	    }
	    System.out.println();
	    System.out.println("Number of POS weight combinations to explore: " + totalCnt);
	    
	    totalCnt = 1;
	    for (int i=0; i<candidatePosPairingIndices.size(); i++) {
	    	System.out.print(maxN_POStoPOSIndices[i] + "\t");
	    	totalCnt *= maxN_POStoPOSIndices[i];
	    }
	    System.out.println();
	    System.out.println("Number of POS interaction matrix index value combinations to explore: " + totalCnt);
	    return maxN_POStoPOSIndices;
	}
	
	/**
	 * Explore all candidate POS weights and candidate POS interaction matrix values in parallel
	 * @param maxN_POStoPOSIndices
	 * @throws InterruptedException
	 */
	private void exploreOptimalWeightCombinations(int[] maxN_POStoPOSIndices) throws InterruptedException {
	    int [] currentN_Indices = new int [candidatePosPairingIndices.size()];
	    ArrayList<PseudoExhaustiveSearchThread> searchThreads = new ArrayList<PseudoExhaustiveSearchThread> ();
		int [] currentThreads = new int [1];
		currentThreads[0] = 0;
		int [] waitObject = new int [1];
		int cnt=1;
		
	    while (true) {
			synchronized (waitObject) {
	    		while (currentThreads[0] > Experiment.MAX_THREADS)
	    			waitObject.wait();
			}
			synchronized(currentThreads) {
				currentThreads[0]++;
			}
			PseudoExhaustiveSearchThread sThread = new PseudoExhaustiveSearchThread (this, state.clone(), candidatePosWeights, cnt++, currentThreads, waitObject);
			searchThreads.add(sThread);
			sThread.start();
	        
	        // Iterate through all candidate POS interaction matrix value combinations
	    	boolean more = false;
		    int i = currentN_Indices.length-1;
	    	for (; i>=0; i--)
	    		if (currentN_Indices[i] != maxN_POStoPOSIndices[i] - 1) {
	    			more = true;
	    			break;
	    		}
	    	if (more) {
	    		currentN_Indices[i]++;
	    		for (int x=i+1; x<currentN_Indices.length; x++)
	    			currentN_Indices[x] = 0;
	    		for (int x=i; x<currentN_Indices.length; x++) {
	    			state.getPosPairingIndices()[getPosPairingIndexMappingI().get(x)][getPosPairingIndexMappingJ().get(x)] = candidatePosPairingIndices.get(x).get(currentN_Indices[x]);
	    			state.getPosPairingIndices()[getPosPairingIndexMappingJ().get(x)][getPosPairingIndexMappingI().get(x)] = candidatePosPairingIndices.get(x).get(currentN_Indices[x]);
	    		}
	    		continue;
	    	}
	    	break;
	    }
		synchronized (waitObject) {
			while (currentThreads[0] > 0)
	    		waitObject.wait();
	    }
	    for (PseudoExhaustiveSearchThread st: searchThreads) {
	    	if (st.getPearson() > getCurrentPearson()) {
	    		setCurrentPearson(st.getPearson());
	    		bestPOSCombinations.clear();
	    		bestPOSCombinations.addAll(st.getPosCombinations());
	    	}
	    	else if (st.getPearson() == getCurrentPearson())
	    		bestPOSCombinations.addAll(st.getPosCombinations());
	    }
	}
	
	/**
	 * Explore different string/semantic similarity weights for all best-performing POS weights/interaction matrix values
	 */
	private void exploreStringSemanticSimilarityWeight () {
	    double bestPearsonTrain = getCurrentPearson();
	    for (double stringSimilarityWeight = POSTSTSSHyperparameters.LOWER_STRINGWEIGHT_BOUND; stringSimilarityWeight <= POSTSTSSHyperparameters.UPPER_STRINGWEIGHT_BOUND + POSTSTSSHyperparameters.ROUNDING_ERROR; stringSimilarityWeight+=POSTSTSSHyperparameters.PARAMETER_STEP)
	    	for (int i=0; i<bestPOSCombinations.size(); i++) {
	    		POSSearchState s = bestPOSCombinations.get(i).clone();
	    		s.setStringSimilarityWeight(stringSimilarityWeight);
	    		double corr = calculatePearsonFromData (trainData, s);
	    		if (corr > bestPearsonTrain) {
	    			s.setCurrentPearson(corr);
	    			bestPearsonTrain = corr;
	    			finalBest.clear();
	    			finalBest.add(s);
	    		}
	    		else if (corr == bestPearsonTrain) {
	    			s.setCurrentPearson(corr);
	    			finalBest.add(s);
	    		}
	    	}
	    setCurrentPearson(bestPearsonTrain);	
	    System.out.println("\nNumber of best parameter combinations on the training set: " + finalBest.size());
	    System.out.println("Best Pearson correlation on the training set: " + getCurrentPearson() + "\n");
	}
	
	/**
	 * Out of all final parameter combinations that behave equally well on the training set/folds, we put the one best-performing on the validation set/fold in the first position.
	 * This is useful during hyperparameter optimization, since it provides the most optimistic performance assessment for a hyperparameter set, rather than a random one.
	 * In case there are a lot of states with identical performance figures on both the train and test data and identical POS settings i.e. states that only differ with regard to string similarity weight, we keep only one state that is closest to the neutral weight value of 0.5.
	 */
	private void sortAccordingToTestSetPerformance () {
	    ArrayList<Double> testResults = new ArrayList<Double> (finalBest.size());
	    for (int i=0; i< finalBest.size(); i++) {
	    	POSSearchState s = finalBest.get(i);
	    	double test = calculatePearsonFromData (testData, s);
	    	testResults.add(test);
	    }	
	    for (int i=0; i< finalBest.size()-1; i++) 
	    	for (int j=i+1; j<finalBest.size();) {
	    		if (finalBest.get(i).posEquals(finalBest.get(j)) && finalBest.get(i).getCurrentPearson() == finalBest.get(j).getCurrentPearson() &&
	    				testResults.get(i).doubleValue() == testResults.get(j).doubleValue()) {
	    			int toRemove = -1;
	    			if (finalBest.get(i).getStringSimilarityWeight() == 0.5)
	    				toRemove = j;
	    			else if (finalBest.get(j).getStringSimilarityWeight() == 0.5)
	    				toRemove = i;
	    			else if (Math.abs(finalBest.get(i).getStringSimilarityWeight() - 0.5) > Math.abs(finalBest.get(j).getStringSimilarityWeight() - 0.5))
	    				toRemove = i;
	    			else
	    				toRemove = j;
	    			finalBest.remove(toRemove);
	    			testResults.remove(toRemove);
	    			if (toRemove == i)
	    				j=i+1;
	    		}
	    		else
	    			j++;
	    }
	    double bestPearsonTest = 0;
	    int bestPearsonStateIndex = -1;
	    for (int i=0; i< finalBest.size(); i++) {
	    	double test = testResults.get(i);
	    	if (test > bestPearsonTest) {
	    		bestPearsonTest = test;
	    		bestPearsonStateIndex = i;
	    	}
	    }	
	    POSSearchState bestState = finalBest.remove(bestPearsonStateIndex);
	    finalBest.add(0, bestState);
	}
	
	/**
	 * Print out the final best parameter settings
	 */
	private void printFinalResults() {
	    System.out.println("Final count of best parameter settings: " + finalBest.size() + "\n");
	    for (int i=0; i< finalBest.size(); i++) {
	    	System.out.println("Parameter setting #" + (i+1));
       		POSSearchState s = finalBest.get(i);
    		super.printFinalResults(s);
	    }
	}
	
	private void preventUnnecessaryWordPairings (POSSearchState s) {
		POSSearchState backup = state;
		state = s;
		double corr = state.getCurrentPearson();
		for (int i=0; i<state.getPosPairingIndices().length-1; i++)
			for (int j=i+1; j<state.getPosPairingIndices().length; j++) {
				if (state.getPosPairingIndices()[i][j] == POSTSTSSHyperparameters.POS_INDEX_FORBID_PAIRING)
					continue;
				state.getPosPairingIndices()[i][j] = state.getPosPairingIndices()[j][i] = POSTSTSSHyperparameters.POS_INDEX_FORBID_PAIRING;
				double newCorr = calculatePearsonFromData(trainData);
				if (newCorr < corr)
					state.getPosPairingIndices()[i][j] = state.getPosPairingIndices()[j][i] = POSTSTSSHyperparameters.POS_INDEX_ALLOW_PAIRING;
				else if (newCorr > corr) {
					state.setCurrentPearson(newCorr);
					corr = newCorr;
				}
			}	
		state.setCurrentPearson(calculatePearsonFromData(trainData));
		state = backup;
	}
	
	private void lowerUnnecessarilyHighPosScores (POSSearchState s) {
		POSSearchState backup = state;
		state = s;
		double corr = state.getCurrentPearson();
		for (int i=state.getPosWeights().length-1; i>=0; i--)
			while (state.getPosWeights()[i] > POSTSTSSHyperparameters.LOWER_POS_BOUND + POSTSTSSHyperparameters.ROUNDING_ERROR) {
				state.getPosWeights()[i] -= POSTSTSSHyperparameters.PARAMETER_STEP;
				double newCorr = calculatePearsonFromData(trainData);
				if (newCorr < corr) {
					state.getPosWeights()[i] += POSTSTSSHyperparameters.PARAMETER_STEP;
					break;
				}
				else if (newCorr > corr) {
					state.setCurrentPearson(newCorr);
					corr = newCorr;
				}
			}
		state.setCurrentPearson(calculatePearsonFromData(trainData));
		state = backup;
	}
	
	public String [] getParameterNames() { return posMapping.getParameterNames(POSMappingDimensionality.LOW_DIMENSIONAL); }
	public HashMap<String, Integer> getPosNameToIndexMap() { return posMapping.getPosNameToIndexMap(POSMappingDimensionality.LOW_DIMENSIONAL); }
	public HashMap<String, Integer> getPosPairingIndexMapping() { return posMapping.getPosPairingIndexMapping(POSMappingDimensionality.LOW_DIMENSIONAL); }
	public HashMap<Integer, Integer> getPosPairingIndexMappingI() { return posMapping.getPosPairingIndexMappingI(POSMappingDimensionality.LOW_DIMENSIONAL); }
	public HashMap<Integer, Integer> getPosPairingIndexMappingJ() { return posMapping.getPosPairingIndexMappingJ(POSMappingDimensionality.LOW_DIMENSIONAL);	}
}
