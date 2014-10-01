import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


/**
 * @author samara trilling
 * Question 4 functions
 */

public class Q4 {
	public static void main (String[] args) {
		
		Scanner in = new Scanner(System.in);
		System.out.println("You're about to run question 4.\nDo you want to rerun the replacement" + 
				" function for rare words?\nPress 1 for yes, 2 if you want to just use the already-created" +
				" count2.dat.");
		int choice = in.nextInt();
		try {
			if (choice == 1) {
				rerunRareReplacement();
			}
			System.out.println("Using a simple HMM tagger to generate probabilities for all words in ner_dev.dat.\n" + 
					"Results will be in q4Tagged.dat.\n");
			//calculateAllProbabilities("ner2.counts", "ner_dev.dat", "q4Tagged.dat");
		}
		
		catch(IOException e) {
			e.printStackTrace();	
		}

	}
	
	/**
	 * A. Write a function that computes emission param for given tag and word.
	 * Produces e(x|y) for each word x. e(x|y) = (#times tag y has been paired with word x) / #times state has been seen.
	 * @param tag
	 * @param word
	 * @param tagProbs
	 * @return
	 */
	public static double calcEmissionParam (double wordTaggedCount, double tagSeenCount) {
		return wordTaggedCount / tagSeenCount;
	}
	
	/**
	 * C. Part of simple HMM tagger - produces mostLikelyTag = argMax e(x|y) for each word x.
	 * @param word
	 * @param tagProbs
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
				double tagProb = calcEmissionParam(wordTaggedCount, tagSeenCount);
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
	
	// C: Calculate tag probabilities for all words in ner_dev.dat.
	// If the word has been seen before, calculate the most likely tag for it.
	// If it has not been seen before, calculate the most likely tag for _RARE_.
	public static void calculateAllProbabilities (String countFile, String fileToTag, String taggedFile) throws IOException{
		// INITIALIZE FREQUENCY COUNTS FOR WORDS AND TAGS BY READING IN FILE
		// Read in counts2 (the one that has the RARE replacements).
		BufferedReader readCounts, readToTag;
		FileWriter writer;
		
		// tagProbs: word -> {[TOTAL, count], [TAG1, count], [TAG2, count]}
		HashMap<String, ArrayList<String[]>> tagProbs = new HashMap<String, ArrayList<String[]>>();
		HashMap<String, Integer> tagFreqs = new HashMap<String, Integer>();
		readCounts = new BufferedReader(new FileReader(countFile));
		readToTag = new BufferedReader(new FileReader(fileToTag));
		writer = new FileWriter(taggedFile, true);
		
		// Add each word to a hashmap: key: word. value: arrayList containing 2-item string arrays.
		// For each word, the first array will always be [TOTAL, #timesWordSeen]. The others will be
		// [TAG, #timesTagAppliedToThisWord]
		// Also, keep track of the number of times each tag has been seen in a separate hashmap.
		String countLine = readCounts.readLine();
		while (countLine != null) {
			String[] components = countLine.split(" ");
			// This assumes the n-gram information will always be at the end of the counts file, not
			// at the beginning (i.e. once you've gotten to the ngram stuff, stop scanning).
			if (!components[1].equals("WORDTAG")) {
				break;
			}
			else {
				String word = components[3];
				String tag = components[2];
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
			countLine = readCounts.readLine();
		}
		// Read in nerdev.dat.
		// For each word, check if that word is in tagProbabilities hashmap. If not, 
		// write the most likely tag (using emission params) for _RARE_ + its prob. If so,
		// write the most likely tag (using emission params) for the word + its prob.
		// Lines will be written to q4Tagged.dat in the form:
		// Nations I-ORG -9.2103403719761818
		String word = readToTag.readLine();
		String newLine;
		while (word != null) {
			if (word.equals("")) {
				newLine = "";
			}
			else {
				String[] mostLikelyTag;
				if (tagProbs.containsKey(word)) {
					mostLikelyTag = computeMostLikelyTag(word, tagProbs, tagFreqs);
				}
				else {
					// Treat as _RARE_
					mostLikelyTag = computeMostLikelyTag("_RARE_", tagProbs, tagFreqs);
				}
				// Most likely tag: [tag, probability]
				newLine = word + " " + mostLikelyTag[0] + " " + mostLikelyTag[1];
			}
			
			word = readToTag.readLine();
			
			writer.write(newLine);
			writer.write("\n");
			writer.flush();
		}
		
		readCounts.close();
		writer.close();
		readToTag.close();
	}
	
	public static void rerunRareReplacement() throws IOException {
	// B: Replace infrequent words in ner_train.dat with "_RARE_"
		BufferedReader readCounts, readTraining, readTest;
		FileWriter trainWriter, testWriter;
		ArrayList<String> toReplace = new ArrayList<String>();
		HashMap<String, Boolean> seen = new HashMap<String, Boolean>();
		readCounts = new BufferedReader(new FileReader("ner.counts"));
		readTraining = new BufferedReader(new FileReader("ner_train.dat"));
		readTest = new BufferedReader(new FileReader("ner_dev.dat"));
		trainWriter = new FileWriter("ner_train2.dat", true);
		testWriter = new FileWriter("ner_dev2.dat", true);

		// Get the strings that need to be replaced.
		String line = readCounts.readLine();
		while (line != null) {
			String[] components = line.split(" ");
			int freq = Integer.parseInt(components[0]);
			// Don't replace words that already have 3-GRAM or something
			if (components[1].equals("WORDTAG")) {
				if (freq < 5) {
					// Tag word for replacement with _RARE_
					toReplace.add(components[3] + " " + components[2]);
				}
				else {
					// Otherwise mark them as seen, so we can tell the difference between
					// words that haven't ever been seen in the training data and words that have
					// been seen but only less than 5 times
					seen.put(components[3], true);
				}
			}
			line = readCounts.readLine();
		}
		
		// Replace the rare words in training data in a new file.
		String targetLine = readTraining.readLine();
		while (targetLine != null) {
			for (String s : toReplace) {
				String toCompare = targetLine.split(" ")[0];
				if (targetLine.equals(s)) {
					//System.out.println("toCompare: " + toCompare + " fullWord: " + s);
					// Only replace toCompare if it's its own word, not if it's inside another
					for (int i = 0; i<s.length(); i++) {
						
					}
					targetLine = targetLine.replace(toCompare + " ", "_RARE_ ");
					System.out.println(targetLine);
					//System.out.println("new target line: " + targetLine);
					break;
				}
			}
			trainWriter.write(targetLine);
			trainWriter.write("\n");
			trainWriter.flush();
			targetLine = readTraining.readLine();
		}
		
		// Replace the rare words in test data in a new file.
		String testLine = readTest.readLine();
		while(testLine != null) {
			if (!testLine.equals("")) {
				if (toReplace.contains(testLine) || seen.get(testLine) == null) {
					testLine = "_RARE_";
				}
			}
			testWriter.write(testLine);
			testWriter.write("\n");
			testWriter.flush();
			testLine = readTest.readLine();
		}
		
		readCounts.close();
		trainWriter.close();
		readTraining.close();
		testWriter.close();
		readTest.close();
	}

}
