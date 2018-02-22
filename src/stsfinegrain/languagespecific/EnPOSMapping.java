package stsfinegrain.languagespecific;

import java.util.Arrays;

/**
* @author Vuk Batanović
*/
public class EnPOSMapping extends POSMapping{
	
	@Override
	protected void initializePosNames () {
		posNames = new String[]
			{"CC	Coordinating conjunction",
			"CD	Cardinal number",
			"DT	Determiner",
			"EX	Existential there",
			"FW	Foreign word",
			"IN	Preposition or subordinating conjunction",
			"JJ	Adjective",
			"JJR	Adjective, comparative",
			"JJS	Adjective, superlative",
			"LS	List item marker",
			"MD	Modal",
			"NN	Noun, singular or mass",
			"NNS	Noun, plural",
			"NNP	Proper noun, singular",
			"NNPS	Proper noun, plural",
			"PDT	Predeterminer",
			"POS	Possessive ending",
			"PRP	Personal pronoun",
			"PRP$	Possessive pronoun",
			"RB	Adverb",
			"RBR	Adverb, comparative",
			"RBS	Adverb, superlative",
			"RP	Particle",
			"SYM	Symbol",
			"TO	to",
			"UH	Interjection",
			"VB	Verb, base form",
			"VBD	Verb, past tense",
			"VBG	Verb, gerund or present participle",
			"VBN	Verb, past participle",
			"VBP	Verb, non-3rd person singular present",
			"VBZ	Verb, 3rd person singular present",
			"WDT	Wh-determiner",
			"WP	Wh-pronoun",
			"WP$	Possessive wh-pronoun",
			"WRB	Wh-adverb"};
	}
	
	@Override
	protected void initializeDimensionSizes() {
		lowDimensionSize = 6;
		highDimensionSize = 36;
	}
	
	@Override
	protected void initializeParameterNames() {
		parameterNamesLowDim = new String [7];
		parameterNamesLowDim[0] = "Verbs (VB, VBD, VBG, VBN, VBP, VBZ, MD, RP)";
		parameterNamesLowDim[1] = "Nouns (NN, NNP, NNS, NNPS, CD)";
		parameterNamesLowDim[2] = "Adverbs (RB, RBR, RBS, WRB)";
		parameterNamesLowDim[3] = "Adjectives (JJ, JJR, JJS)";
		parameterNamesLowDim[4] = "Pronouns (PRP, PRP$, WP, WP$)";
		parameterNamesLowDim[5] = "Others (12)";
		parameterNamesLowDim[6] = "String Similarity Weight";
		
		parameterNamesHighDim = new String [37];
		parameterNamesHighDim = Arrays.copyOf(posNames, 37);
		parameterNamesHighDim[36] = "String Similarity Weight";
	}
	
	@Override
	protected void initializeHashMaps() {
		for (int i=0; i<posNames.length; i++) {
			String posName = posNames[i].split("\\t")[0].toLowerCase();
			if (posName.contains("vb") || posName.equals("md") || posName.equals("rp"))
				posNameToIndexMapLowDim.put(posName, 0);
			else if (posName.contains("nn") || posName.equals("cd"))
				posNameToIndexMapLowDim.put(posName, 1);
			else if (posName.contains("rb"))
				posNameToIndexMapLowDim.put(posName, 2);
			else if (posName.contains("jj"))
				posNameToIndexMapLowDim.put(posName, 3);
			else if (posName.contains("prp") || posName.contains("wp"))
				posNameToIndexMapLowDim.put(posName, 4);
			else
				posNameToIndexMapLowDim.put(posName, 5);
		}
		
		for (int i=0; i<posNames.length; i++) {
			String posName = posNames[i];
			posNameToIndexMapHighDim.put(posName.split("\\t")[0].toLowerCase(), i);
		}
		
		int cnt = 0;
		for (int i=0; i<5; i++)
			for (int j=i+1; j<6; j++) {
				posPairingIndexMappingLowDim.put(i + " " + j, cnt);
				posPairingIndexMappingLowDimI.put(cnt, i);
				posPairingIndexMappingLowDimJ.put(cnt++, j);
			}
		
		cnt = 0;
		for (int i=0; i<35; i++)
			for (int j=i+1; j<36; j++) {
				posPairingIndexMappingHighDim.put(i + " " + j, cnt);
				posPairingIndexMappingHighDimI.put(cnt, i);
				posPairingIndexMappingHighDimJ.put(cnt++, j);
			}
	}
	
	public String transformMSDtoPOStag (String msd) {
		return msd;
	}
}
