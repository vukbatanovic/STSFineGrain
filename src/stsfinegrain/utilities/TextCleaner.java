package stsfinegrain.utilities;

/**
 * @author Vuk Batanović
 */
public class TextCleaner {

	/**
     * @param strLine Line of text that will be cleaned
     * @return String[] The cleaned text line as an array of tokens
     */
    public static String filter (String strLine) {
    	String regexNotOk = "( |^)[^a-zA-Z0-9šŠđĐčČćĆžŽ ]+";
    	String retLine = strLine.replaceAll(regexNotOk, "").trim();
    	return retLine.toLowerCase();
    }
}