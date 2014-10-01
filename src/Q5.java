import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;


public class Q5 {
	public static HashMap<String, Integer> nGramCounts;
	public static ArrayList<String> tags;
	// tagFreqs: tag -> count
	public static HashMap<String, Integer> tagFreqs;
	// tagProbs: word -> {[TOTAL, count], [TAG1, count], [TAG2, count]}
	public static HashMap<String, ArrayList<String[]>> tagProbs;
	
	public static void main (String[] args) {
		
		Scanner in = new Scanner(System.in);
		System.out.println("You're about to run question 5. I'm reading tag n-gram counts from ner.counts");
		String countsFile = "ner2.counts";
		String devFile = "ner_dev.dat";
		String taggedFile = "q5ViterbiTagged.dat";
		nGramCounts = new HashMap<String, Integer>();
		tags = new ArrayList<String>();
		tagFreqs = new HashMap<String, Integer>();
		tagProbs = new HashMap<String, ArrayList<String[]>>();

		try {
			FileWriter taggedWriter = new FileWriter(taggedFile, true);
			// Populate the nGram counts, list of tags, tag counts, and word/tag counts.
			nGramCounts = readTagCounts(countsFile);
			ArrayList<String> sentences = readDevFile(devFile);
			for (String s : sentences) {
				/*if(s.startsWith("Jones Medical") || s.startsWith("A _RARE_ man , detained")) {
					continue;
				}*/
				ArrayList<String> viterbiTagged = viterbiAlgorithm(s);
				for (String sent : viterbiTagged) {
					taggedWriter.write(sent);
					taggedWriter.write("\n");
					taggedWriter.flush();
				}
				taggedWriter.write("\n");
				taggedWriter.flush();
			}
			//viterbiAlgorithm("Jones Medical _RARE_ acquisition");
			//viterbiAlgorithm("France on Friday _RARE_ another African man seized in a police _RARE_ on a Paris church as about 100 Air");

		}
		
		catch (IOException e){
			e.printStackTrace();
		}
		
	}
	
	public static ArrayList<String> readDevFile(String devFile) throws IOException{
		ArrayList<String> sentences = new ArrayList<String>();
		BufferedReader readSentences = new BufferedReader(new FileReader(devFile));
		
		ArrayList<String> words = new ArrayList<String>();
		String wordLine = readSentences.readLine();
		while(wordLine != null) {
			if (wordLine.equals("")) {
				// You've reached the end of a sentence.
				StringBuffer sentence = new StringBuffer();
				for (String word : words) {
					sentence.append(word);
					sentence.append(" ");
				}
				String strSentence = sentence.toString();
				strSentence = strSentence.trim();
				sentences.add(strSentence);
				words = new ArrayList<String>();
				System.out.println(strSentence);
			}
			else {
				words.add(wordLine);
			}
			wordLine = readSentences.readLine();
		}

		readSentences.close();
		return sentences;
	}
	
