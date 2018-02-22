package stsfinegrain.similarity;

import java.util.ArrayList;

/**
 * @author Davor Jovanović
 * @author Vuk Batanović
 */
public class StringSimilarity {

	protected String[] sentence1;
	protected String[] sentence2;
    protected int sigmaM = 0;    // shorter
    protected int sigmaN = 0;    // longer
    // dimension of matrix
    protected int m = 0;         // shorter
    protected int n = 0;         // longer
    // List of P and R
    protected ArrayList<String> P = new ArrayList<String>();
	protected ArrayList<String> R = new ArrayList<String>();
    protected String[] sameWords;

	public int getM() { return m; }
    public int getN() { return n; }
    public int getSigmaM() { return sigmaM; }
    public int getSigmaN() { return sigmaN; }
    public ArrayList<String> getP() { return P; }
	public ArrayList<String> getR() { return R; }
    public String[] getSameWords() { return sameWords; }
	
	public StringSimilarity(String[] sentence1, String[] sentence2) {
		this.sentence1 = sentence1;
		this.sentence2 = sentence2;
	}
	
	/**
	 * Longest common subsequence algorithm.
	 * Implementation style taken from wikipedia.
	 */
    private String lcs(String a, String b) {
        int[][] lengths = new int[a.length()+1][b.length()+1];
        // row 0 and column 0 are initialized to 0 already
        for (int i = 0; i < a.length(); i++)
            for (int j = 0; j < b.length(); j++)
                if (a.charAt(i) == b.charAt(j))
                    lengths[i+1][j+1] = lengths[i][j] + 1;
                else
                    lengths[i+1][j+1] = Math.max(lengths[i+1][j], lengths[i][j+1]);
                    // read the substring out from the matrix
        StringBuffer sb = new StringBuffer();
        for (int x = a.length(), y = b.length();
        x != 0 && y != 0; )
        {
            if (lengths[x][y] == lengths[x-1][y])
                x--;
            else if (lengths[x][y] == lengths[x][y-1])
                y--;
            else {
                assert a.charAt(x-1) == b.charAt(y-1);
                sb.append(a.charAt(x-1));
                x--;
                y--;
            }
        }     return sb.reverse().toString();
    }
    
    /**
     * Maximal Consecutive Longest Common Subsequence starting from the first character (MCLCS1).
     * Takes two strings as input and returns the shorter string or maximal consecutive portions of the shorter string that consecutively match with the longer string.
     * Matching must be from the first character for both strings.
     */
    private String mclcs1 (String str1, String str2) {
        String shorterString = null;
        String longerString = null;
        if(str1.length() >= str2.length()) {
            shorterString = str2;
            longerString = str1;
        }
        else {
            shorterString = str1;
            longerString = str2;
        }
        int endIndex=0;
        for(int i=0; i<shorterString.length(); i++) {
            if (shorterString.charAt(i) == longerString.charAt(i)) endIndex++;
            else break;
        }
        return longerString.substring(0,endIndex);
    }

    /**
     * Maximal consecutive longest common subsequence starting at n (MCLCSn).
     * The same as MCLCS1, but matching can start at any character.
     */
    private String mclcsN(String str1, String str2) {
        String shorterString = null;
        String longerString = null;
        if(str1.length() >= str2.length()) {
            shorterString = str2;
            longerString = str1;
        }
        else {
            shorterString = str1;
            longerString = str2;
        }

        String retString = "";
        int beginIndex = 0;
        int max = 0;
        int sum = 0;
        boolean bSubSeq = false;

        for (int i=0; i<shorterString.length(); i++) {
            int k = i;
            sum = 0;
            beginIndex = 0;
            for (int j=0; j<longerString.length(); j++) {
                if (longerString.charAt(j) == shorterString.charAt(k)) {
                    if (!bSubSeq)
                    	beginIndex = j;
                    k++;
                    sum++;
                    bSubSeq = true;
                }
                else if (bSubSeq) {
                    bSubSeq = false;
                    if (sum > max) {
                        max = sum;
                        try {
                        	retString = longerString.substring(beginIndex, j);
                        }
                        catch(Exception ex) {
                          System.out.println(beginIndex+"   "+j);
                        }
                    }
                    k = i;
                    sum = 0;
                    beginIndex = 0;
                }

                // This has to be checked as well, because if the string match until the end of the shorter string, the else branch is not entered
                if (((j+1) == longerString.length() || k == shorterString.length()) && bSubSeq) {
                    bSubSeq = false;
                    if (sum > max) {
                        max = sum;
                        try {
                        	retString = longerString.substring(beginIndex, j+1);
                        }
                        catch(Exception ex) {
                          System.out.println(beginIndex+"   "+j);
                        }
                    }
                    k = i;
                    sum = 0;
                    beginIndex = 0;
                }
            }
        }
        return retString;
    }
    
    /**
     * Get the string similarity, calculated as the length-normalized average of LCS, MCLCS1 and MCLCSn
     */
    public double getStringSimilarity(String word1, String word2){
        String strLCS = lcs(word1,word2);
        String strMCLCS1 = mclcs1(word1,word2);
        String strMCLCSN = mclcsN(word1,word2);

        // Normalize it
        double v1 = ( strLCS.length()*strLCS.length() );
        v1 /= ( word1.length()*word2.length() );
        double v2 = (strMCLCS1.length()*strMCLCS1.length() );
        v2 /= ( word1.length()*word2.length() );
        double v3 = (strMCLCSN.length()*strMCLCSN.length() );
        v3 /= ( word1.length()*word2.length() );

        double alfa = (v1+v2+v3)/3;
        return alfa;
    }

    public void initialize() {
        String strShorterSent[] = null;
        String strLongerSent[] = null;

        if (sentence1.length >= sentence2.length) {
            strLongerSent = sentence1;
            strShorterSent = sentence2;
        }
        else {
            strLongerSent = sentence2;
            strShorterSent = sentence1;
        }

        sameWords = new String[strShorterSent.length];    // maximal
        int k = 0;
        boolean isFound = false;

        // Find the same strings, fill the sameWords array with them and fill this.R with different strings
        for (int i=0; i<strShorterSent.length; i++) {
            for (int j=0; j<strLongerSent.length; j++) {
                if (strLongerSent[j].equals(strShorterSent[i])) {
                   isFound = true;
                   sameWords[k++] = strShorterSent[i];
                   break;
                }
            }
            if (!isFound)
            	R.add(strShorterSent[i]);
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
            if (!isFound)
            	P.add(strLongerSent[i]);
            isFound=false;
        }
        sigmaN = k;

        this.m = strShorterSent.length;
        this.n = strLongerSent.length;

        // Compact the sameWords array
        String [] temp = new String[sigmaM];
        for (int i=0; i<sigmaM; i++)
        	temp[i] = sameWords[i];
        sameWords = temp;
    }

    public double[][] getStringSimilarityMatrix(){
        double [][] simMatrix=new double[m-sigmaM][n-sigmaN];
        for(int i=0; i<m-sigmaM; i++)
            for(int j=0; j<n-sigmaN; j++)
                simMatrix[i][j] = getStringSimilarity(P.get(j), R.get(i));
        return simMatrix;
    }
}