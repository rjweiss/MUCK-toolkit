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
	dict['headline_bigrams'] = bigrams(dict['headline_words'])
	dict['headline_trigrams'] = trigrams(dict['headline_words'])
	dict['lead_bigrams'] = bigrams(dict['lead_words'])
	dict['lead_trigrams'] = trigrams(dict['lead_words'])
	dict['body_bigrams'] = bigrams(dict['body_words'])
	dict['body_trigrams'] = trigrams(dict['body_words'])

def get_tags(dict):
	st = StanfordTagger('/home/rebecca/Desktop/fp/newspaper-project/stanford/models/bidirectional-distsim-wsj-0-18.tagger','/home/rebecca/Desktop/fp/newspaper-project/stanford/stanford-postagger.jar')
	dict['headline_tags'] = st.tag(dict['headline_words'])
	dict['lead_tags'] = st.tag(dict['lead_words'])
	#TODO: full body tagging takes a LOOONG time
	#dict['body_tags'] = st(dict['body_words'])

def process_article(file):
		article = pickle.load(open(file, 'rb'))
		get_tokens(article)
		get_ngrams(article)
		get_tags(article)

		p = open(file + '.nltk', 'wb')
		pickle.dump(article, p)
		p.close()

def main():
	file = sys.argv[1]
	if file.endswith('.pkl'):
		print "Processing " + file
		process_article(file)
	
if __name__ == '__main__':
	main()
