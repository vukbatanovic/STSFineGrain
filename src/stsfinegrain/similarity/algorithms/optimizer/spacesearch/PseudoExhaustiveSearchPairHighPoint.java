package stsfinegrain.similarity.algorithms.optimizer.spacesearch;

/**
* @author Vuk Batanović
*/
public class PseudoExhaustiveSearchPairHighPoint {

	private double pearson;
	private int pos1Index, pos2Index;
	private double pos1Weight, pos2Weight;
	private int pairingValue;
	
	public int getPos1Index() { return pos1Index; }	
	public int getPos2Index() { return pos2Index; }
	public double getPos1Weight() { return pos1Weight; }
	public double getPos2Weight() { return pos2Weight; }
	public int getPairingValue() { return pairingValue; }
	
	public PseudoExhaustiveSearchPairHighPoint(double pearson, int pos1Index, int pos2Index, double pos1Weight, double pos2Weight, int pairingValue) {
		this.pearson = pearson;
		this.pos1Index = pos1Index;
		this.pos2Index = pos2Index;
		this.pos1Weight = pos1Weight;
		this.pos2Weight = pos2Weight;
		this.pairingValue = pairingValue;
	}

	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("Train pearson: " + pearson).append("\n");
		sb.append("POS indices: " + pos1Index + "\t" + pos2Index).append("\n");
		sb.append("POS weights: " + pos1Weight + "\t" + pos2Weight).append("\n");
		sb.append("Pairing value: " + pairingValue);
		return sb.toString();
	}
}
