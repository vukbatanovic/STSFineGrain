package stsfinegrain;

import stsfinegrain.Evaluator.EvaluationMode;
import stsfinegrain.languagespecific.EnPOSMapping;
import stsfinegrain.languagespecific.POSMapping;
import stsfinegrain.languagespecific.SrPOSMapping;
import stsfinegrain.similarity.SentenceSimilarity;
import stsfinegrain.similarity.algorithms.SimilarityCalculation;
import stsfinegrain.similarity.algorithms.SimilarityCalculation.SimilarityAlg;
import stsfinegrain.utilities.TermFrequencyCalculator;
import stsfinegrain.utilities.TextVectorizer;
import stsfinegrain.utilities.Word2VecModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * @author Vuk Batanović
 */
public class Experiment {
	
	public static final int MAX_THREADS = 10;
	public static final String RESULTS_DIR = "Results";
	
    public static void main(String[] args) {
    	int action = 0;
    	try {
    		action = Integer.parseInt(args[0]);
    		if (action != 0 && action != 1) throw new Exception();
    	}
    	catch (Exception e) {
    		System.out.println("Incorrect call to the program!");
    		printInstructions();
    		System.exit(1);
    	}
    	
    	// calculate term frequencies
    	if (action == 0) {
    		System.out.println("Selected action: Term frequency calculation.");
    		String [] inputCorpusPaths = new String[args.length - 2];
    		for (int i=0; i<args.length-2; i++)
    			inputCorpusPaths[i] = args[i+1];
    		String outputTermFrequenciesPath = args[args.length-1];
    		calculateTermFrequencies(inputCorpusPaths, outputTermFrequenciesPath);
    	}
    	
    	// evaluate STS model
    	else if (action == 1) {
    		System.out.println("Selected action: STS model evaluation");
    		if (args.length != 9){
        		System.out.println("Unexpected number of program arguments!");
        		printInstructions();
        		System.exit(1);
        	}
    		
    		// Selecting the STS model
    		int algorithmIndex = -1;
    		SimilarityAlg simAlgorithm = null;
    		try {
        		algorithmIndex = Integer.parseInt(args[1]);
        		switch(algorithmIndex) {
	    			case 1: simAlgorithm = SimilarityAlg.WORD_OVERLAP;
	    					break;
	    			case 2: simAlgorithm = SimilarityAlg.WORD2VEC_MEAN;
	    					break;
	    			case 3: simAlgorithm = SimilarityAlg.WORD_OVERLAP_AND_WORD2VEC_MEAN;
	    					break;
	    			case 4: simAlgorithm = SimilarityAlg.ISLAM_AND_INKPEN;
	    					break;
	    			case 5: simAlgorithm = SimilarityAlg.LINSTSS;
	    					break;
	    			case 6: simAlgorithm = SimilarityAlg.POST_STSS;
	    					break;
	    			case 7: simAlgorithm = SimilarityAlg.POS_TF_STSS;
	    					break;
	    			default: throw new Exception();
        		}
        	}
        	catch (Exception e) {
        		System.out.println("Algorithm index must be between 1 and 7!");
        		printInstructions();
        		System.exit(1);
        	}
    		System.out.println("Selected model: " + SimilarityCalculation.getSimilarityAlgorithmName(simAlgorithm));
    		
    		// Selecting the evaluation mode
    		EvaluationMode evaluationMode = null;
    		try {
    			int evaluationModeIndex = Integer.parseInt(args[2]);
    			switch(evaluationModeIndex) {
	    			case 1: evaluationMode = EvaluationMode.FULL_DATASET;
	    			break;
	    			case 2: evaluationMode = EvaluationMode.CROSS_VALIDATION;
	    			break;
	    			default: throw new Exception();
    			}
    		}
        	catch (Exception e) {
        		System.out.println("Evaluation mode index must be 1 or 2!");
        		printInstructions();
        		System.exit(1);
        	}
    		System.out.println("Selected evaluation mode: " + Evaluator.getEvaluationModeName(evaluationMode));
    		
    		// Selecting the language of the texts
    		String language = args[3].toLowerCase();
    		if (!language.equals("sr") && !language.equals("en")){
        		System.out.println("Only Serbian and English are currently supported!");
        		printInstructions();
        		System.exit(1);
        	}
    		POSMapping posMapping = null;
    		if (language.equals("sr")) {
    			posMapping = new SrPOSMapping();
    			System.out.println("Selected language: Serbian");
    		}
    		else if (language.equals("en")) {
    			posMapping = new EnPOSMapping();
    			System.out.println("Selected language: English");
    		}
    		System.out.println();
    		
    		String inputTextFilePath = args[4];
    		String inputScoreFilePath = args[5];
    		String word2VecModelPath = args[6];
    		String termFrequenciesPath = args[7];
    		String inputMsdFilePath = args[8];
    		
    		evaluateSTSModel (simAlgorithm, evaluationMode, posMapping, inputTextFilePath, inputScoreFilePath, word2VecModelPath, termFrequenciesPath, inputMsdFilePath);
    	}
    }
    
