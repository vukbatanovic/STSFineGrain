package stsfinegrain.similarity.algorithms.optimizer.spacesearch;

/**
* @author Vuk Batanović
*/
public class SearchState {
	protected double stringSimilarityWeight;

	public double getStringSimilarityWeight() {	return stringSimilarityWeight; }
	public void setStringSimilarityWeight(double stringSimilarityWeight) { this.stringSimilarityWeight = stringSimilarityWeight; }
	
	public SearchState (double stringSimilarityWeight) {
		this.stringSimilarityWeight = stringSimilarityWeight;
	}
	
	public SearchState () {}
	
	public String toString() {
		StringBuilder sb = new StringBuilder ();
		sb.append(stringSimilarityWeight + "\n");
		return sb.toString();
	}
}
