package stsfinegrain.languagespecific;

import java.util.Arrays;

/**
* @author Vuk Batanović
*/
public class SrPOSMapping extends POSMapping {

	@Override
	protected void initializePosNames() {
		posNames = new String[] {
				"Nc	Noun common",
				"Np	Noun proper",
				"Vm	Verb main",
				"Va	Verb auxiliary",
				"Ag	Adjective general",
				"As	Adjective possessive",
				"Ap	Adjective participle",
				"Pp	Pronoun personal",
				"Pd	Pronoun demonstrative",
				"Pi	Pronoun indefinite",
				"Ps	Pronoun possessive",
				"Pq	Pronoun interrogative",
				"Px	Pronoun reflexive",
				"Rg	Adverb general",
				"Rr	Adverb participle",
				"S	Adposition",
				"Cc	Conjunction coordinating",
				"Cs	Conjunction subordinating",
				"M_c	Numeral cardinal",
				"M_o	Numeral ordinal",
				"M_m	Numeral multiple",
				"M_s	Numeral special",
				"Qz	Particle negative",
				"Qq	Particle interrogative",
				"Qo	Particle modal",
				"Qr	Particle affirmative",
				"I	Interjection",
				"Y	Abbreviation",
				"X	Residual"
		};	
	}
	
	@Override
	protected void initializeDimensionSizes() {
		lowDimensionSize = 7;
		highDimensionSize = 29;
	}

	@Override
	protected void initializeParameterNames() {
		parameterNamesLowDim = new String [8];
		parameterNamesLowDim[0] = "Verbs (Vm, Va)";
		parameterNamesLowDim[1] = "Nouns (Nc, Np)";
		parameterNamesLowDim[2] = "Adverbs (Rg, Rr)";
		parameterNamesLowDim[3] = "Adjectives (Ag, As, Ap)";
		parameterNamesLowDim[4] = "Pronouns (Pp, Pd, Pi, Ps, Pq, Px)";
		parameterNamesLowDim[5] = "Numerals (M_c, M_o, M_m, M_s)";
		parameterNamesLowDim[6] = "Others (10)";
		parameterNamesLowDim[7] = "String Similarity Weight";
		
		parameterNamesHighDim = new String [30];
		parameterNamesHighDim = Arrays.copyOf(posNames, 30);
		parameterNamesHighDim[29] = "String Similarity Weight";
	}

	@Override
	protected void initializeHashMaps() {
		for (int i=0; i<posNames.length; i++) {
			String posName = posNames[i].split("\\t")[0].toLowerCase();
			if (posName.startsWith("v"))
				posNameToIndexMapLowDim.put(posName, 0);
			else if (posName.startsWith("n"))
				posNameToIndexMapLowDim.put(posName, 1);
			else if (posName.startsWith("r"))
				posNameToIndexMapLowDim.put(posName, 2);
			else if (posName.startsWith("a"))
				posNameToIndexMapLowDim.put(posName, 3);
			else if (posName.startsWith("p"))
				posNameToIndexMapLowDim.put(posName, 4);
			else if (posName.startsWith("m"))
				posNameToIndexMapLowDim.put(posName, 5);
			else
				posNameToIndexMapLowDim.put(posName, 6);
		}
		
		for (int i=0; i<posNames.length; i++) {
			String posName = posNames[i];
			posNameToIndexMapHighDim.put(posName.split("\\t")[0].toLowerCase(), i);
		}
		
		int cnt = 0;
		for (int i=0; i<6; i++)
			for (int j=i+1; j<7; j++) {
				posPairingIndexMappingLowDim.put(i + " " + j, cnt);
				posPairingIndexMappingLowDimI.put(cnt, i);
				posPairingIndexMappingLowDimJ.put(cnt++, j);
			}
		
		cnt = 0;
		for (int i=0; i<28; i++)
			for (int j=i+1; j<29; j++) {
				posPairingIndexMappingHighDim.put(i + " " + j, cnt);
				posPairingIndexMappingHighDimI.put(cnt, i);
				posPairingIndexMappingHighDimJ.put(cnt++, j);
			}
	}
	
	@Override
	public String transformMSDtoPOStag (String msd) {
		if (msd.length() == 1)
			return msd;
		else if (msd.length() == 2)
			switch (msd.charAt(0)) {
				case 'c':
				case 'p':
				case 'q':
				case 'r':
					return msd;
				case 's':
					return "s";
				case 'x':
					return "x";
				default:
					return null;
			}
		else if (msd.startsWith("m"))
			switch (msd.charAt(2)) {
				case 'c':
					return "m_c";
				case 'o':
					return "m_o";
				case 'm':
					return "m_m";
				case 's':
					return "m_s";
				default:
					return null;
			}
		else
			return msd.substring(0, 2);
	}
}