    private static void printInstructions() {
    	System.out.println("The first program argument specifies the task to be performed and must be 0 or 1.");
    	System.out.println("0 indicates that the task is the calculation of term frequencies from a corpus.");
    	System.out.println("1 indicates that the task is the evaluation of an STS model.");
    	System.out.println();
    	System.out.println("If TF calculation is chosen, and N arguments are given to the program, the arguments #2 - #N-1 are the paths of the corpora files to be used.");
    	System.out.println("The last argument is the path to the TF output file.");
    	System.out.println();
    	System.out.println("If STS model evaluation is chosen, the program expects 9 arguments.");
    	System.out.println("The argument #2 specifies the model to be evaluated, in the following coding system:");
    	System.out.println("\t1 - Word overlap\n\t2 - Mean of word2vec word vectors\n\t3 - Mixture of 1 & 2\n\t4 - Islam and Inkpen method\n\t5 - LInSTSS\n\t6 - POST STSS\n\t7 - POS-TF STSS");
    	System.out.println("The argument #3 specifies the mode of evaluation (1 for evaluation on the full dataset, 2 for 10-fold cross-validation).");
    	System.out.println("The argument #4 specifies the two-letter code of the language of the text corpora. Currently, Serbian ('SR') and English ('EN') are supported.");
    	System.out.println("The argument #5 specifies the path to the raw text file of the STS corpus to be used. Each line of the text file should contain the sentences of a pair, separated with a tab.");
    	System.out.println("The argument #6 specifies the path to the score file of the STS corpus to be used. Each line of the file should contain the score of the corresponding line pair in the file specified by argument #4. The score file can also contain other information, but the score has to be the first item in a row, separated from other data with a tab.");
    	System.out.println("The argument #7 specifies the path to the word2vec vector file that is used by all implemented STS models except the word overlap method. The vector file must be saved in the original C word2vec-tool format. For the word overlap method this argument is ignored.");
    	System.out.println("The argument #8 specifies the path to the term frequencies file created by this program. Term frequencies are only used for the LInSTSS and the POS-TF STSS models - for other models this argument is ignored.");
    	System.out.println("The argument #9 specifies the path to the STS corpus MSD/POS tag file. Each line of the text file should contain the tags of a pair of sentences, and sentences in a pair should be separated with a tab. The number of tags in a sentence should be identical to the number of tokens (not counting punctuation, which is eliminated in the text-cleaning phase). POS/MSD tags are only used for the POST STSS and the POS-TF STSS models - for other models this argument is ignored.");
    	System.out.println();
    }
    
    public static void evaluateSTSModel (SimilarityAlg simAlgorithm, EvaluationMode evaluationMode, POSMapping posMapping, String inputTextFilePath, String inputScoreFilePath, String word2VecModelPath, String termFrequenciesPath, String inputMsdFilePath) {
    	long timeStart = System.currentTimeMillis();
		File directoryMain = new File(RESULTS_DIR);
		if (!directoryMain.exists())
			directoryMain.mkdirs();
    	try {
    		SimilarityCalculation [] allResults = scoreFile(simAlgorithm, inputTextFilePath, inputScoreFilePath, word2VecModelPath, termFrequenciesPath, inputMsdFilePath);
	        Evaluator rg = new Evaluator (RESULTS_DIR + "/Results.txt", allResults, evaluationMode, posMapping);
	        System.out.println("Final Pearson correlation: " + rg.getFinalPearson());
		}
    	catch (Exception e) {
			e.printStackTrace();
		}
	    long timeEnd = System.currentTimeMillis();
	    System.out.println("Execution time: " + (timeEnd-timeStart)/1000.0 + " s");
    }
    