	public static HashMap<String, Integer> readTagCounts(String filename) throws IOException{
		// nGramCounts: nGram -> count
		HashMap<String, Integer> nGramCounts = new HashMap<String, Integer>();
		// tagFreqs: tag -> count
		tagFreqs = new HashMap<String, Integer>();
		// tagProbs: word -> {[TOTAL, count], [TAG1, count], [TAG2, count]}
		tagProbs = new HashMap<String, ArrayList<String[]>>();
		
		BufferedReader readTagCounts;
		readTagCounts = new BufferedReader(new FileReader(filename));
		String countLine = readTagCounts.readLine();
		while (countLine != null) {
			String[] components = countLine.split(" ");
			if (components[1].equals("WORDTAG")) {
				String word = components[3];
				String tag = components[2];
				if (!tags.contains(tag)) {
					tags.add(tag);
				}
				String wordAndTagCount = components[0];
				String[] wordTagFreq = {tag, wordAndTagCount};
				
				// Update tag seen frequency
				int currentFreq = 0;
				if (tagFreqs.containsKey(tag)) {
					currentFreq = tagFreqs.get(tag);
					currentFreq += Integer.parseInt(wordAndTagCount);
				}
				else {
					currentFreq = Integer.parseInt(wordAndTagCount);
				}
				tagFreqs.put(tag, currentFreq);
				
				// Update tag+word frequency and word frequency.
				if (tagProbs.containsKey(word)) {
					// Add the new tag on to the end of the list.
					ArrayList<String[]> existingTags = tagProbs.get(word);
					existingTags.add(wordTagFreq);
					// Update the 'word seen' total.
					String[] currentTotal = existingTags.get(0);
					currentTotal[1] += wordAndTagCount;
					existingTags.set(0, currentTotal);
					tagProbs.put(word, existingTags);
				}
				else {
					// Create the current total and first tag object, put in Hashmap.
					String[] currentTotal = {"TOTAL", wordAndTagCount};
					ArrayList<String[]> tags = new ArrayList<String[]>();
					tags.add(currentTotal);
					tags.add(wordTagFreq);
					tagProbs.put(word, tags);
				}
			}
			else if (components[1].equals("2-GRAM") || components[1].equals("3-GRAM")){
				int count = Integer.parseInt(components[0]);
				// Start by assuming we're dealing with a bigram
				String ngram = components[2] + " " + components[3];
				
				// If we're dealing with a trigram
				if (components.length == 5) {
					ngram = ngram + " " + components[4];
				}
				nGramCounts.put(ngram, count);
			}
			countLine = readTagCounts.readLine();
		}
		//System.out.println(tagProbs.get("France").toString());
		readTagCounts.close();
		return nGramCounts;
	}
	
	public static double computeQValue(String yim2, String yim1, String yi) {
		String trigram = yim2 + " " + yim1 + " " + yi;
		double trigramCount = 0;
		if (nGramCounts.containsKey(trigram)) {
			trigramCount = nGramCounts.get(trigram);
		}
		String bigram = yim2 + " " + yim1;
		double bigramCount = 0;
		if (nGramCounts.containsKey(bigram)) {
			bigramCount = nGramCounts.get(bigram);
		}
		//System.out.println("Computing " + trigramCount + "/" + bigramCount);
		return trigramCount / bigramCount;
	}
	
	public static double computeEValue(String word, String tag) {
		// # times tag y is paired with tag x
		double countXY = 0;
		// # times tag y is seen
		double countY;

		//System.out.println("Computing e " + word + "|" + tag);
		if (tagProbs.containsKey(word)) {
			ArrayList<String[]> wordStats = tagProbs.get(word);
			// word -> {[total, count], [tag, count], [tag, count]}
			for (String[] s: wordStats) {
				if (s[0].equals(tag)) {
					countXY = Double.parseDouble(s[1]);
					break;
				}
			}
		}
		// Use the 'rare' emission parameter.
		else {
			ArrayList<String[]> wordStats = tagProbs.get("_RARE_");
			for (String[] s: wordStats) {
				if (s[0].equals(tag)) {
					countXY = Double.parseDouble(s[1]);
					break;
				}
			}
		}
		countY = tagFreqs.get(tag);
		return (Double) countXY / countY;
	}
	
