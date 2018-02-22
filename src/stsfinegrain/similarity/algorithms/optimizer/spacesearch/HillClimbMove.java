package stsfinegrain.similarity.algorithms.optimizer.spacesearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
* @author Vuk Batanović
*/
public class HillClimbMove {

	private POSSearchState newState;
	private double errorReduction;
	private int parameterIndex;
	private double parameterChange;
	private double oldParameterValue;
	private boolean posIndexChange;
	private double testPearson;
	
	public POSSearchState getNewState() { return newState; }
	public double getPearson() { return newState.getCurrentPearson(); }
	public double getErrorReduction() {	return errorReduction; }
	public int getParameterIndex() { return parameterIndex; }
	public double getParameterChange() { return parameterChange; }
	public double getOldParameterValue() { return oldParameterValue; }
	public boolean isPosIndexChange() { return posIndexChange; }
	public double getTestPearson() { return testPearson; }
	public void setTestPearson(double testPearson) { this.testPearson = testPearson; }
	
	public HillClimbMove(POSSearchState state, double errorReduction, int parameterIndex, double parameterChange, double oldParameterValue, boolean posIndexChange) {
		this.newState = state;
		this.errorReduction = errorReduction;
		this.parameterIndex = parameterIndex;
		this.parameterChange = parameterChange;
		this.oldParameterValue = oldParameterValue;
		this.posIndexChange = posIndexChange;
	}
	
	public HillClimbMove (String fileName, int dimensions) throws IOException {
		BufferedReader br = new BufferedReader (new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		double pearson = Double.parseDouble(br.readLine());
		errorReduction = Double.parseDouble(br.readLine());
		testPearson = Double.parseDouble(br.readLine());
		br.readLine();
		double stringSimilarityWeight = Double.parseDouble(br.readLine());
		br.readLine();
		parameterIndex = Integer.parseInt(br.readLine());
		parameterChange = Double.parseDouble(br.readLine());
		oldParameterValue = Double.parseDouble(br.readLine());
		int x = Integer.parseInt(br.readLine());
		if (x == 1)
			posIndexChange = true;
		br.readLine();
		double [] posWeights = new double [dimensions];
		String [] values = br.readLine().split("\t");
		for (int i=0; i<dimensions; i++)
			posWeights[i] = Double.parseDouble(values[i]);
		br.readLine();
		int[][] posPairingIndices = new int [dimensions][dimensions];
		for (int i=0; i<dimensions; i++) {
			values = br.readLine().split("\t");
			for (int j=0; j<dimensions; j++)
				posPairingIndices[i][j] = Integer.parseInt(values[j]);
		}
		br.close();
		POSSearchState state = new POSSearchState(pearson, stringSimilarityWeight, posWeights, posPairingIndices);
		this.newState = state;
	}
	
	public void saveMove (String fileName) throws IOException {
		File output = new File (fileName);
		if (output.exists()) {
			output.delete();
			output.createNewFile();
		}
		PrintWriter pw = new PrintWriter(output, "UTF-8");
		pw.println(newState.getCurrentPearson());
		pw.println(errorReduction);
		pw.println(testPearson);
		pw.println();
		pw.println(newState.getStringSimilarityWeight());
		pw.println();
		pw.println(parameterIndex);
		pw.println(parameterChange);
		pw.println(oldParameterValue);
		if (posIndexChange)
			pw.println("1");
		else
			pw.println("0");
		pw.println();
		for (int i=0; i<newState.getPosWeights().length-1; i++)
			pw.print(newState.getPosWeights()[i] + "\t");
		pw.println(newState.getPosWeights()[newState.getPosWeights().length-1]);
		pw.println();
		for (int i=0; i<newState.getPosPairingIndices().length; i++) {
			for (int j=0; j<newState.getPosPairingIndices()[i].length-1; j++)
				pw.print(newState.getPosPairingIndices()[i][j] + "\t");
			pw.println(newState.getPosPairingIndices()[i][newState.getPosPairingIndices()[i].length-1]);
		}
		pw.flush();
		pw.close();
	}
}
