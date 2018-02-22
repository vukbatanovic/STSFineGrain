package stsfinegrain.utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
* @author Vuk Batanović
*/
public class Word2VecModel {
	private String w2vPath;
	private int vocabLength;
	private int dimensions;
	private HashMap<String, double []> model = new HashMap<String, double []> ();
	
	public int getDimensions() { return dimensions; }
	public double [] getWordVector (String word) { return model.get(word); }
	public boolean containsWord (String word) { return model.containsKey(word); }
	
	public Word2VecModel (String path) throws IOException {
		w2vPath = path;
		BufferedReader br = new BufferedReader (new InputStreamReader(new FileInputStream(w2vPath), "UTF-8"));
		String[] header = br.readLine().split("\\s+");
		vocabLength = Integer.parseInt(header[0]);
		dimensions = Integer.parseInt(header[1]);
		String line = null;
		int count = 0;
		while ((line = br.readLine()) != null) {
			String[] tokens = line.split("\\s+");
			String word = tokens[0];
			double [] vector = new double[dimensions];
			for (int i=1; i<tokens.length; i++)
				vector[i-1] = Double.parseDouble(tokens[i]);
			model.put(word, vector);
			count++;
		}
		br.close();
		if (count != vocabLength)
			System.out.println("Vocab length: " + vocabLength + "; Count = " + count);
		else
			System.out.println("Word2vec model loaded!");
	}
}
