package stsfinegrain.similarity.algorithms.optimizer;

import java.util.ArrayList;

import stsfinegrain.similarity.algorithms.optimizer.spacesearch.SearchState;

/**
* @author Vuk Batanović
*/
public class OptimizationResults {
    private double bestPearsonTrain = 0;
    private double bestPearsonTest = 0;
    private double finalPearson = 0;
    private ArrayList<SearchState> bestParametersTrain = new ArrayList<SearchState>();
    private ArrayList<SearchState> bestParametersTest = new ArrayList<SearchState>();
    private ArrayList<SearchState> selectedParameters = new ArrayList<SearchState>();

	public double getBestPearsonTrain() { return bestPearsonTrain;}
	public void setBestPearsonTrain(double bestPearsonTrain) { this.bestPearsonTrain = bestPearsonTrain; }
	public double getBestPearsonTest() { return bestPearsonTest; }
	public void setBestPearsonTest(double bestPearsonTest) { this.bestPearsonTest = bestPearsonTest; }
	public double getFinalPearson() { return finalPearson; }
	public void setFinalPearson(double finalPearson) { this.finalPearson = finalPearson; }
	public ArrayList<SearchState> getBestParametersTrain() { return bestParametersTrain; }
	public ArrayList<SearchState> getBestParametersTest() { return bestParametersTest; }
	public ArrayList<SearchState> getSelectedParameters() { return selectedParameters; }
	public void setBestParametersTrain(ArrayList<SearchState> bestParametersTrain) { this.bestParametersTrain = bestParametersTrain; }
}
