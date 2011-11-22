import nltk
import re
import os
import cPickle as pickle

class Article(object):
	def __init__(self, headline, lead, body):
		self.headline = headline
		self.lead = lead
		self.body = body
	def 

def main():
	main_path = '/home/rebecca/Desktop/final project/'
	os.chdir(main_path)

	nytimes = open('nytimes2000.pkl', 'r')
	nytimes = pickle.load(nytimes)

	articles = []
	for article in nytimes:
		articles.append(Article(article['headline'], article['lead'], article['body']))	

if __name__ == '__main__':
	main()
	print "Done."
