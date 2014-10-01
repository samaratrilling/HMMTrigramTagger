
public class Q52 {

	public static void main (String[] args) {
	}
	
	// Part 1B of question 5
	public static void readTagTrigrams (String filename) {
		// Read in lines of tag trigram counts (in this case, from ner.counts). for each trigram,
		// call printLogProbability(trigram, true).
		
	}
	
	// Part 1A of question 5
	public static double computeQValue(String yim2, String yim1, String yi) {
		// This q value should get its trigram and bigram counts from count_freqs.py.
	}
	
	public static double computeEValue(String word, String tag) {
		
	}
	
	// Part 1B of Question 5
	public static printLogProbability (String trigram, boolean print) {
		// you have counts for all trigram combinations (or 0 if they're not in ner.counts)
		//Compute log2 q
			// this is log 2 q(word1 word2 word3)
		
		// Print out log of q value for the trigram. That is IT.
	}
	
	// Part 2 of question 5
	public static void viterbi() {
		// NO BACKPOINTERS
		// Calculate argMax for the sequence up to this point over all possible tags of
			// p (x1...xn, y1....yn).
			// = 
		
		// CRICKET			tag		log probability of THE BEST tag sequence * * tag
									/* 
									k starts at 1. tag-1 is * and tag0 is *.
									max val for k is length of tag sequence (same as length of sentence up to this point)
									// q (tagoptionsfor1 | tagoptions1-2 tagoptions1-1) *
									// q(tagoptions2 | tagoptions2-2 tagoptions2-1) *
									// q (tagoptions3 | tagoptions3-2 tagoptions3-1)
		
									// so for this one, if the set of tags consisted of two possible tags,
									// best tag seq for (0, * *) is * *. probability is 1.
									// best tag seq for (1, * CRICKET) is
									best of:
									prob(0, * *) x q(* * tag1) * e(CRICKET, tag1)
									prob(0, * *) x q(* * tag2) * e(CRICKET, tag2)
									
									Print out
									CRICKET bestTag bestProb
		// -				tag		log probability of the best tag sequence * * tag tag
									// best tag seq for (2, CRICKET -) is
									best of:
									prob(1, * CRICKET) x q(* bestTagFork1, tag1) * e(-, tag1)
									prob(1, * CRICKET) x q(* bestTagFork1, tag2) * e(-, tag2)
									
									
		// LEICESTERSHIRE	tag		log probability of tag sequence * * tag tag tag
		 * 							// best tag seq for (3, - LEICESTERSHIRE)
		// TAKE				tag		log probability of tag sequence * * tag tag tag tag
		 * */
		 
	}
}
