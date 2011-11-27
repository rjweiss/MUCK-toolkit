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

#class Article(object):

#	def __init__(self, article_dict):
#		self.data = article_dict

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
	#main_path = '/home/rebecca/Desktop/fp/'
	#convert_dir(main_path + '2000/')

	#articles = []
	#for article in articles_pickle:
	#	articles.append(Article(article))

	#count = 0
	#for article in articles:
	#	count += 1
	#	article.tokenizing()
	#	article.ngrams()
	#	if (count % 1000 == 0):
	#		print 'Processed %d files.' % count

	#there is an error here with ipython; because i am pickling in the __main__ namespace, Article will not be in the fake.__dict__ namespace, so you have to manually set it equal to Article when loading the pickle file...super hacky
	#fake=__import__('__main__')
	#fake.__dict['Article'] = Article

	#os.chdir(main_path + '2000/pickle')
	#articles_pickle = open('processed_nytimes2000.pkl', 'wb')
	#pickle.dump(articles, articles_pickle)
	
if __name__ == '__main__':
	main()
