import re
import os
import cPickle as pickle
import nltk
import nltk.data
from nltk.tokenize.punkt import PunktSentenceTokenizer, PunktWordTokenizer
from nltk import bigrams, trigrams
import itertools

class Article(object):

	def __init__(self, article_dict):
		self.data = article_dict

	def tokenizing(self):
		#using the punkt tokenizer because it is trained on real data
		sentence_tokenizer = nltk.data.load('nltk:tokenizers/punkt/english.pickle')
		word_tokenizer = PunktWordTokenizer()
		
		#title
		self.headline_words = word_tokenizer.tokenize(self.data['headline'])

		#lead
		self.lead_sentences = sentence_tokenizer.tokenize(self.data['lead'], realign_boundaries=True)
		self.lead_words = [word_tokenizer.tokenize(sentence) for sentence in self.lead_sentences]
		self.lead_words = list(itertools.chain(*self.lead_words))

		#body
		self.body_sentences = sentence_tokenizer.tokenize(self.data['body'], realign_boundaries=True)
		self.body_words = [word_tokenizer.tokenize(sentence) for sentence in self.body_sentences]
		self.body_words = list(itertools.chain(*self.body_words))

		#TODO: remove caps, punctuation

	def ngrams(self):

		self.headline_bigrams = bigrams(self.headline_words)
		self.headline_trigrams = trigrams(self.headline_words)

		self.lead_bigrams = [bigrams(sentence) for sentence in self.lead_words]
		self.lead_trigrams = [trigrams(sentence) for sentence in self.lead_words]
		
		self.body_bigrams = [bigrams(sentence) for sentence in self.body_words]
		self.body_trigrams = [trigrams(sentence) for sentence in self.body_words]
#	def tagging(self):
		
def main():
	main_path = '/home/rebecca/Desktop/final project/'
	os.chdir(main_path + '2000/')

	articles_pickle = open('nytimes2000.pkl', 'r')
	articles_pickle = pickle.load(articles_pickle)

	articles = []
	for article in articles_pickle:
		articles.append(Article(article))

	count = 0
	for article in articles:
		count += 1
		article.preprocess()
		if (count % 1000 == 0):
			print 'Processed %d files.' % count
		article.preprocess()

	#there is an error here with ipython; because i am pickling in the __main__ namespace, Article will not be in the fake.__dict__ namespace, so you have to manually set it equal to Article when loading the pickle file...super hacky
	#fake=__import__('__main__')
	#fake.__dict['Article'] = Article
	articles_pickle = open('new_nytimes2000.pkl', 'w')
	pickle.dump(articles, articles_pickle)
	
if __name__ == '__main__':
	main()
	print "Done."