	public static ArrayList<String> viterbiAlgorithm (String sentence) {
		String[] words = sentence.split(" ");
		int n = words.length;
		
		
		ArrayList<String> S = tags;
		
		// Initialize possible tags for each k value
		HashMap<Integer, ArrayList<String>> Sk = new HashMap<Integer, ArrayList<String>>();
		// Define an arrayList with just the start symbol inside
		ArrayList<String> startSymbol = new ArrayList<String>();
		startSymbol.add("*");
		// S(-1) = *, S(0) = *
		Sk.put(new Integer(-1), startSymbol);
		Sk.put(new Integer(0), startSymbol);
		for (int i = 1; i <= n; i++) {
			Sk.put(new Integer(i), S);
		}
		
		// k, u, v => log probability
		HashMap<String, Pi> logPiStorage = new HashMap<String, Pi>();
		
		// Create the base case.
		// pi(0, *, *) = 1. Log of that is 0.
		String base = "0 * *";
		logPiStorage.put(base, new Pi(new Double(0), null, "*", 0));
		
		for (int k = 1; k <= n; k++) {
			// For all possible tags for position k-1
			ArrayList<String> allUs = Sk.get(k-1);
			for (int uIndex = 0; uIndex < allUs.size(); uIndex++) {

				String uVal = allUs.get(uIndex);
				
				// For all possible tags for position k, find the best.
				ArrayList<String> allVs = Sk.get(k);
				for (int vIndex = 0; vIndex < allVs.size(); vIndex++) {
					String vVal = allVs.get(vIndex);
					String newKey = new Integer(k).toString() + " " +  uVal + " " + vVal;
					//System.out.println("NEW KEY = " + newKey);
					
					double bestProbSoFar = Double.NEGATIVE_INFINITY; // value of base.
					String bestTagSoFar = "";
					Pi bestLogPiPrev = null;
					// For all possible tags for position k-2
					ArrayList<String> allWs = Sk.get(k-2);
					for (int wIndex = 0; wIndex < allWs.size(); wIndex++) {
						String wVal = allWs.get(wIndex);
						// key: {k, w, u} => log value
						String prevKey = new Integer(k-1).toString() + " " + wVal + " " + uVal;
						
						Pi logPiPrev = logPiStorage.get(prevKey);
						// Add instead of multiply because of log distribution rules.
						if (logPiPrev != null) {

							if (newKey.equals("2 I-PER I-ORG")) {
								System.out.println();
							}

							double logProbKUV = logPiPrev.logProbability +
									Math.log(computeQValue(wVal, uVal, vVal)) + 
									Math.log(computeEValue(words[k-1], vVal));
									// Get the value of words[k-1] instead of k because
									// sentences index their words starting at 1 and arrays
									// index their words starting at 0. We start k at 1, so the
									// minimum value for this is 0.
								//if (logProbKUV != Double.NEGATIVE_INFINITY) {

							/*System.out.println(newKey);
									System.out.println("double logProbKUV = "+logPiPrev.logProbability+" + "
									+Math.log(computeQValue(wVal, uVal, vVal))+" + "+
									Math.log(computeEValue(words[k-1], vVal)));
								//}*/
							
							if (logProbKUV >= bestProbSoFar) {
								bestProbSoFar = logProbKUV;
								bestTagSoFar = vVal;
								bestLogPiPrev = logPiPrev;
							}
						}
					} // end w iteration
					// Store the kuv value for the best w value
					//System.out.println("best probability so far: " + bestProbSoFar + " for tags " + newKey);
					logPiStorage.put(newKey, new Pi(bestProbSoFar, bestLogPiPrev, bestTagSoFar, k));
				} // end v iteration	
			} // end u iteration
		} // end k iteration
		
		// Go follow the backpointers back through the sentence.
		// Start from the end of the sentence. ALWAYS. This is the only way you'll get the best
		// path. Each time you're only calculating up to that point.
		
		// Find the best pi value for n, u, v.
		double bestProbSoFar = Double.NEGATIVE_INFINITY;
		Pi bestLogPrev = null;
		
		ArrayList<String> allUs = Sk.get(n-1);
		for (int uIndex = 0; uIndex < allUs.size(); uIndex++) {
			String uVal = allUs.get(uIndex);
			ArrayList<String> allVs = Sk.get(n);
			for (int vIndex = 0; vIndex < allVs.size(); vIndex++) {
				String vVal = allVs.get(vIndex);
				
				String NUVKey = "" + n + " " + uVal + " " + vVal;
				Pi potentialBestNUV = logPiStorage.get(NUVKey);
				//if (potentialBestNUV.logProbability != Double.NEGATIVE_INFINITY) {
				//	System.out.println("potential best NUV nonneginfinity = " + potentialBestNUV.logProbability);
				//}
				double logProbSentence = potentialBestNUV.logProbability +
						Math.log(computeQValue(uVal, vVal, "STOP"));
				if (logProbSentence > bestProbSoFar) {
					bestProbSoFar = logProbSentence;
					bestLogPrev = potentialBestNUV;
					//System.out.println("NUV key: " + NUVKey);
				}
			}
		}

		ArrayList<String> bestTagSequence = new ArrayList<String>();
		if (bestLogPrev == null) {
			for (int i = 0; i < words.length; i++) {
				String word = words[i];
				String[] mostLikelyTag;
				if (tagProbs.containsKey(word)) {
					mostLikelyTag = computeMostLikelyTag(word, tagProbs, tagFreqs);
				}
				else {
					// Treat as _RARE_
					mostLikelyTag = computeMostLikelyTag("_RARE_", tagProbs, tagFreqs);
				}
				String bestTag = mostLikelyTag[0] + " " + mostLikelyTag[1];
				// Most likely tag: [tag, probability]
				bestTagSequence.add(bestTag);
			}
		}
		else {
			while (!bestLogPrev.v.equals("*")) {
				String bestTag = bestLogPrev.v + " " + bestLogPrev.logProbability;
				bestTagSequence.add(bestTag);
				//System.out.println(bestLogPrev.k);
				bestLogPrev = bestLogPrev.backPointer;
			}
		}
		Collections.reverse(bestTagSequence);
		//System.out.println(bestTagSequence);
		
		System.out.println("Tag seq length: " + bestTagSequence.size());
		System.out.println("sentence length : " + words.length);
		
		ArrayList<String> viterbiTagged = new ArrayList<String>();
		for (int i = 0; i<bestTagSequence.size(); i++) {
			String fullTag = words[i] + " " + bestTagSequence.get(i);
			viterbiTagged.add(fullTag);
		}
		System.out.println(viterbiTagged);
		return viterbiTagged;
		
	}
	
