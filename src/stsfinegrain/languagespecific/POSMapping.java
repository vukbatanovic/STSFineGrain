package stsfinegrain.languagespecific;

import java.util.HashMap;

/**
* @author Vuk Batanović
*/
public abstract class POSMapping {
	
	public enum POSMappingDimensionality {LOW_DIMENSIONAL, HIGH_DIMENSIONAL}
	
	protected int lowDimensionSize;
	protected int highDimensionSize;
	protected String [] posNames;
	protected String [] parameterNamesLowDim;
	protected String [] parameterNamesHighDim;
	protected HashMap<String, Integer> posNameToIndexMapLowDim = new HashMap<String, Integer> ();		
	protected HashMap<String, Integer> posNameToIndexMapHighDim = new HashMap<String, Integer> ();	
	protected HashMap<String, Integer> posPairingIndexMappingLowDim = new HashMap<String, Integer> ();
	protected HashMap<String, Integer> posPairingIndexMappingHighDim = new HashMap<String, Integer> ();
	protected HashMap<Integer, Integer> posPairingIndexMappingLowDimI = new HashMap<Integer, Integer> ();
	protected HashMap<Integer, Integer> posPairingIndexMappingLowDimJ = new HashMap<Integer, Integer> ();
	protected HashMap<Integer, Integer>	posPairingIndexMappingHighDimI = new HashMap<Integer, Integer> ();
	protected HashMap<Integer, Integer> posPairingIndexMappingHighDimJ = new HashMap<Integer, Integer> ();
	
	public POSMapping () {
		initializeDimensionSizes();
		initializePosNames();
		initializeParameterNames();
		initializeHashMaps();
	}
	
	protected abstract void initializeDimensionSizes();
	protected abstract void initializePosNames();
	protected abstract void initializeParameterNames();
	protected abstract void initializeHashMaps();
	
	public abstract String transformMSDtoPOStag (String msd);
	
	public int getLowDimensionSize() { return lowDimensionSize; }
	public int getHighDimensionSize() { return highDimensionSize; }
	
	public String [] getPosNames () { return posNames;}
	
	public String [] getParameterNames(POSMappingDimensionality dim) {
		if (dim == POSMappingDimensionality.LOW_DIMENSIONAL) return parameterNamesLowDim;
		else return parameterNamesHighDim;
	}
	
	public HashMap<String, Integer> getPosNameToIndexMap(POSMappingDimensionality dim) {
		if (dim == POSMappingDimensionality.LOW_DIMENSIONAL) return posNameToIndexMapLowDim;
		else return posNameToIndexMapHighDim;
	}
	
	public HashMap<String, Integer> getPosPairingIndexMapping(POSMappingDimensionality dim) {
		if (dim == POSMappingDimensionality.LOW_DIMENSIONAL) return posPairingIndexMappingLowDim;
		else return posPairingIndexMappingHighDim;
	}
	
	public HashMap<Integer, Integer> getPosPairingIndexMappingI(POSMappingDimensionality dim) {
		if (dim == POSMappingDimensionality.LOW_DIMENSIONAL) return posPairingIndexMappingLowDimI;
		else return posPairingIndexMappingHighDimI;
	}
	
	public HashMap<Integer, Integer> getPosPairingIndexMappingJ(POSMappingDimensionality dim) {
		if (dim == POSMappingDimensionality.LOW_DIMENSIONAL) return posPairingIndexMappingLowDimJ;
		else return posPairingIndexMappingHighDimJ;
	}
}
