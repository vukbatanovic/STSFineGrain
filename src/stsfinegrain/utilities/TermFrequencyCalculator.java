package stsfinegrain.utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

import stsfinegrain.utilities.TextCleaner;

/**
 * 	@author Vuk Batanović
 */
public class TermFrequencyCalculator {

    private HashMap<String, Integer> lexiconTF = null;
    private HashMap<String, Double> lexiconTF_Norm = null;
    private HashMap<String, Double> lexiconTF_Norm0_1 = null;
    private long totalNumberOfWords = 0;

    public HashMap<String, Double> getLexiconTF_Norm0_1 () { return lexiconTF_Norm0_1; }
    
    private void resetHashMaps() {
    	totalNumberOfWords = 0;
        lexiconTF = new HashMap<String, Integer>();
        lexiconTF_Norm = new HashMap<String, Double>();
        lexiconTF_Norm0_1 = new HashMap<String, Double>();
    }

    public void buildLexiconAndCalculateFreq(String[] corpusPaths, String outputTermFrequenciesPath) throws IOException {
    	resetHashMaps();
    	for (String path: corpusPaths) {
	        BufferedReader br = new BufferedReader (new InputStreamReader(new FileInputStream(path), "UTF-8"));
	        String line = null;
	        while ((line = br.readLine()) != null) {
	        	line = TextCleaner.filter(line);
	            String words[] = line.split("\\s+");
	            for (int i = 0; i < words.length; i++) {
	                if (!words[i].equals("")) {
	                    totalNumberOfWords++;
	                    if (lexiconTF.containsKey(words[i]))
	                    	lexiconTF.put(words[i], lexiconTF.get(words[i]) + 1);
	                    else
	                    	lexiconTF.put(words[i], 1);
	                }
	            }
	        }
	        br.close();
    	}
        calculate_TF_and_TFnorm(outputTermFrequenciesPath);
        System.out.println("\nTerm frequencies file creation complete!\n");
        System.out.println("Total number of terms : " + lexiconTF.size());
        System.out.println("Total number of words : " + totalNumberOfWords);
    }

    private void calculate_TF_and_TFnorm(String outputTermFrequenciesPath) throws FileNotFoundException, IOException {
        PrintWriter pw = new PrintWriter(outputTermFrequenciesPath, "UTF-8");
        double TFmax = -(java.lang.Math.log10(1.0/totalNumberOfWords));
        for (String term: lexiconTF.keySet()) {
            int TF =  lexiconTF.get(term);
            double TFnorm = -(java.lang.Math.log10((TF*1.0) / totalNumberOfWords));
            double TFnorm0_1 = TFnorm / TFmax;
        	lexiconTF_Norm.put(term, TFnorm);
        	lexiconTF_Norm0_1.put(term, TFnorm0_1);
            pw.write(term + "\t" + TF + "\t" + TFnorm + "\t" + TFnorm0_1 + "\r\n");
        }
        pw.flush();
        pw.close();
    }
    
    public void loadLexiconsFromFile (String termFrequenciesPath) throws NumberFormatException, IOException {
    	resetHashMaps();
        BufferedReader br = new BufferedReader (new InputStreamReader(new FileInputStream(termFrequenciesPath), "UTF-8"));
    	String line = null;
    	while ((line = br.readLine()) != null) {
    		String [] niz = line.split("\\s");
    		lexiconTF.put(niz[0], Integer.parseInt(niz[1]));
    		lexiconTF_Norm.put(niz[0], Double.parseDouble(niz[2]));
    		lexiconTF_Norm0_1.put(niz[0], Double.parseDouble(niz[3]));
    	}
    	br.close();
    }
}