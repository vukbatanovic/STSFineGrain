package stsfinegrain.utilities;

import java.util.HashMap;

/**
* @author Vuk Batanović
*/
public class TextVectorizer {
	private HashMap<String, Integer> dictionary = new HashMap<String, Integer> ();
	private int index = 0;
	
	public int getDictionarySize () { return dictionary.size(); }
	
	public TextVectorizer (String [] sentences) {
		for (String s: sentences) {
			String [] tokens = TextCleaner.filter(s).split("\\s+");
			for (String t: tokens)
				if (!dictionary.containsKey(t))
					dictionary.put(t, index++);
		}
	}
	
	public int [] getSentenceVector (String[] sent) {
		int [] vector = new int[dictionary.size()];
		for (String s: sent)
			vector[dictionary.get(s)] = 1;
		return vector;
	}
}
