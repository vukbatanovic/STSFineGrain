package stsfinegrain.similarity.algorithms;

import java.util.HashMap;

import stsfinegrain.utilities.Word2VecModel;

/**
 * @author Vuk Batanović
 */
public class LInSTSS extends IslamAndInkpen {

	private HashMap<String, Double> termFrequencies;
	private double[][] termFrequencyMatrix;
	
	public LInSTSS(String[] sent1, String[] sent2, double initialScore, Word2VecModel model, HashMap<String, Double> termFrequencies) {
		super(sent1, sent2, initialScore, model);
		this.termFrequencies = termFrequencies;
	}
	
	@Override
	public void initialize () {
		super.initialize();
        termFrequencyMatrix = new double[sSim.getM()-sSim.getSigmaM()][sSim.getN()-sSim.getSigmaN()];

        for(int i=0; i<sSim.getM()-sSim.getSigmaM(); i++) {
           	double tfnorm2 = 1, tfnorm1 = 1;
           	if (termFrequencies.containsKey(sSim.getR().get(i)))
           		tfnorm2 = termFrequencies.get(sSim.getR().get(i));
        	for(int j=0; j<sSim.getN()-sSim.getSigmaN(); j++) {
                if (termFrequencies.containsKey(sSim.getP().get(j)))
                	tfnorm1 = termFrequencies.get(sSim.getP().get(j));
                else
                	tfnorm1 = 1;
               	termFrequencyMatrix[i][j] = tfnorm1*tfnorm2;
        		termFrequencyMatrix[i][j] = Math.pow(2, termFrequencyMatrix[i][j]-1);
            }
        }
	}
	
	@Override
	protected double[][] constructJointMatrix () {
		double [][] jointMatrix = super.constructJointMatrix();
        for(int i=0; i<sSim.getM()-sSim.getSigmaM(); i++)
        	for(int j=0; j<sSim.getN()-sSim.getSigmaN(); j++)
                jointMatrix[i][j] *= termFrequencyMatrix[i][j];
        return jointMatrix;
	}
	
	@Override
	protected double getDeltaValue () {
		double delta = sSim.getSigmaM();
        double deltaValue = 0; 
        for (int i=0; i<delta; i++) {
        	String word = sSim.getSameWords()[i];
           	double tfnorm = 1;
           	if (termFrequencies.containsKey(word)) {
           		tfnorm = termFrequencies.get(word);
           		tfnorm = tfnorm * tfnorm;
           		tfnorm = Math.pow(2, tfnorm-1);
           	}
           	deltaValue += tfnorm;
        }
        return deltaValue;
	}
} 