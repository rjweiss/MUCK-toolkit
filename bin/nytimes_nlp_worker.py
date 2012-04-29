import sys
import re
import os
import cPickle as pickle
import nltk
import nltk.data
from nltk.tokenize.punkt import PunktSentenceTokenizer, PunktWordTokenizer
from nltk import bigrams, trigrams
from nltk.tag.stanford import StanfordTagger
import itertools

def get_tokens(dict):
	"""
	This method uses the Punkt Sentence Tokenizer and Punkt Word Tokenizer that come with NLTK.  These tokenizers are unsupervised learners trained on Penn Treebank data.  

	Right now, there is a slight problem where the tokenized sentences and words include punctuation. This creates problems further down the road, so an improved method (removing punctuation) is a necessity.
	"""
	#using the punkt tokenizer because it is trained on real data
	sentence_tokenizer = nltk.data.load('nltk:tokenizers/punkt/english.pickle')
	word_tokenizer = PunktWordTokenizer()
	
	#title
	dict['headline_words'] = word_tokenizer.tokenize(dict['headline'])

	#lead
	dict['lead_sentences'] = sentence_tokenizer.tokenize(dict['lead'], realign_boundaries=True)
	dict['lead_words'] = [word_tokenizer.tokenize(sentence) for sentence in dict['lead_sentences']]
	dict['lead_words'] = list(itertools.chain(*dict['lead_words']))

	#body
	dict['body_sentences'] = sentence_tokenizer.tokenize(dict['body'], realign_boundaries=True)
	dict['body_words'] = [word_tokenizer.tokenize(sentence) for sentence in dict['body_sentences']]
	dict['body_words'] = list(itertools.chain(*dict['body_words']))

	#TODO: remove caps, punctuation

def get_ngrams(dict):
	"""
	This method uses the nltk native bigram and trigram functions to find the bigrams and trigrams from the tokenized headline, lead paragraph, and body text.
	"""
	dict['headline_bigrams'] = bigrams(dict['headline_words'])
	dict['headline_trigrams'] = trigrams(dict['headline_words'])
	dict['lead_bigrams'] = bigrams(dict['lead_words'])
	dict['lead_trigrams'] = trigrams(dict['lead_words'])
	dict['body_bigrams'] = bigrams(dict['body_words'])
	dict['body_trigrams'] = trigrams(dict['body_words'])

def get_tags(dict):
	"""
	This method defines a StanfordTagger that has been pretrained on Wall Street Journal corpora.  Right now, it only tags the headline and the lead paragraph.  Tagging the body text is a very intensive task that might be more useful later.
	"""

	st = StanfordTagger('/home/rebecca/Desktop/fp/newspaper-project/stanford/models/bidirectional-distsim-wsj-0-18.tagger','/home/rebecca/Desktop/fp/newspaper-project/stanford/stanford-postagger.jar')
	dict['headline_tags'] = st.tag(dict['headline_words'])
	dict['lead_tags'] = st.tag(dict['lead_words'])
	#TODO: full body tagging takes a LOOONG time
	#dict['body_tags'] = st(dict['body_words'])

#def get_NER(dict):
#	"""
#	TODO: Explore the Named Entity Recognizer module in NLTK
#	"""

def process_article(file):
	"""
	This method defines the workflow of the NLP work on each document.
	"""
	article = pickle.load(open(file, 'rb'))
	get_tokens(article)
	get_ngrams(article)
	get_tags(article)

	p = open(file + '.nltk', 'wb')
	pickle.dump(article, p)
	p.close()

def get_indicators(file):
	#TODO: Use FreqDist()
	"""
	Implement this method to use FreqDist() from nltk to find the highest occurring unigrams, bigrams, and trigrams in a document, as well as the highest occuring NNP/NP from the tagged documents.  We will use these indicators to try and match articles within a span of time.
	e.g. FreqDist(file['lead_tags']).keys()[:10]
	This returns the top 10 word:tag tuples in the lead paragraph.

	Note that this will currently match on most frequent *words*, so there are a lot of determiners and punctuation ngrams that are dominating the distribution.  TF-IDF?

	"""

def main():
	"""
	This defines the worker tasks that are performed on each document.  This allows multiprocessing.  See nytimes_processing.py for more information.
	"""
	file = sys.argv[1]
	if file.endswith('.pkl'):
		print "Processing " + file
		process_article(file)
	
if __name__ == '__main__':
	main()