    public static void calculateTermFrequencies (String[] inputCorpusPaths, String outputTermFrequenciesPath) {
        try {
	    	File fileOut = new File(outputTermFrequenciesPath);
	        if (fileOut.exists()) {
	            fileOut.delete();
	            fileOut.createNewFile();
	        }
	    	TermFrequencyCalculator termFrequencyCalculator = new TermFrequencyCalculator ();
	        termFrequencyCalculator.buildLexiconAndCalculateFreq(inputCorpusPaths, outputTermFrequenciesPath);
        } 
        catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private static SimilarityCalculation[] scoreFile (SimilarityAlg simAlgorithm, String inputTextFilePath, String inputScoreFilePath, String word2VecModelPath, String termFrequenciesPath, String inputMsdFilePath) throws IOException, InterruptedException {
        File inputTextFile = new File (inputTextFilePath);
        File inputScoreFile = new File (inputScoreFilePath);
        BufferedReader brText = new BufferedReader (new InputStreamReader(new FileInputStream(inputTextFile), "UTF-8"));
        BufferedReader brScore = new BufferedReader (new InputStreamReader(new FileInputStream(inputScoreFile), "UTF-8"));
    	
    	Word2VecModel model = null;
    	if (simAlgorithm != SimilarityAlg.WORD_OVERLAP)
    		model = new Word2VecModel(word2VecModelPath);
    	HashMap<String, Double> lexiconTF_Norm0_1 = null;
    	if (termFrequenciesPath != null && (simAlgorithm == SimilarityAlg.LINSTSS || simAlgorithm == SimilarityAlg.POS_TF_STSS)) {
	    	TermFrequencyCalculator termFrequencies = new TermFrequencyCalculator();
	    	termFrequencies.loadLexiconsFromFile(termFrequenciesPath);
	    	lexiconTF_Norm0_1 = termFrequencies.getLexiconTF_Norm0_1();
    	}
    	
    	TextVectorizer vectorizer = null;
    	if (simAlgorithm == SimilarityAlg.WORD_OVERLAP || simAlgorithm == SimilarityAlg.WORD_OVERLAP_AND_WORD2VEC_MEAN) {
            String line = null;
            String [] sentences = new String [2*Evaluator.PAIR_COUNT];
            int i = 0;
            while ((line = brText.readLine()) != null) {
            	String [] strs = line.split("\t");
            	sentences[i++] = strs[0];
            	sentences[i++] = strs[1];
            }
            vectorizer = new TextVectorizer (sentences);
            brText.close();
            brText = new BufferedReader (new InputStreamReader(new FileInputStream(inputTextFile), "UTF-8"));
    	}
    	
    	String [] msds1 = null, msds2 = null;
    	if (simAlgorithm == SimilarityAlg.POST_STSS || simAlgorithm == SimilarityAlg.POS_TF_STSS) {
            File inputMsdFile = new File (inputMsdFilePath);
            BufferedReader brMsd = new BufferedReader (new InputStreamReader(new FileInputStream(inputMsdFile), "UTF-8"));
            msds1 = new String [Evaluator.PAIR_COUNT];
            msds2 = new String [Evaluator.PAIR_COUNT];
            String line = null;
            int i = 0;
            while ((line = brMsd.readLine()) != null) {
            	String [] parts = line.split("\t");
            	msds1[i] = parts[0];
            	msds2[i++] = parts[1];
            }
            brMsd.close();
    	}

        SimilarityCalculation [] simCalcVector = new SimilarityCalculation [Evaluator.PAIR_COUNT];
        String lineText = null, lineScore = null;
        int lineCnt = 1;
        int [] currentThreads = new int [1];
        currentThreads[0] = 0;
        while ((lineText = brText.readLine()) != null) {
        	lineScore = brScore.readLine();
        	String [] sentences = lineText.split("\t");
            String score = lineScore.split("\t")[0];
			synchronized (simCalcVector) {
        		while (currentThreads[0] > Experiment.MAX_THREADS)
        			simCalcVector.wait();
			}
			synchronized(currentThreads) {
				currentThreads[0]++;
			}
			SentenceSimilarity sThread;
	    	if (simAlgorithm == SimilarityAlg.POST_STSS || simAlgorithm == SimilarityAlg.POS_TF_STSS)
	    		sThread = new SentenceSimilarity (simAlgorithm, score, sentences[0], sentences[1], msds1[lineCnt-1].split("\\s+"), msds2[lineCnt-1].split("\\s+"), lineCnt, simCalcVector, currentThreads, vectorizer, model, lexiconTF_Norm0_1);
	    	else
	    		sThread = new SentenceSimilarity (simAlgorithm, score, sentences[0], sentences[1], null, null, lineCnt, simCalcVector, currentThreads, vectorizer, model, lexiconTF_Norm0_1);
			sThread.start();
        	lineCnt++;
        }
        synchronized (simCalcVector) {
        	while (currentThreads[0] > 0)
        		simCalcVector.wait();
        }
        brText.close();
        brScore.close();
        System.out.println("Finished scoring file " + inputTextFilePath);
        return simCalcVector;
    }
}