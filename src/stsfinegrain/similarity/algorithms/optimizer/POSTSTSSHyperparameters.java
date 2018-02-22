package stsfinegrain.similarity.algorithms.optimizer;

/**
* @author Vuk Batanović
*/
public class POSTSTSSHyperparameters {

	public enum POSWeightingFunction {CHOOSE_BIGGER, CHOOSE_SMALLER, CHOOSE_ARITHMETIC_MEAN, CHOOSE_GEOMETRIC_MEAN, CHOOSE_HARMONIC_MEAN}
	public static final double LOWER_POS_BOUND = 0.7;
	public static final double UPPER_POS_BOUND = 1.3;
	public static final int POS_INDEX_FORBID_PAIRING = 0;
	public static final int POS_INDEX_ALLOW_PAIRING = 1;
	public static final double LOWER_STRINGWEIGHT_BOUND = 0.3;
	public static final double UPPER_STRINGWEIGHT_BOUND = 0.7;
	public static final double PARAMETER_STEP = 0.1;
	public static final double ROUNDING_ERROR = 0.01;
	public static final double MAXIMAL_PERFORMANCE_VALUE = 0.92; // max Pearson correlation value, dataset-specific
	public static final int POS_INDEX_PARAMETER_SHIFT = 100; // used to index changes in POS interaction matrix values
	
	public static final double [] INITIAL_STRING_SIMILARITY_WEIGHT_OPTIONS = {0.5};
	public static final double [] INITIAL_POS_WEIGHT_OPTIONS = {LOWER_POS_BOUND, 1.0, UPPER_POS_BOUND};
    public static final int [] INITIAL_POS_INDEX_OPTIONS = {POS_INDEX_ALLOW_PAIRING, POS_INDEX_FORBID_PAIRING};
    public static final boolean [] VALUE_MINIMIZATION_OPTIONS = {false}; 
    public static final POSWeightingFunction [] POS_WEIGHTING_FUNCTION_OPTIONS = {POSWeightingFunction.CHOOSE_ARITHMETIC_MEAN};
    
	private POSWeightingFunction posWeightingFunction;
	private double initialPosWeight;
	private int initialPosPairingValue;
	private double initialStringSimilarityWeight;
	private boolean useValueMinimization;
	
	public POSWeightingFunction getPosWeightingFunction() {	return posWeightingFunction; }
	public double getInitialPosWeight() { return initialPosWeight; }
	public int getInitialPosPairingValue() { return initialPosPairingValue; }
	public double getInitialStringSimilarityWeight() { return initialStringSimilarityWeight; }
	public boolean useValueMinimization() { return useValueMinimization; }
	
	public POSTSTSSHyperparameters (double initialPosWeight, int initialPosPairingValue, double initialStringSimilarityWeight, 
			POSWeightingFunction posWeightingFunction, boolean useValueMinimization) {
		this.initialPosWeight = initialPosWeight;
		this.initialPosPairingValue = initialPosPairingValue;
		this.initialStringSimilarityWeight = initialStringSimilarityWeight;
		this.posWeightingFunction = posWeightingFunction;
		this.useValueMinimization = useValueMinimization;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("POS weighting function:\t" + posWeightingFunction.toString() + "\n");
		sb.append("Initial POS weight:\t" + initialPosWeight + "\n");
		sb.append("Initial POS pairing value:\t" + initialPosPairingValue + "\n");
		sb.append("Initial string similarity weight:\t" + initialStringSimilarityWeight + "\n");
		sb.append("Use value minimization:\t" + useValueMinimization + "\n");
		return sb.toString();
	}
}
