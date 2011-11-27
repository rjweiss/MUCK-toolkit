import sys
import re
import os
import cPickle as pickle
import nltk
import nltk.data
from nltk.tokenize.punkt import PunktSentenceTokenizer, PunktWordTokenizer
from nltk import bigrams, trigrams
import itertools
import re

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

#	def tagging(self):

def convert_dir(file):
		article = pickle.load(open(file))
		get_tokens(article)
		get_ngrams(article)

		p = open(file + '.nltk', 'wb')
		pickle.dump(article, p)
		p.close()

def main():
	file = sys.argv[1]
	if file.endswith('.pkl'):
		print "Converting " + file
		convert_dir(file)
	
if __name__ == '__main__':
	main()