	/**
	 * Failsafe method for if there is no optimal tagging. This is the same as in part 4
	 * where we calculated optimal tagging based solely on emission parameter.
	 * Yinghui recommended this as the solution for the few sentences where we haven't seen the
	 * trigram before (e.g. Jones Medical => (* I-PER I-ORG) but we have not seen that possible
	 * tag trigram before.
	 * @param word
	 * @param tagProbs
	 * @param tagFreqs
	 * @return
	 */
	public static String[] computeMostLikelyTag (String word, HashMap<String, ArrayList<String[]>> tagProbs,
			HashMap<String, Integer> tagFreqs) {
		ArrayList<String[]> wordStats = tagProbs.get(word);
		double highestTagProb = Double.MIN_VALUE;
		String mostLikelyTag = "";
		for (String[] s : wordStats) {
			if (s[0].equals("TOTAL")) {
				continue;
			}
			else {
				String tag = s[0];
				double tagSeenCount = tagFreqs.get(tag);
				double wordTaggedCount = Double.parseDouble(s[1]);
				double tagProb = wordTaggedCount / tagSeenCount;
				//System.out.println("Tag under consideration: " + tag + " tagged " + word + " " +
				//		wordTaggedCount + " times out of " + tagSeenCount + " times seen. Emission param: " + tagProb);
				if (tagProb > highestTagProb) {
					highestTagProb = tagProb;
					mostLikelyTag = tag;
				}
			}
		}
		String[] mostLikelyTagAndProb = {mostLikelyTag, Double.toString(highestTagProb)};
		return mostLikelyTagAndProb;
	}
	
	/**
	 * Computes probability that the given tag sequence will be paired with the given word sequence.
	 * @param tagSequence
	 * @param words
	 * @return
	 */
	private static double computeRValue (String[] tagSequence, String[] words) {
		
		double productOfQs = 1;
		double productOfEs = 1;
		for (int i = 2; i < tagSequence.length; i++) {
			productOfQs = productOfQs * computeQValue(tagSequence[i-2], tagSequence[i-1], tagSequence[i]);
			productOfEs = productOfEs * computeEValue(words[i], tagSequence[i]);
		}
		return productOfQs * productOfEs;
	}
	
}
