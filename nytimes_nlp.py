import re
import os
import cPickle as pickle
import nltk
import nltk.data
from nltk.tokenize.punkt import PunktSentenceTokenizer, PunktWordTokenizer
from nltk import bigrams, trigrams


class Article(object):

	#might not need access to all these attributes, but for now might as well define them
	def __init__(self, article_dict):
		#TODO: go back to nytimes_parser.py and include .xml filename in dictionary
		self.data = article_dict

	def preprocess(self):
		#using the punkt tokenizer because it is trained on real data
		sentence_tokenizer = nltk.data.load('nltk:tokenizers/punkt/english.pickle')
		word_tokenizer = PunktWordTokenizer()
		
		#title
		self.headline_words = word_tokenizer.tokenize(self.data['headline'])
		self.headline_bigrams = bigrams(self.headline_words)
		self.headline_trigrams = trigrams(self.headline_words)

		#lead
		self.lead_sentences = sentence_tokenizer.tokenize(self.data['lead'], realign_boundaries=True)
		self.lead_words = [word_tokenizer.tokenize(sentence) for sentence in self.lead_sentences]
		self.lead_bigrams = bigrams(self.lead_words)
		self.lead_trigrams = trigrams(self.lead_words)

		#body
		self.body_sentences = sentence_tokenizer.tokenize(self.data['body'], realign_boundaries=True)
		self.body_words = [word_tokenizer.tokenize(sentence) for sentence in self.body_sentences]
		self.body_bigrams = bigrams(self.body_words)
		self.body_trigrams = trigrams(self.body_words)

		#TODO: remove caps, punctuation?

#	def tagging(self):
		
def main():
	main_path = '/home/rebecca/Desktop/final project/'
	os.chdir(main_path)

	articles_pickle = open('nytimes2000.pkl', 'r')
	articles_pickle = pickle.load(articles_pickle)

	articles = []
	for article in articles_pickle:
		articles.append(Article(article))

	for article in articles:
		article.preprocess()

	articles_pickle = open('new_nytimes2000.pkl', 'w')
	pickle.dump(articles, articles_pickle)
	
if __name__ == '__main__':
	main()
	print "Done."
