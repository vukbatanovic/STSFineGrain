package stsfinegrain.similarity;

import java.util.ArrayList;

import stsfinegrain.similarity.algorithms.optimizer.spacesearch.SearchMethod;

/**
* @author Vuk Batanović
*/
public class StringAndPOSSimilarity extends StringSimilarity {
    
    private String[] tags1;
    private String[] tags2;
    private ArrayList<String> Ptag=new ArrayList<String>();
    private ArrayList<String> Rtag=new ArrayList<String>();
    private String[] sameWordTags1, sameWordTags2;
    
    public String [] getSameWordTags1 () { return sameWordTags1; }
    public String [] getSameWordTags2 () { return sameWordTags2; }
    
    public StringAndPOSSimilarity(String[] sentence1, String[] sentence2, String[] tags1, String[] tags2) {
    	super(sentence1, sentence2);
    	this.tags1 = tags1;
    	this.tags2 = tags2;
    }

    @Override
    public void initialize() {
        String strShorterSent[] = null;
        String strLongerSent[] = null;
        String strShorterSentTags[] = null;
        String strLongerSentTags[] = null;

        if (sentence1.length >= sentence2.length) {
            strLongerSent = sentence1;
            strShorterSent = sentence2;
            strLongerSentTags = tags1;
            strShorterSentTags = tags2;
        }
        else {
            strLongerSent = sentence2;
            strShorterSent = sentence1;
            strLongerSentTags = tags2;
            strShorterSentTags = tags1;
        }

        sameWords = new String[strShorterSent.length];    // maximal
        sameWordTags1 = new String[strShorterSent.length];
        sameWordTags2 = new String[strShorterSent.length];
        
        int k = 0;
        boolean isFound = false;

        // Find the same strings, fill the sameWords array with them and fill this.R with different strings
        for (int i=0; i<strShorterSent.length; i++) {
            for (int j=0; j<strLongerSent.length; j++) {
                if (strLongerSent[j].equals(strShorterSent[i])) {
                   isFound = true;
                   sameWords[k] = strShorterSent[i];
                   sameWordTags1[k] = strShorterSentTags[i];
                   sameWordTags2[k++] = strLongerSentTags[j];
                   break;
                }
            }
            if (!isFound) {
            	R.add(strShorterSent[i]);
            	this.Rtag.add(strShorterSentTags[i]);
            }
            isFound = false;
        }
        sigmaM = k;
        k = 0;

        // The same, just for this.P
        isFound = false;
        for (int i=0; i<strLongerSent.length; i++){
            for (int j=0; j<strShorterSent.length; j++){
                if (strShorterSent[j].equals(strLongerSent[i])) {
                    isFound = true;
                    k++;
                    break;
                }
            }
            if (!isFound) {
            	P.add(strLongerSent[i]);
            	this.Ptag.add(strLongerSentTags[i]);
            }
            isFound=false;
        }
        sigmaN = k;

        this.m = strShorterSent.length;
        this.n = strLongerSent.length;

        // Compact the sameWords array
        String [] temp = new String[sigmaM], tempT1 = new String[sigmaM], tempT2 = new String[sigmaM];;
        for (int i=0; i<sigmaM; i++) {
        	temp[i] = sameWords[i];
        	tempT1[i] = sameWordTags1[i];
        	tempT2[i] = sameWordTags2[i];
        }
        sameWords = temp;
        sameWordTags1 = tempT1;
        sameWordTags2 = tempT2;
    }
    
    public double[][] getPOSSimilarityMatrix(SearchMethod sm) {
        double [][] simMatrix=new double[m-sigmaM][n-sigmaN];
        for(int i=0; i<m-sigmaM; i++)
            for(int j=0; j<n-sigmaN; j++)
            	simMatrix[i][j] = sm.getPOSSimilarity (Ptag.get(j), Rtag.get(i));
        return simMatrix;
    }
}
