Samara Trilling
sat2160

HOMEWORK 1 REPORT
----------------------------
RUN INSTRUCTION
----
FOR QUESTION 4:

Run Q4.java.
- You have the option of rerunning the function that replaces rare words OR
just using the pre-saved output in count2.dat

	- If you choose to rerun the function, note that running the function that replaces rare words
	with _RARE_ can take up to 15 minutes. The replacement function produces ner_train2.dat.
	You should then run count_freqs.py on ner_train2.dat and pipe the results into a new file. (I've done this
	into ner2.counts).
	$	python count_freqs.py ner_train2.dat > newFile

	- If you choose to use the already-saved output: I have saved the output of re-running count_freqs.py
	on ner_train2.dat into ner2.counts, so you can view it there.

-
FOR QUESTION 5:

Run Q5.java.

-
FOR QUESTION 6:

Run Q6.java. It will ask if you want to rerun the function that replaces rare words OR
just use the pre-saved output in count3.dat.
- If you choose to rerun the function, note that running the function that replaces rare words
	can take up to 15 minutes. The replacement function produces ner_train3.dat.
	You should then run count_freqs.py on ner_train3.dat and pipe the results into a new file.
	(I've done this into ner3.counts).
	$	python count_freqs.py ner_train3.dat > newFile

	- If you choose to use the already-saved output: I have saved the output of re-running count_freqs.py
	on ner_train3.dat into ner3.counts, so you can view it there.
	You can then run Q5 using the updated data by changing lines 23 and 25 in Q5.java to:

		23 String countsFile = "ner3.counts";
		24 String devFile = "ner_dev.dat";
		25 String taggedFile = "q6ViterbiTagged.dat";


--------------------------------------------------------------------------------------------------
DESIGN
----

A. For question 4B, the function that replaces low-occurrence words with _RARE_ appends to the file
instead of overwriting, so if you want to run it more than once, you'll have to delete the file it
writes to, ner_train2.dat, before running it again.

B. For question 4C, I have assumed that the n-gram information (as opposed to the WORDTAG data) will
always be at the end of the counts file, not at the beginning (i.e. once you've gotten to the ngram stuff,
it stops scanning. This is just to lower runtime and could be changed if the data format changes).

C. For question 5A, I'm assuming that the bigrams and trigrams we're reading in will be perfect data - 
e.g. there will only be one line for each trigram or bigram.

D. For question 6, the code is virtually the same as in question 4, it just has a few more options for
_RARE_ tags.
--------------------------------------------------------------------------------------------------
PERFORMANCE for algorithm (precision, recall, F-score)
and OBSERVATIONS
----
Q4:
Found 13720 NEs. Expected 5931 NEs; Correct: 2904.

	     precision 	recall 		F1-Score
Total:	 0.211662	0.489631	0.295557
PER:	 0.429461	0.225245	0.295503
ORG:	 0.522908	0.392377	0.448335
LOC:	 0.825512	0.681025	0.746340
MISC:	 0.069929	0.777416	0.128315

I noticed that because one tag is only ever used to tag RARE words,
its emission value ends up being 1 and so it is the one used for all
previously unseen or rare words. This is unfortunate, and is part of the cause
for the low correctness.
The highest score is for the LOC tag, which seems to indicate that this tag is
very emission-parameter sensitive - the correctness of the emission parameter
directs the correctness of the tag.
----
Q5:
Found 4768 NEs. Expected 5931 NEs; Correct: 3587.

	 precision 	recall 		F1-Score
Total:	 0.752307	0.604788	0.670530
PER:	 0.767819	0.591948	0.668510
ORG:	 0.569470	0.465620	0.512336
LOC:	 0.863042	0.683751	0.763006
MISC:	 0.773632	0.675353	0.721159
This is a pretty good evaluation (according to Yinghui) - it's definitely
better than the just-emission-param evaluation. The precision is much higher,
the average recall is significantly higher and the F-Scores are also significantly
higher.
The one exception is the LOC tag - this one seems much less sensitive to q parameters.
For LOC, if the emission parameter indicates that it is a LOC, there is a much higher
chance that it actually is a LOC. Its place in a sentence doesn't affect its
tag that much. There is very low ambiguity for location words, and they are unlikely
to be misinterpreted as a different type of tag because of this.
Other tags are more sensitive to the q-value and the previously seen words - this is
because they have more diverse potential meanings and need more context to determine
what the correct tag is.
----
Q6:
Found 5855 NEs. Expected 5931 NEs; Correct: 4182.
	 precision 	recall 		F1-Score
Total:	 0.714261	0.705109	0.709656
PER:	 0.776796	0.764962	0.770833
ORG:	 0.501156	0.647982	0.565189
LOC:	 0.827763	0.702290	0.759882
MISC:	 0.818182	0.674267	0.739286

This is with one extra rare tag, INITCAP, instead of just tagging all words as rare.
If a word's first character was capitalized and it was seen less than 5 times, then
it's treated as INITCAP.
The precision increased for MISC in this case, and recall increased, but precision went
down for some particular tags. However, you can see that it found nearly all of the expected
named entities and it got a much higher percentage of them correct.
The Fscores are, on the whole, better than for the normal RARE viterbi run, with the exception
of LOC.
--------------------------------------------------------------------------------------------------
